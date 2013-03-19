package com.salesforce.rundeck.plugin;

/**
 * Represents an exception dispatching to salt-api.
 */
public class SaltApiException extends Exception {
    public SaltApiException(String message) {
        super(message);
    }
}
