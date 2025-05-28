package mil.army.usace.hec.usgs.io;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class UsgsRequest {
    private static final Logger LOGGER = Logger.getLogger(UsgsRequest.class.getName());
    private static final String ERROR_PROPERTY = "error";

    final PropertyChangeSupport support = new PropertyChangeSupport(this);

    UsgsRequest() {
    }

    public String retrieve() {
        Instant start = Instant.now();

        String request = toString();

        Client client = ClientBuilder.newClient();
        LOGGER.info(request);
        try {
            WebTarget target = client.target(request);
            Response response = target.request().get();

            String body;
            try (InputStream is = response.readEntity(InputStream.class)) {
                body = new String(is.readAllBytes());
            }

            int status = response.getStatus();
            if (status == Response.Status.OK.getStatusCode()) {
                return body;
            } else {
                fireError(body);
            }

            response.close();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e, e::getMessage);
            fireError();
        } finally {
            client.close();
        }

        Instant end = Instant.now();
        Duration duration = Duration.between(start, end);
        long seconds = duration.toSeconds();

        LOGGER.info(() -> String.format("Retrieval time: %d:%02d:%02d%n", seconds / 3600, (seconds % 3600) / 60, (seconds % 60)));

        return null;
    }

    private void fireError(String html) {
        Document doc = Jsoup.parse(html);
        String title = doc.title().toLowerCase();
        Element h1 = doc.selectFirst("h1");
        if (title.contains(ERROR_PROPERTY) && h1 != null && h1.hasText()) {
            support.firePropertyChange(ERROR_PROPERTY, null, h1.text());
        } else {
            fireError();
        }
    }

    private void fireError() {
        support.firePropertyChange(ERROR_PROPERTY, null, String.format("Could not access: %s", this));
    }

    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        support.addPropertyChangeListener(pcl);
    }

    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        support.removePropertyChangeListener(pcl);
    }
}
