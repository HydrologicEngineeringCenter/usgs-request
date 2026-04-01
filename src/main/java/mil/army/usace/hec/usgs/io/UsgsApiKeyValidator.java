package mil.army.usace.hec.usgs.io;

import java.util.regex.Pattern;

public final class UsgsApiKeyValidator {
    private static final int REQUIRED_LENGTH = 40;
    private static final Pattern VALID_PATTERN = Pattern.compile("^[a-zA-Z0-9\\-]+$");

    private UsgsApiKeyValidator() {
    }

    public static boolean isValid(String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            return false;
        }

        if (apiKey.length() != REQUIRED_LENGTH) {
            return false;
        }

        return VALID_PATTERN.matcher(apiKey).matches();
    }
}
