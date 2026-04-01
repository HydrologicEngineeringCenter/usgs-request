package mil.army.usace.hec.usgs.io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class UsgsApiKeyCache {
    private static final Logger LOGGER = Logger.getLogger(UsgsApiKeyCache.class.getName());
    private static final String FILE_NAME = "api_key";

    private UsgsApiKeyCache() {
    }

    public static String load() {
        Path file = getCacheFile();
        if (!Files.exists(file)) {
            return null;
        }

        try {
            String key = Files.readString(file).trim();
            if (!UsgsApiKeyValidator.isValid(key)) {
                return null;
            }
            return key;
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to read cached API key", e);
            return null;
        }
    }

    public static void save(String apiKey) {
        if (!UsgsApiKeyValidator.isValid(apiKey)) {
            return;
        }

        Path dir = getCacheDir();
        try {
            Files.createDirectories(dir);
            Files.writeString(dir.resolve(FILE_NAME), apiKey);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to cache API key", e);
        }
    }

    private static Path getCacheFile() {
        return getCacheDir().resolve(FILE_NAME);
    }

    private static Path getCacheDir() {
        String osName = System.getProperty("os.name").toLowerCase();
        String userHome = System.getProperty("user.home");

        if (osName.startsWith("windows")) {
            String appData = System.getenv("APPDATA");
            if (appData == null) {
                appData = Path.of(userHome, "AppData", "Roaming").toString();
            }
            return Path.of(appData, "HEC", "usgs-request");
        } else if (osName.contains("mac")) {
            return Path.of(userHome, "Library", "Application Support", "HEC", "usgs-request");
        } else {
            return Path.of(userHome, ".hec", "usgs-request");
        }
    }
}
