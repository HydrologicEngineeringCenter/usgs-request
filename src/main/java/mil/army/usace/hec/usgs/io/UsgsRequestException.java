package mil.army.usace.hec.usgs.io;

public class UsgsRequestException extends RuntimeException {

    UsgsRequestException(String message) {
        super(message);
    }

    UsgsRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
