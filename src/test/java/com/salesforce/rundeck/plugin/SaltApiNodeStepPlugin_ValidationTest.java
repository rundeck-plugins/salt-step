package com.salesforce.rundeck.plugin;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.dtolabs.rundeck.core.execution.workflow.steps.node.NodeStepException;
import com.salesforce.rundeck.plugin.SaltApiNodeStepPlugin.SaltApiNodeStepFailureReason;
import com.salesforce.rundeck.plugin.validation.SaltStepValidationException;
import com.salesforce.rundeck.plugin.validation.Validators;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Validators.class)
public class SaltApiNodeStepPlugin_ValidationTest extends AbstractSaltApiNodeStepPluginTest {
    
    @Test
    public void testValidateAllArguments() throws SaltStepValidationException {
        PowerMockito.mockStatic(Validators.class);
        PowerMockito.doNothing().when(Validators.class);
        Validators.checkNotEmpty(Mockito.anyString(), Mockito.anyString(),
                Mockito.any(SaltApiNodeStepFailureReason.class), Mockito.same(node));

        plugin.validate(PARAM_USER, PARAM_PASSWORD, node);

        PowerMockito.verifyStatic();
        Validators.checkNotEmpty(SaltApiNodeStepPlugin.SALT_API_END_POINT_OPTION_NAME, PARAM_ENDPOINT,
                SaltApiNodeStepFailureReason.ARGUMENTS_MISSING, node);
        PowerMockito.verifyStatic();
        Validators.checkNotEmpty(SaltApiNodeStepPlugin.SALT_API_FUNCTION_OPTION_NAME, PARAM_FUNCTION,
                SaltApiNodeStepFailureReason.ARGUMENTS_MISSING, node);
        PowerMockito.verifyStatic();
        Validators.checkNotEmpty(SaltApiNodeStepPlugin.SALT_API_EAUTH_OPTION_NAME, PARAM_EAUTH,
                SaltApiNodeStepFailureReason.ARGUMENTS_MISSING, node);
        PowerMockito.verifyStatic();
        Validators.checkNotEmpty(SaltApiNodeStepPlugin.SALT_USER_OPTION_NAME, PARAM_USER,
                SaltApiNodeStepFailureReason.ARGUMENTS_MISSING, node);
        PowerMockito.verifyStatic();
        Validators.checkNotEmpty(SaltApiNodeStepPlugin.SALT_PASSWORD_OPTION_NAME, PARAM_PASSWORD,
                SaltApiNodeStepFailureReason.ARGUMENTS_MISSING, node);
        PowerMockito.verifyNoMoreInteractions(Validators.class);
    }

    @Test
    public void testValidateThrowsIfValidatorThrows() throws SaltStepValidationException {
        SaltStepValidationException e = new SaltStepValidationException("some property", "Some message",
                SaltApiNodeStepFailureReason.ARGUMENTS_INVALID, node.getNodename());
        PowerMockito.mockStatic(Validators.class);
        PowerMockito.doThrow(e).when(Validators.class);
        Validators.checkNotEmpty(Mockito.anyString(), Mockito.anyString(),
                Mockito.any(SaltApiNodeStepFailureReason.class), Mockito.same(node));

        try {
            plugin.validate(PARAM_USER, PARAM_PASSWORD, node);
            Assert.fail("Expected exception");
        } catch (SaltStepValidationException ne) {
            Assert.assertSame(e, ne);
        }
    }

    @Test
    public void testValidateChecksValidEndpointHttpUrl() throws NodeStepException {
        plugin.saltEndpoint = "http://some.machine.com";
        plugin.validate(PARAM_USER, PARAM_PASSWORD, node);
    }

    @Test
    public void testValidateChecksValidEndpointHttpsUrl() throws NodeStepException {
        plugin.saltEndpoint = "https://some.machine.com";
        plugin.validate(PARAM_USER, PARAM_PASSWORD, node);
    }

    @Test
    public void testValidateChecksInvalidEndpointUrl() throws NodeStepException {
        plugin.saltEndpoint = "ftp://some.machine.com";
        try {
            plugin.validate(PARAM_USER, PARAM_PASSWORD, node);
            Assert.fail("Expected failure.");
        } catch (SaltStepValidationException e) {
            Assert.assertEquals(SaltApiNodeStepFailureReason.ARGUMENTS_INVALID, e.getFailureReason());
            Assert.assertEquals(SaltApiNodeStepPlugin.SALT_API_END_POINT_OPTION_NAME, e.getFieldName());
        }
    }
}
