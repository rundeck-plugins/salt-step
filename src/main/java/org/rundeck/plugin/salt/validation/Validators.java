package org.rundeck.plugin.salt.validation;

import org.apache.commons.lang.StringUtils;

import com.dtolabs.rundeck.core.common.INodeEntry;
import com.dtolabs.rundeck.core.execution.workflow.steps.FailureReason;

/**
 * Utility methods to help validate properties within node step plugins.
 */
public class Validators {
    
    /**
     * Checks that the given propertyValue is not empty or null.
     * 
     * @param propertyName The name of the property that is being checked
     * @param propertyValue The value of the property that is being checked.
     * @param reason A {@link com.dtolabs.rundeck.core.execution.workflow.steps.node.NodeStepFailure} for this this validation failure.
     * @param nodeName The node this node step is executing against.
     */ 
    public static void checkNotEmpty(String propertyName, String propertyValue, FailureReason reason, INodeEntry entry)
            throws SaltStepValidationException {
        if (StringUtils.isEmpty(propertyValue)) {
            throw new SaltStepValidationException(propertyName, String.format("%s is a required property.",
                    propertyName), reason, entry.getNodename());
        }
    }
}
