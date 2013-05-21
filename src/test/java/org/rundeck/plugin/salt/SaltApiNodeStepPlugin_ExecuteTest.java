/**
 * Copyright (c) 2013, salesforce.com, inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided
 * that the following conditions are met:
 *
 *    Redistributions of source code must retain the above copyright notice, this list of conditions and the
 *    following disclaimer.
 *
 *    Redistributions in binary form must reproduce the above copyright notice, this list of conditions and
 *    the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 *    Neither the name of salesforce.com, inc. nor the names of its contributors may be used to endorse or
 *    promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package org.rundeck.plugin.salt;

import junit.framework.Assert;

import org.apache.http.HttpException;
import org.apache.http.client.HttpClient;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.rundeck.plugin.salt.SaltApiException;
import org.rundeck.plugin.salt.SaltTargettingMismatchException;
import org.rundeck.plugin.salt.SaltApiNodeStepPlugin.SaltApiNodeStepFailureReason;
import org.rundeck.plugin.salt.output.SaltReturnResponse;
import org.rundeck.plugin.salt.output.SaltReturnResponseParseException;
import org.rundeck.plugin.salt.validation.SaltStepValidationException;
import org.rundeck.plugin.salt.version.SaltApiCapability;

import com.dtolabs.rundeck.core.execution.workflow.steps.node.NodeStepException;

public class SaltApiNodeStepPlugin_ExecuteTest extends AbstractSaltApiNodeStepPluginTest {

    @Before
    public void setup() throws Exception {
        spyPlugin();
    }

    @Test
    public void testExecuteWithAuthenticationFailure() {
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
            Assert.assertSame("Expected execute to throw mocked exception", e, ne);
        }
    }

    @Test
    public void testExecuteWithDataContextMissing() {
        dataContext.clear();
        try {
            plugin.executeNodeStep(pluginContext, configuration, node);
            Assert.fail("Expected exception");
        } catch (NodeStepException e) {
            Assert.assertEquals("Expected failure reason to be set based on exception type",
                    SaltApiNodeStepFailureReason.ARGUMENTS_MISSING, e.getFailureReason());
        }
    }

    @Test
    public void testExecuteInvokesWithCorrectCapability() throws Exception {
        setupAuthenticate();
        setupDoReturnJidWhenSubmitJob();
        setupDoReturnHostResponseWhenWaitForResponse();
        setupDoReturnSaltResponseWhenExtractResponse(0, new String[0], new String[0]);

        SaltApiCapability capability = new SaltApiCapability();
        Mockito.when(plugin.getSaltApiCapability()).thenReturn(capability);

        plugin.executeNodeStep(pluginContext, configuration, node);
        Mockito.verify(plugin, Mockito.times(1)).authenticate(Mockito.same(capability), Mockito.any(HttpClient.class),
                Mockito.anyString(), Mockito.anyString());
    }
    
    @Test
    public void testExecuteInvokesLogout() throws Exception {
        setupAuthenticate();
        setupDoReturnJidWhenSubmitJob();
        setupDoReturnHostResponseWhenWaitForResponse();
        setupDoReturnSaltResponseWhenExtractResponse(0, new String[0], new String[0]);

        plugin.executeNodeStep(pluginContext, configuration, node);
        Mockito.verify(plugin, Mockito.times(1)).logoutQuietly(Mockito.any(HttpClient.class),
                Mockito.eq(AUTH_TOKEN));
    }

    @Test
    public void testExecuteMakesLogWrapperAvailable() throws Exception {
        setupAuthenticate();
        setupDoReturnJidWhenSubmitJob();
        setupDoReturnHostResponseWhenWaitForResponse();
        setupDoReturnSaltResponseWhenExtractResponse(0, new String[0], new String[0]);

        plugin.executeNodeStep(pluginContext, configuration, node);

        Mockito.verify(plugin, Mockito.times(1)).setLogWrapper(Mockito.same(pluginLogger));
    }

    @Test
    public void testSetLogWrapper() {
        plugin.setLogWrapper(pluginLogger);
        Assert.assertNotNull(plugin.logWrapper);
        Assert.assertSame(pluginLogger, plugin.logWrapper.getUnderlyingLogger());
    }

    @Test
    public void testExecuteWithSuccessfulExitCode() throws NodeStepException {
        setupAuthenticate();
        doNothingWhenSetupLogger();
        setupDoReturnJidWhenSubmitJob();
        setupDoReturnHostResponseWhenWaitForResponse();

        String output1 = "line 1 of output";
        String output2 = "line 2 of output";
        String error1 = "line 1 of error";
        String error2 = "line 2 of error";
        setupDoReturnSaltResponseWhenExtractResponse(0, new String[] { output1, output2 }, new String[] { error1,
                error2 });

        plugin.executeNodeStep(pluginContext, configuration, node);

        Mockito.verify(returnHandlerRegistry, Mockito.times(1)).getHandlerFor(Mockito.eq(PARAM_FUNCTION),
                Mockito.same(plugin.defaultReturnHandler));
        Mockito.verify(returnHandler, Mockito.times(1)).extractResponse(Mockito.eq(HOST_RESPONSE));

        InOrder ordering = Mockito.inOrder(log);
        ordering.verify(log, Mockito.times(1)).info(Mockito.eq(output1));
        ordering.verify(log, Mockito.times(1)).info(Mockito.eq(output2));
        ordering.verify(log, Mockito.times(1)).error(Mockito.eq(error1));
        ordering.verify(log, Mockito.times(1)).error(Mockito.eq(error2));
    }

    @Test
    public void testExecuteWithUnsuccessfulExitCode() {
        setupAuthenticate();
        doNothingWhenSetupLogger();
        setupDoReturnJidWhenSubmitJob();
        setupDoReturnHostResponseWhenWaitForResponse();

        String output1 = "line 1 of output";
        String output2 = "line 2 of output";
        String error1 = "line 1 of error";
        String error2 = "line 2 of error";
        setupDoReturnSaltResponseWhenExtractResponse(1, new String[] { output1, output2 }, new String[] { error1,
                error2 });

        try {
            plugin.executeNodeStep(pluginContext, configuration, node);
            Assert.fail("Expected failure due to exit code.");
        } catch (NodeStepException e) {
            Assert.assertEquals("Expected failure reason to be set based on exception type",
                    SaltApiNodeStepFailureReason.EXIT_CODE, e.getFailureReason());
        }

        Mockito.verify(returnHandlerRegistry, Mockito.times(1)).getHandlerFor(Mockito.eq(PARAM_FUNCTION),
                Mockito.same(plugin.defaultReturnHandler));
        Mockito.verify(returnHandler, Mockito.times(1)).extractResponse(Mockito.eq(HOST_RESPONSE));

        InOrder ordering = Mockito.inOrder(log);
        ordering.verify(log, Mockito.times(1)).info(Mockito.eq(output1));
        ordering.verify(log, Mockito.times(1)).info(Mockito.eq(output2));
        ordering.verify(log, Mockito.times(1)).error(Mockito.eq(error1));
        ordering.verify(log, Mockito.times(1)).error(Mockito.eq(error2));
    }

    @Test
    public void testExecuteWithSaltResponseParseException() {
        setupAuthenticate();
        setupDoReturnJidWhenSubmitJob();
        setupDoReturnHostResponseWhenWaitForResponse();

        SaltReturnResponseParseException pe = new SaltReturnResponseParseException("message");
        Mockito.when(returnHandler.extractResponse(Mockito.anyString())).thenThrow(pe);

        try {
            plugin.executeNodeStep(pluginContext, configuration, node);
            Assert.fail("Expected failure due to response parse exception.");
        } catch (NodeStepException e) {
            Assert.assertEquals("Expected failure reason to be set based on exception type",
                    SaltApiNodeStepFailureReason.SALT_API_FAILURE, e.getFailureReason());
            Assert.assertSame("Expected parse exception to be set as root cause", pe, e.getCause());
        }

        Mockito.verify(returnHandlerRegistry, Mockito.times(1)).getHandlerFor(Mockito.eq(PARAM_FUNCTION),
                Mockito.same(plugin.defaultReturnHandler));
        Mockito.verify(returnHandler, Mockito.times(1)).extractResponse(Mockito.eq(HOST_RESPONSE));
    }

    @Test
    public void testExecuteWithSaltApiException() {
        setupAuthenticate();
        setupDoThrowWhenSubmitJob(new SaltApiException("Some message"));

        try {
            plugin.executeNodeStep(pluginContext, configuration, node);
            Assert.fail("Expected node step failure.");
        } catch (NodeStepException e) {
            Assert.assertEquals("Expected failure reason to be set based on exception type",
                    SaltApiNodeStepFailureReason.SALT_API_FAILURE, e.getFailureReason());
        }
    }

    @Test
    public void testExecuteWithSaltTargettingException() {
        setupAuthenticate();
        setupDoThrowWhenSubmitJob(new SaltTargettingMismatchException("Some message"));

        try {
            plugin.executeNodeStep(pluginContext, configuration, node);
            Assert.fail("Expected node step failure.");
        } catch (NodeStepException e) {
            Assert.assertEquals("Expected failure reason to be set based on exception type",
                    SaltApiNodeStepFailureReason.SALT_TARGET_MISMATCH, e.getFailureReason());
        }
    }

    @Test
    public void testExecuteWithHttpException() {
        setupAuthenticate();
        setupDoThrowWhenSubmitJob(new HttpException("Some message"));

        try {
            plugin.executeNodeStep(pluginContext, configuration, node);
            Assert.fail("Expected node step failure.");
        } catch (NodeStepException e) {
            Assert.assertEquals("Expected failure reason to be set based on exception type",
                    SaltApiNodeStepFailureReason.COMMUNICATION_FAILURE, e.getFailureReason());
        }
    }

    @Test
    public void testExecuteWithInterruptedException() throws Exception {
        setupAuthenticate();
        setupDoReturnJidWhenSubmitJob();

        Mockito.doThrow(new InterruptedException())
                .when(plugin)
                .waitForJidResponse(Mockito.same(client), Mockito.eq(AUTH_TOKEN), Mockito.eq(OUTPUT_JID),
                        Mockito.eq(PARAM_MINION_NAME));

        try {
            plugin.executeNodeStep(pluginContext, configuration, node);
            Assert.fail("Expected node step failure.");
        } catch (NodeStepException e) {
            Assert.assertEquals("Expected failure reason to be set based on exception type",
                    SaltApiNodeStepFailureReason.INTERRUPTED, e.getFailureReason());
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

    protected SaltApiNodeStepPlugin_ExecuteTest setupDoReturnHostResponseWhenWaitForResponse() {
        try {
            Mockito.doReturn(HOST_RESPONSE)
                    .when(plugin)
                    .waitForJidResponse(Mockito.same(client), Mockito.eq(AUTH_TOKEN), Mockito.eq(OUTPUT_JID),
                            Mockito.eq(PARAM_MINION_NAME));
            return this;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected SaltApiNodeStepPlugin_ExecuteTest setupDoReturnJidWhenSubmitJob() {
        try {
            Mockito.doReturn(OUTPUT_JID).when(plugin)
                    .submitJob(Mockito.same(client), Mockito.eq(AUTH_TOKEN), Mockito.eq(PARAM_MINION_NAME));
            return this;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected SaltApiNodeStepPlugin_ExecuteTest setupDoThrowWhenSubmitJob(Throwable t) {
        try {
            Mockito.doThrow(t).when(plugin)
                    .submitJob(Mockito.same(client), Mockito.eq(AUTH_TOKEN), Mockito.eq(PARAM_MINION_NAME));
            return this;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected SaltApiNodeStepPlugin_ExecuteTest doNothingWhenSetupLogger() {
        Mockito.doNothing().when(plugin).setLogWrapper(Mockito.same(pluginLogger));
        return this;
    }
}