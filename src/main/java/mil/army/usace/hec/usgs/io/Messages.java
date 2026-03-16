package mil.army.usace.hec.usgs.io;

import java.text.MessageFormat;
import java.util.ResourceBundle;

final class Messages {
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("mil.army.usace.hec.usgs.io.message");

    private Messages() {
    }

    static String format(String key, Object... args) {
        return MessageFormat.format(BUNDLE.getString(key), args);
    }
}