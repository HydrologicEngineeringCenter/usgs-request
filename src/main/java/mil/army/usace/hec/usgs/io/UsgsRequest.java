package mil.army.usace.hec.usgs.io;

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
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class UsgsRequest {
    public static final String ERROR_PROPERTY = "error";
    public static final String PROGRESS_PROPERTY = "progress";
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

    @SuppressWarnings("unchecked")
    public String retrieve() {
        Instant start = Instant.now();

        String baseRequest = toString();

        try {
            // Fetch first page
            String firstBody = fetchPage(baseRequest);
            JSONParser parser = new JSONParser();
            JSONObject firstJson = (JSONObject) parser.parse(firstBody);

            // Detect API error responses returned with 200 status
            checkForApiError(firstJson);

            String nextUrl = getNextLink(firstJson);
            if (nextUrl == null) {
                return firstBody;
            }

            // Paginate to collect all features by following "next" links
            int pageCount = 1;
            JSONArray allFeatures = (JSONArray) firstJson.get("features");
            if (allFeatures == null) {
                allFeatures = new JSONArray();
                firstJson.put("features", allFeatures);
            }
            fireProgress(1, allFeatures.size());

            while (nextUrl != null) {
                String pageBody = fetchPage(nextUrl);
                JSONObject pageJson = (JSONObject) parser.parse(pageBody);

                JSONArray pageFeatures = (JSONArray) pageJson.get("features");
                if (pageFeatures == null || pageFeatures.isEmpty()) {
                    break;
                }

                pageCount++;
                allFeatures.addAll(pageFeatures);
                fireProgress(pageCount, allFeatures.size());
                nextUrl = getNextLink(pageJson);
            }

            // Update metadata and return merged response
            int totalRetrieved = allFeatures.size();
            firstJson.put("numberReturned", totalRetrieved);
            fireProgress(Messages.format("retrieved.all.records", totalRetrieved));
            return firstJson.toJSONString();
        } catch (UsgsRequestException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e, e::getMessage);
            fireError();
            throw new UsgsRequestException(Messages.format("failed.to.retrieve", baseRequest), e);
        } finally {
            Instant end = Instant.now();
            Duration duration = Duration.between(start, end);
            long seconds = duration.toSeconds();
            LOGGER.info(() -> Messages.format("retrieval.time", seconds / 3600, (seconds % 3600) / 60, seconds % 60));
        }
    }

    private String fetchPage(String request) {
        LOGGER.info(request);
        WebTarget target = CLIENT.target(request);

        try (Response response = target.request().get()) {
            String body;
            try (InputStream is = response.readEntity(InputStream.class)) {
                body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            }

            int status = response.getStatus();
            if (status == Response.Status.OK.getStatusCode()) {
                return body;
            } else {
                String errorMessage = extractErrorMessage(body);
                fireError(errorMessage);
                throw new UsgsRequestException(Messages.format("http.error", status, errorMessage));
            }
        } catch (UsgsRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new UsgsRequestException(Messages.format("failed.to.retrieve", request), e);
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

    private String extractErrorMessage(String body) {
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

        return Messages.format("could.not.access", this);
    }

    private void fireProgress(String message) {
        support.firePropertyChange(PROGRESS_PROPERTY, null, message);
    }

    private void fireProgress(int page, int totalRecords) {
        fireProgress(Messages.format("progress.page.fetched", page, totalRecords));
    }

    private void fireError(String message) {
        support.firePropertyChange(ERROR_PROPERTY, null, message);
    }

    private void fireError() {
        fireError(Messages.format("could.not.access", this));
    }

    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        support.addPropertyChangeListener(pcl);
    }

    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        support.removePropertyChangeListener(pcl);
    }
}
