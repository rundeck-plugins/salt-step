package org.rundeck.plugin.salt.output;

/**
 * Represents an exception while trying to parse a salt json minion response.
 */
public class SaltReturnResponseParseException extends RuntimeException {
    public SaltReturnResponseParseException(String message) {
        super(message);
    }

    public SaltReturnResponseParseException(Throwable e) {
        super(e);
    }
}
