package mil.army.usace.hec.usgs.io;

/**
 * Thrown when USGS cancels a query server-side for running too long, as opposed to any
 * other 4xx/5xx failure. Callers that only catch {@link UsgsRequestException} are unaffected;
 * this exists so {@link UsgsRequest#retrieve()} can distinguish "split the time window and
 * retry" from an ordinary request failure.
 */
public class UsgsQueryCancelledException extends UsgsRequestException {

    UsgsQueryCancelledException(String message) {
        super(message);
    }
}
