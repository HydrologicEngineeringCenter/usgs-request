package mil.army.usace.hec.usgs.io;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

class UrlValidator {
    private static final Logger LOGGER = Logger.getLogger(UrlValidator.class.getName());

    private UrlValidator() {
    }

    static String validate(String urlString) {
        if (UrlValidator.isSafeUrl(urlString))
            return urlString;

        LOGGER.log(Level.SEVERE, () -> "BAD URL: " + urlString);
        return "";
    }

    private static boolean isSafeUrl(String urlString) {
        try {
            URL url = new URL(urlString);

            // Allow only http and https
            String protocol = url.getProtocol();
            if (!protocol.equals("http") && !protocol.equals("https")) {
                return false;
            }

            return isGovSite(url) && !isPrivateHost(url);
        } catch (MalformedURLException e) {
            return false;
        }
    }

    private static boolean isGovSite(URL url) {
        String host = url.getHost().toLowerCase();
        return host.endsWith(".gov");
    }

    private static boolean isPrivateHost(URL url) {
        try {
            String host = url.getHost();
            InetAddress address = InetAddress.getByName(host);

            return address.isAnyLocalAddress()     // 0.0.0.0
                    || address.isLoopbackAddress()     // 127.0.0.1
                    || address.isSiteLocalAddress();   // 10.x.x.x, 192.168.x.x, 172.16.x.x–172.31.x.x

        } catch (UnknownHostException e) {
            LOGGER.log(Level.SEVERE, () -> "Invalid or unresolvable URL: " + e.getMessage());
            return false;
        }
    }
}
