package com.salesforce.rundeck.plugin;

import junit.framework.Assert;

import org.apache.commons.httpclient.HttpStatus;
import org.junit.Test;
import org.mockito.Mockito;

public class SaltApiNodeStepPlugin_ExtractSaltResponseTest extends AbstractSaltApiNodeStepPluginTest {

    protected static final String JOBS_ENDPOINT = String.format("%s/jobs/%s", PARAM_ENDPOINT, OUTPUT_JID);
    protected static final String HOST_JSON_RESPONSE = String.format("{\"return\":[{%s:%s}]}", PARAM_MINION_NAME, HOST_RESPONSE);

    @Test
    public void testExtractOutputForJid() throws Exception {
        setupResponse(getMethod, HttpStatus.SC_OK, HOST_JSON_RESPONSE);

        Assert.assertEquals(HOST_RESPONSE,
                plugin.extractOutputForJid(pluginContext, client, AUTH_TOKEN, OUTPUT_JID, PARAM_MINION_NAME));

        assertThatJobPollAttemptedSuccessfully();
    }

    @Test
    public void testExtractOutputForJidBadResponse() throws Exception {
        setupResponseCode(getMethod, HttpStatus.SC_INTERNAL_SERVER_ERROR);

        Assert.assertNull(plugin.extractOutputForJid(pluginContext, client, AUTH_TOKEN, OUTPUT_JID, PARAM_MINION_NAME));

        assertThatJobPollAttemptedSuccessfully();
    }

    @Test
    public void testExtractOutputForJidHostEmptyResponse() throws Exception {
        String emptyHostResponse = "{\"return\":[{" + PARAM_MINION_NAME + ": \"\"}]}";
        setupResponse(getMethod, HttpStatus.SC_OK, emptyHostResponse);

        Assert.assertEquals("\"\"",
                plugin.extractOutputForJid(pluginContext, client, AUTH_TOKEN, OUTPUT_JID, PARAM_MINION_NAME));

        assertThatJobPollAttemptedSuccessfully();
    }

    @Test
    public void testExtractOutputForJidNoResponse() throws Exception {
        String noResponse = "{\"return\":[{}]}";
        setupResponse(getMethod, HttpStatus.SC_OK, noResponse);

        Assert.assertNull(plugin.extractOutputForJid(pluginContext, client, AUTH_TOKEN, OUTPUT_JID, PARAM_MINION_NAME));

        assertThatJobPollAttemptedSuccessfully();
    }

    @Test
    public void testExtractOutputForJidMultipleResponses() throws Exception {
        String multipleResponse = "{\"return\":[{},{}]}";
        setupResponse(getMethod, HttpStatus.SC_OK, multipleResponse);

        try {
            plugin.extractOutputForJid(pluginContext, client, AUTH_TOKEN, OUTPUT_JID, PARAM_MINION_NAME);
            Assert.fail("Expected exception for multiple responses.");
        } catch (SaltApiException e) {
            // expected
        }

        assertThatJobPollAttemptedSuccessfully();
    }

    protected void assertThatJobPollAttemptedSuccessfully() throws Exception {
        Assert.assertEquals(JOBS_ENDPOINT, getMethod.getURI().toString());
        Mockito.verify(getMethod, Mockito.times(1)).setRequestHeader(SaltApiNodeStepPlugin.SALT_AUTH_TOKEN_HEADER,
                AUTH_TOKEN);
        Mockito.verify(getMethod, Mockito.times(1)).setRequestHeader(SaltApiNodeStepPlugin.REQUEST_ACCEPT_HEADER_NAME,
                SaltApiNodeStepPlugin.JSON_RESPONSE_ACCEPT_TYPE);
        Mockito.verify(client, Mockito.times(1)).executeMethod(Mockito.same(getMethod));
        Mockito.verify(getMethod, Mockito.times(1)).releaseConnection();
    }
}
