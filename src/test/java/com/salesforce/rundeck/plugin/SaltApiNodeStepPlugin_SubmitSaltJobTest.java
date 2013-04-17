package com.salesforce.rundeck.plugin;

import junit.framework.Assert;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class SaltApiNodeStepPlugin_SubmitSaltJobTest extends AbstractSaltApiNodeStepPluginTest {

    protected static final String MINIONS_ENDPOINT = String.format("%s/minions", PARAM_ENDPOINT);
    protected static final String MINION_JSON_RESPONSE = String.format("[{\"return\": {\"jid\": \"%s\", \"minions\": [\"%s\"]}}]", OUTPUT_JID, PARAM_MINION_NAME);
    
    @Before
    public void setup() throws Exception {
        spyPlugin();
    }

    @Test
    public void testSubmitJob() throws Exception {
        setupResponse(postMethod, HttpStatus.SC_ACCEPTED, MINION_JSON_RESPONSE);

        Assert.assertEquals(OUTPUT_JID, plugin.submitJob(pluginContext, client, AUTH_TOKEN, PARAM_MINION_NAME));

        assertThatSubmitSaltJobAttemptedSuccessfully();
    }

    @Test
    public void testSubmitJobWithArgs() throws Exception {
        setupResponse(postMethod, HttpStatus.SC_ACCEPTED, MINION_JSON_RESPONSE);

        String arg1 = "sdf%33&";
        String arg2 = "adsf asdf";
        plugin.function = String.format("%s \"%s\" \"%s\"", PARAM_FUNCTION, arg1, arg2);
        Assert.assertEquals(OUTPUT_JID, plugin.submitJob(pluginContext, client, AUTH_TOKEN, PARAM_MINION_NAME));

        assertThatSubmitSaltJobAttemptedSuccessfully("fun=%s&tgt=%s&arg=%s&arg=%s", PARAM_FUNCTION, PARAM_MINION_NAME,
                arg1, arg2);
    }

    @Test
    public void testSubmitJobResponseCodeError() throws Exception {
        setupResponse(postMethod, HttpStatus.SC_TEMPORARY_REDIRECT, MINION_JSON_RESPONSE);

        try {
            plugin.submitJob(pluginContext, client, AUTH_TOKEN, PARAM_MINION_NAME);
            Assert.fail("Expected http exception due to bad response code.");
        } catch (HttpException e) {
            // expected
        }

        assertThatSubmitSaltJobAttemptedSuccessfully();
    }

    @Test
    public void testSubmitJobNoMinionsMatched() throws Exception {
        String output = "[{\"return\": {}}]";
        setupResponse(postMethod, HttpStatus.SC_ACCEPTED, output);

        try {
            plugin.submitJob(pluginContext, client, AUTH_TOKEN, PARAM_MINION_NAME);
            Assert.fail("Expected targetting mismatch exception.");
        } catch (SaltTargettingMismatchException e) {
            // expected
        }

        assertThatSubmitSaltJobAttemptedSuccessfully();
    }

    @Test
    public void testSubmitJobMinionCountMismatch() throws Exception {
        String output = String.format("[{\"return\": {\"jid\": \"%s\", \"minions\": []}}]", OUTPUT_JID);
        setupResponse(postMethod, HttpStatus.SC_ACCEPTED, output);

        try {
            plugin.submitJob(pluginContext, client, AUTH_TOKEN, PARAM_MINION_NAME);
            Assert.fail("Expected targetting mismatch exception.");
        } catch (SaltTargettingMismatchException e) {
            // expected
        }

        assertThatSubmitSaltJobAttemptedSuccessfully();
    }

    @Test
    public void testSubmitJobMultipleResponses() throws Exception {
        String output = String.format("[{\"return\": {\"jid\": \"%s\", \"minions\": [\"SomeOtherMinion\"]}},{}]", OUTPUT_JID);
        setupResponse(postMethod, HttpStatus.SC_ACCEPTED, output);

        try {
            plugin.submitJob(pluginContext, client, AUTH_TOKEN, PARAM_MINION_NAME);
            Assert.fail("Expected salt-api response exception.");
        } catch (SaltApiException e) {
            // expected
        }

        assertThatSubmitSaltJobAttemptedSuccessfully();
    }

    @Test
    public void testSubmitJobMinionIdMismatch() throws Exception {
        String output = String.format("[{\"return\": {\"jid\": \"%s\", \"minions\": [\"SomeOtherMinion\"]}}]", OUTPUT_JID);
        setupResponse(postMethod, HttpStatus.SC_ACCEPTED, output);

        try {
            plugin.submitJob(pluginContext, client, AUTH_TOKEN, PARAM_MINION_NAME);
            Assert.fail("Expected targetting mismatch exception.");
        } catch (SaltTargettingMismatchException e) {
            // expected
        }

        assertThatSubmitSaltJobAttemptedSuccessfully();
    }

    protected void assertThatSubmitSaltJobAttemptedSuccessfully() throws Exception {
        assertThatSubmitSaltJobAttemptedSuccessfully("fun=%s&tgt=%s", PARAM_FUNCTION, PARAM_MINION_NAME);
    }

    protected void assertThatSubmitSaltJobAttemptedSuccessfully(String template, String... args) throws Exception {
        Assert.assertEquals(MINIONS_ENDPOINT, postMethod.getURI().toString());
        assertPostBody(template, args);
        Mockito.verify(postMethod, Mockito.times(1)).setRequestHeader(SaltApiNodeStepPlugin.SALT_AUTH_TOKEN_HEADER,
                AUTH_TOKEN);
        Mockito.verify(postMethod, Mockito.times(1)).setRequestHeader(SaltApiNodeStepPlugin.REQUEST_ACCEPT_HEADER_NAME,
                SaltApiNodeStepPlugin.JSON_RESPONSE_ACCEPT_TYPE);
        Mockito.verify(client, Mockito.times(1)).executeMethod(Mockito.same(postMethod));
        Mockito.verify(postMethod, Mockito.times(1)).releaseConnection();
    }
}
