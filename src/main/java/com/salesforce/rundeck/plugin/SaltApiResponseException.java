package com.salesforce.rundeck.plugin;

/**
 * Represents an exception dispatching to salt-api.
 */
public class SaltApiResponseException extends Exception {
    public SaltApiResponseException(String message) {
        super(message);
    }
}
