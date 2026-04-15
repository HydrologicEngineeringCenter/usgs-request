package mil.army.usace.hec.usgs.io;

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class UsgsRequest {
    public static final String ERROR_PROPERTY = "error";
    public static final String PROGRESS_PROPERTY = "progress";
    private static final int HTTP_TOO_MANY_REQUESTS = 429;
    private static final int HTTP_SERVICE_UNAVAILABLE = 503;
    private static final int MAX_RETRIES = 3;
    private static final long DEFAULT_RETRY_SECONDS = 2;
    private static final long MAX_RETRY_SECONDS = 60;
    private static final Logger LOGGER = Logger.getLogger(UsgsRequest.class.getName());
    private static final Client CLIENT = ClientBuilder.newClient();

    private final PropertyChangeSupport support = new PropertyChangeSupport(this);
    private final String apiKey;

    UsgsRequest() {
        this(null);
    }

    UsgsRequest(String apiKey) {
        this.apiKey = apiKey;
    }

    String formatApiKey() {
        if (apiKey == null || apiKey.isBlank() || !UsgsApiKeyValidator.isValid(apiKey))
            return "";

        return "&api_key=" + apiKey;
    }

    protected List<String> buildRequestUrls() {
        return List.of(toString());
    }

    @SuppressWarnings("unchecked")
    public String retrieve() {
        Instant start = Instant.now();

        List<String> urls = buildRequestUrls();
        int chunkCount = urls.size();
        String firstRequest = urls.get(0);

        try {
            JSONParser parser = new JSONParser();
            JSONObject mergedJson = null;
            JSONArray mergedFeatures = null;

            for (int i = 0; i < chunkCount; i++) {
                String chunkUrl = urls.get(i);
                int chunkIndex = i + 1;

                JSONObject chunkJson = retrieveChunk(parser, chunkUrl, chunkIndex, chunkCount, mergedFeatures);

                if (mergedJson == null) {
                    mergedJson = chunkJson;
                    mergedFeatures = (JSONArray) mergedJson.get("features");
                    if (mergedFeatures == null) {
                        mergedFeatures = new JSONArray();
                        mergedJson.put("features", mergedFeatures);
                    }
                } else {
                    JSONArray chunkFeatures = (JSONArray) chunkJson.get("features");
                    if (chunkFeatures != null) {
                        mergedFeatures.addAll(chunkFeatures);
                    }
                }
            }

            int totalRetrieved = mergedFeatures.size();
            mergedJson.put("numberReturned", totalRetrieved);
            fireProgress(Messages.format("retrieved.all.records", totalRetrieved));
            return mergedJson.toJSONString();
        } catch (UsgsRequestException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e, e::getMessage);
            fireError(Messages.format("could.not.access", firstRequest));
            throw new UsgsRequestException(Messages.format("failed.to.retrieve", firstRequest), e);
        } finally {
            Instant end = Instant.now();
            Duration duration = Duration.between(start, end);
            long seconds = duration.toSeconds();
            String timing = Messages.format("retrieval.time", seconds / 3600, (seconds % 3600) / 60, seconds % 60);
            LOGGER.info(timing);
            fireProgress(timing);
        }
    }

    @SuppressWarnings("unchecked")
    private JSONObject retrieveChunk(JSONParser parser, String url, int chunkIndex, int chunkCount,
                                     JSONArray priorFeatures) throws Exception {
        String firstBody = fetchPage(url);
        JSONObject firstJson = (JSONObject) parser.parse(firstBody);

        checkForApiError(firstJson);

        JSONArray chunkFeatures = (JSONArray) firstJson.get("features");
        if (chunkFeatures == null) {
            chunkFeatures = new JSONArray();
            firstJson.put("features", chunkFeatures);
        }

        int priorCount = priorFeatures != null ? priorFeatures.size() : 0;
        int pageCount = 1;
        fireProgress(chunkIndex, chunkCount, pageCount, priorCount + chunkFeatures.size());

        String nextUrl = ensureApiKey(getNextLink(firstJson));
        while (nextUrl != null) {
            String pageBody = fetchPage(nextUrl);
            JSONObject pageJson = (JSONObject) parser.parse(pageBody);

            JSONArray pageFeatures = (JSONArray) pageJson.get("features");
            if (pageFeatures == null || pageFeatures.isEmpty()) {
                break;
            }

            pageCount++;
            chunkFeatures.addAll(pageFeatures);
            fireProgress(chunkIndex, chunkCount, pageCount, priorCount + chunkFeatures.size());
            nextUrl = ensureApiKey(getNextLink(pageJson));
        }

        return firstJson;
    }

    private String ensureApiKey(String url) {
        if (url == null) {
            return null;
        }
        String keyParam = formatApiKey();
        if (keyParam.isEmpty() || url.contains("api_key=")) {
            return url;
        }
        return url + keyParam;
    }

    private String fetchPage(String request) {
        LOGGER.info(request);
        WebTarget target = CLIENT.target(request);

        for (int attempt = 0; attempt <= MAX_RETRIES; attempt++) {
            try (Response response = target.request().get()) {
                String body;
                try (InputStream is = response.readEntity(InputStream.class)) {
                    body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                }

                int status = response.getStatus();
                if (status == Response.Status.OK.getStatusCode()) {
                    return body;
                }

                if ((status == HTTP_TOO_MANY_REQUESTS || status == HTTP_SERVICE_UNAVAILABLE)
                        && attempt < MAX_RETRIES) {
                    long waitSeconds = parseRetryAfter(response.getHeaderString("Retry-After"), attempt);
                    fireRetryMessage("rate.limited.retrying", waitSeconds, attempt);
                    sleepSeconds(waitSeconds);
                    continue;
                }

                String errorMessage = extractErrorMessage(body, request);
                fireError(errorMessage);
                throw new UsgsRequestException(Messages.format("http.error", status, errorMessage));
            } catch (UsgsRequestException e) {
                throw e;
            } catch (ProcessingException | IOException e) {
                if (attempt < MAX_RETRIES) {
                    long waitSeconds = Math.min(DEFAULT_RETRY_SECONDS * (1L << attempt), MAX_RETRY_SECONDS);
                    fireRetryMessage("network.error.retrying", waitSeconds, attempt);
                    sleepSeconds(waitSeconds);
                    continue;
                }
                throw new UsgsRequestException(Messages.format("failed.to.retrieve", request), e);
            } catch (Exception e) {
                throw new UsgsRequestException(Messages.format("failed.to.retrieve", request), e);
            }
        }
        throw new UsgsRequestException(Messages.format("failed.to.retrieve", request));
    }

    private void fireRetryMessage(String messageKey, long waitSeconds, int attempt) {
        String msg = Messages.format(messageKey, waitSeconds, attempt + 1, MAX_RETRIES);
        fireProgress(msg);
        LOGGER.info(msg);
    }

    private static long parseRetryAfter(String header, int attempt) {
        long fallback = Math.min(DEFAULT_RETRY_SECONDS * (1L << attempt), MAX_RETRY_SECONDS);
        if (header == null || header.isBlank()) {
            return fallback;
        }
        try {
            long seconds = Long.parseLong(header.trim());
            if (seconds > 0) {
                return Math.min(seconds, MAX_RETRY_SECONDS);
            }
        } catch (NumberFormatException ignored) {
            // Header may be HTTP-date form; fall back to exponential backoff.
        }
        return fallback;
    }

    private static void sleepSeconds(long seconds) {
        try {
            Thread.sleep(Duration.ofSeconds(seconds).toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new UsgsRequestException("Interrupted while waiting to retry", e);
        }
    }

    private static String getNextLink(JSONObject json) {
        JSONArray links = (JSONArray) json.get("links");
        if (links == null) return null;
        for (Object linkObj : links) {
            JSONObject link = (JSONObject) linkObj;
            if ("next".equals(link.get("rel"))) {
                return (String) link.get("href");
            }
        }
        return null;
    }

    private static void checkForApiError(JSONObject json) {
        Object code = json.get("code");
        if (code != null && !json.containsKey("features")) {
            String description = (String) json.get("description");
            String message = description != null ? description : String.valueOf(code);
            throw new UsgsRequestException(message);
        }
    }

    private String extractErrorMessage(String body, String requestUrl) {
        // Try JSON error format (OGC API)
        try {
            JSONObject json = (JSONObject)
                    new JSONParser().parse(body);
            String description = (String) json.get("description");
            if (description != null && !description.isBlank()) {
                return description;
            }
        } catch (Exception ignored) {
            // Not JSON — fall through
        }

        // Try HTML error format (legacy)
        Document doc = Jsoup.parse(body);
        String title = doc.title().toLowerCase();
        Element h1 = doc.selectFirst("h1");
        if (title.contains(ERROR_PROPERTY) && h1 != null && h1.hasText()) {
            return h1.text();
        }

        return Messages.format("could.not.access", requestUrl);
    }

    private void fireProgress(String message) {
        support.firePropertyChange(PROGRESS_PROPERTY, null, message);
    }

    private void fireProgress(int chunkIndex, int chunkCount, int page, int totalRecords) {
        if (chunkCount > 1) {
            fireProgress(Messages.format("progress.chunk.page.fetched", chunkIndex, chunkCount, page, totalRecords));
        } else {
            fireProgress(Messages.format("progress.page.fetched", page, totalRecords));
        }
    }

    private void fireError(String message) {
        support.firePropertyChange(ERROR_PROPERTY, null, message);
    }

    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        support.addPropertyChangeListener(pcl);
    }

    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        support.removePropertyChangeListener(pcl);
    }
}
