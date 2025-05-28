package mil.army.usace.hec.usgs.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

enum Config {
    INSTANCE;

    private final Properties properties;

    Config() {
        properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                throw new IllegalStateException("config.properties file not found in classpath");
            }
            properties.load(input);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load config.properties", e);
        }
    }

    String getOrDefault(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
}
