package org.rundeck.plugin.salt.validation;

import com.dtolabs.rundeck.core.execution.workflow.steps.FailureReason;
import com.dtolabs.rundeck.core.execution.workflow.steps.node.NodeStepException;

/**
 * Represents an exception when validating a field for plugin execution.
 */
public class SaltStepValidationException extends NodeStepException {
    
    protected final String fieldName;
    
    /**
     * @param fieldName The name of the field that failed validation.
     * @param message A human readable message of the problem.
     * @param reason The failure reason for the node step.
     * @param nodeName The node this execution was running against.
     */
    public SaltStepValidationException(String fieldName, String message, FailureReason reason, String nodeName) {
        super(message, reason, nodeName);
        this.fieldName = fieldName;
    }
    
    public String getFieldName() {
        return fieldName;
    }
}
