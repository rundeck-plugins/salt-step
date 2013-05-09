package org.rundeck.plugin.salt.validation;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.rundeck.plugin.salt.SaltApiNodeStepPlugin.SaltApiNodeStepFailureReason;
import org.rundeck.plugin.salt.validation.SaltStepValidationException;
import org.rundeck.plugin.salt.validation.Validators;

import com.dtolabs.rundeck.core.common.INodeEntry;
import com.dtolabs.rundeck.core.execution.workflow.steps.FailureReason;
import com.dtolabs.rundeck.core.execution.workflow.steps.node.NodeStepException;

public class ValidatorsTest {
    protected String NODE_NAME = "node";
    protected INodeEntry entry;

    @Before
    public void setup() {
        entry = Mockito.mock(INodeEntry.class);
        Mockito.when(entry.getNodename()).thenReturn(NODE_NAME);
    }

    @Test
    public void testCheckNotEmptyWithNullString() {
        FailureReason reason = SaltApiNodeStepFailureReason.ARGUMENTS_MISSING;
        try {
            Validators.checkNotEmpty("some prop", null, reason, entry);
        } catch (NodeStepException e) {
            Assert.assertEquals(reason, e.getFailureReason());
            Assert.assertEquals(NODE_NAME, e.getNodeName());
        }
    }

    @Test
    public void testCheckNotEmptyWithEmptyString() {
        FailureReason reason = SaltApiNodeStepFailureReason.ARGUMENTS_MISSING;
        try {
            Validators.checkNotEmpty("some prop", "", reason, entry);
        } catch (NodeStepException e) {
            Assert.assertEquals(reason, e.getFailureReason());
            Assert.assertEquals(NODE_NAME, e.getNodeName());
        }
    }

    @Test
    public void testCheckNotEmptyWithNonEmptyString() throws SaltStepValidationException {
        FailureReason reason = SaltApiNodeStepFailureReason.ARGUMENTS_MISSING;
        Validators.checkNotEmpty("some prop", "value", reason, entry);
    }

}
