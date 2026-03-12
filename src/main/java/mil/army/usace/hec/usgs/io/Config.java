package mil.army.usace.hec.usgs.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

enum Config {
    INSTANCE;

    private final Properties properties;

    Config() {
        properties = new Properties();
        Logger logger = Logger.getLogger(Config.class.getName());
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                logger.info("config.properties not found in classpath; using defaults");
                return;
            }
            properties.load(input);
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to load config.properties; using defaults", e);
        }
    }

    String getOrDefault(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
}
