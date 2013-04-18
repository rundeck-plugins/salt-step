package com.salesforce.rundeck.plugin;

import junit.framework.Assert;

import org.apache.http.HttpException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import com.dtolabs.rundeck.core.Constants;
import com.dtolabs.rundeck.core.execution.workflow.steps.node.NodeStepException;
import com.salesforce.rundeck.plugin.SaltApiNodeStepPlugin.SaltApiNodeStepFailureReason;
import com.salesforce.rundeck.plugin.output.SaltReturnResponse;
import com.salesforce.rundeck.plugin.output.SaltReturnResponseParseException;
import com.salesforce.rundeck.plugin.validation.SaltStepValidationException;

public class SaltApiNodeStepPlugin_ExecuteTest extends AbstractSaltApiNodeStepPluginTest {

    @Before
    public void setup() throws Exception {
        spyPlugin();
    }

    @Test
    public void testExecuteWithAuthenticationFailure() throws Exception {
        setupAuthenticate(null);

        try {
            plugin.executeNodeStep(pluginContext, configuration, node);
            Assert.fail("Expected authentication failure");
        } catch (NodeStepException e) {
            Assert.assertEquals(SaltApiNodeStepFailureReason.AUTHENTICATION_FAILURE, e.getFailureReason());
        }

        Mockito.verifyZeroInteractions(client, post);
    }

    @Test
    public void testExecuteWithValidationFailure() throws Exception {
        SaltStepValidationException e = new SaltStepValidationException("some property", "Some message",
                SaltApiNodeStepFailureReason.ARGUMENTS_INVALID, node.getNodename());
        Mockito.doThrow(e).when(plugin)
                .validate(Mockito.eq(PARAM_USER), Mockito.eq(PARAM_PASSWORD), Mockito.same(node));
        try {
            plugin.executeNodeStep(pluginContext, configuration, node);
            Assert.fail("Expected exception");
        } catch (SaltStepValidationException ne) {
            Assert.assertSame(e, ne);
        }
    }

    @Test
    public void testExecuteWithDataContextMissing() {
        dataContext.clear();
        try {
            plugin.executeNodeStep(pluginContext, configuration, node);
            Assert.fail("Expected exception");
        } catch (NodeStepException e) {
            Assert.assertEquals(SaltApiNodeStepFailureReason.ARGUMENTS_MISSING, e.getFailureReason());
        }
    }

    @Test
    public void testExecuteWithSuccessfulExitCode() throws Exception {
        setupAuthenticate();
        setupDoReturnJidWhenSubmitJob();
        setupDoReturnHostResponseWhenWaitForResponse();
        
        String output1 = "line 1 of output";
        String output2 = "line 2 of output";
        String error1 = "line 1 of error";
        String error2 = "line 2 of error";
        setupDoReturnSaltResponseWhenExtractResponse(0, new String[] { output1,  output2 }, new String[] { error1, error2 });

        plugin.executeNodeStep(pluginContext, configuration, node);

        Mockito.verify(returnHandlerRegistry, Mockito.times(1)).getHandlerFor(Mockito.eq(PARAM_FUNCTION),
                Mockito.same(plugin.defaultReturnHandler));
        Mockito.verify(returnHandler, Mockito.times(1)).extractResponse(Mockito.eq(HOST_RESPONSE));

        InOrder ordering = Mockito.inOrder(logger);
        ordering.verify(logger, Mockito.times(1)).log(Mockito.eq(Constants.INFO_LEVEL), Mockito.eq(output1));
        ordering.verify(logger, Mockito.times(1)).log(Mockito.eq(Constants.INFO_LEVEL), Mockito.eq(output2));
        ordering.verify(logger, Mockito.times(1)).log(Mockito.eq(Constants.ERR_LEVEL), Mockito.eq(error1));
        ordering.verify(logger, Mockito.times(1)).log(Mockito.eq(Constants.ERR_LEVEL), Mockito.eq(error2));
    }

    @Test
    public void testExecuteWithUnsuccessfulExitCode() throws Exception {
        setupAuthenticate();
        setupDoReturnJidWhenSubmitJob();
        setupDoReturnHostResponseWhenWaitForResponse();
        
        String output1 = "line 1 of output";
        String output2 = "line 2 of output";
        String error1 = "line 1 of error";
        String error2 = "line 2 of error";
        setupDoReturnSaltResponseWhenExtractResponse(1, new String[] { output1,  output2 }, new String[] { error1, error2 });

        try {
            plugin.executeNodeStep(pluginContext, configuration, node);
            Assert.fail("Expected failure due to exit code.");
        } catch (NodeStepException e) {
            Assert.assertEquals(SaltApiNodeStepFailureReason.EXIT_CODE, e.getFailureReason());
        }

        Mockito.verify(returnHandlerRegistry, Mockito.times(1)).getHandlerFor(Mockito.eq(PARAM_FUNCTION),
                Mockito.same(plugin.defaultReturnHandler));
        Mockito.verify(returnHandler, Mockito.times(1)).extractResponse(Mockito.eq(HOST_RESPONSE));

        InOrder ordering = Mockito.inOrder(logger);
        ordering.verify(logger, Mockito.times(1)).log(Mockito.eq(Constants.INFO_LEVEL), Mockito.eq(output1));
        ordering.verify(logger, Mockito.times(1)).log(Mockito.eq(Constants.INFO_LEVEL), Mockito.eq(output2));
        ordering.verify(logger, Mockito.times(1)).log(Mockito.eq(Constants.ERR_LEVEL), Mockito.eq(error1));
        ordering.verify(logger, Mockito.times(1)).log(Mockito.eq(Constants.ERR_LEVEL), Mockito.eq(error2));
    }

    @Test
    public void testExecuteWithSaltResponseParseException() throws Exception {
        setupAuthenticate();
        setupDoReturnJidWhenSubmitJob();
        setupDoReturnHostResponseWhenWaitForResponse();

        SaltReturnResponseParseException pe = new SaltReturnResponseParseException("message");
        Mockito.when(returnHandler.extractResponse(Mockito.anyString())).thenThrow(pe);

        try {
            plugin.executeNodeStep(pluginContext, configuration, node);
            Assert.fail("Expected failure due to response parse exception.");
        } catch (NodeStepException e) {
            Assert.assertEquals(SaltApiNodeStepFailureReason.SALT_API_FAILURE, e.getFailureReason());
            Assert.assertSame(pe, e.getCause());
        }

        Mockito.verify(returnHandlerRegistry, Mockito.times(1)).getHandlerFor(Mockito.eq(PARAM_FUNCTION),
                Mockito.same(plugin.defaultReturnHandler));
        Mockito.verify(returnHandler, Mockito.times(1)).extractResponse(Mockito.eq(HOST_RESPONSE));
    }

    @Test
    public void testExecuteWithSaltApiException() throws Exception {
        setupAuthenticate();
        setupDoThrowWhenSubmitJob(new SaltApiException("Some message"));

        try {
            plugin.executeNodeStep(pluginContext, configuration, node);
            Assert.fail("Expected node step failure.");
        } catch (NodeStepException e) {
            Assert.assertEquals(SaltApiNodeStepFailureReason.SALT_API_FAILURE, e.getFailureReason());
        }
    }

    @Test
    public void testExecuteWithSaltTargettingException() throws Exception {
        setupAuthenticate();
        setupDoThrowWhenSubmitJob(new SaltTargettingMismatchException("Some message"));

        try {
            plugin.executeNodeStep(pluginContext, configuration, node);
            Assert.fail("Expected node step failure.");
        } catch (NodeStepException e) {
            Assert.assertEquals(SaltApiNodeStepFailureReason.SALT_TARGET_MISMATCH, e.getFailureReason());
        }
    }

    @Test
    public void testExecuteWithHttpException() throws Exception {
        setupAuthenticate();
        setupDoThrowWhenSubmitJob(new HttpException("Some message"));

        try {
            plugin.executeNodeStep(pluginContext, configuration, node);
            Assert.fail("Expected node step failure.");
        } catch (NodeStepException e) {
            Assert.assertEquals(SaltApiNodeStepFailureReason.COMMUNICATION_FAILURE, e.getFailureReason());
        }
    }

    @Test
    public void testExecuteWithInterruptedException() throws Exception {
        setupAuthenticate();
        setupDoReturnJidWhenSubmitJob();

        Mockito.doThrow(new InterruptedException())
                .when(plugin)
                .waitForJidResponse(Mockito.same(pluginContext), Mockito.same(client), Mockito.eq(AUTH_TOKEN),
                        Mockito.eq(OUTPUT_JID), Mockito.eq(PARAM_MINION_NAME));

        try {
            plugin.executeNodeStep(pluginContext, configuration, node);
            Assert.fail("Expected node step failure.");
        } catch (NodeStepException e) {
            Assert.assertEquals(SaltApiNodeStepFailureReason.INTERRUPTED, e.getFailureReason());
        }
    }

    protected SaltApiNodeStepPlugin_ExecuteTest setupDoReturnSaltResponseWhenExtractResponse(int exitCode,
            String[] stdout, String[] stderr) {
        SaltReturnResponse response = new SaltReturnResponse();
        for (String out : stdout) {
            response.addOutput(out);
        }
        for (String err : stderr) {
            response.addError(err);
        }
        response.setExitCode(exitCode);
        Mockito.when(returnHandler.extractResponse(Mockito.anyString())).thenReturn(response);
        return this;
    }

    protected SaltApiNodeStepPlugin_ExecuteTest setupDoReturnHostResponseWhenWaitForResponse() throws Exception {
        Mockito.doReturn(HOST_RESPONSE)
                .when(plugin)
                .waitForJidResponse(Mockito.same(pluginContext), Mockito.same(client), Mockito.eq(AUTH_TOKEN),
                        Mockito.eq(OUTPUT_JID), Mockito.eq(PARAM_MINION_NAME));
        return this;
    }

    protected SaltApiNodeStepPlugin_ExecuteTest setupDoReturnJidWhenSubmitJob() throws Exception {
        Mockito.doReturn(OUTPUT_JID)
                .when(plugin)
                .submitJob(Mockito.same(pluginContext), Mockito.same(client), Mockito.eq(AUTH_TOKEN),
                        Mockito.eq(PARAM_MINION_NAME));
        return this;
    }

    protected SaltApiNodeStepPlugin_ExecuteTest setupDoThrowWhenSubmitJob(Throwable t) throws Exception {
        Mockito.doThrow(t)
                .when(plugin)
                .submitJob(Mockito.same(pluginContext), Mockito.same(client), Mockito.eq(AUTH_TOKEN),
                        Mockito.eq(PARAM_MINION_NAME));
        return this;
    }
}
