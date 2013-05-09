package org.rundeck.plugin.salt;

/**
 * Represents a mismatch between salt was told to target and what salt 
 * actually targetted.
 */
public class SaltTargettingMismatchException extends Exception {
    public SaltTargettingMismatchException(String message) {
        super(message);
    }
}
