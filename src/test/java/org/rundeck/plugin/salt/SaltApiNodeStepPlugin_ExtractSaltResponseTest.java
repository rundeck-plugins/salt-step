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

import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.rundeck.plugin.salt.SaltApiException;
import org.rundeck.plugin.salt.SaltApiNodeStepPlugin;

public class SaltApiNodeStepPlugin_ExtractSaltResponseTest extends AbstractSaltApiNodeStepPluginTest {

    protected static final String JOBS_ENDPOINT = String.format("%s/jobs/%s", PARAM_ENDPOINT, OUTPUT_JID);
    protected static final String HOST_JSON_RESPONSE = String.format("{\"return\":[{%s:%s}]}", PARAM_MINION_NAME,
            HOST_RESPONSE);

    @Before
    public void setup() throws Exception {
        spyPlugin();
    }

    @Test
    public void testExtractOutputForJid() throws Exception {
        setupResponse(get, HttpStatus.SC_OK, HOST_JSON_RESPONSE);

        Assert.assertEquals("Expected host response to be parsed out from json response", HOST_RESPONSE,
                plugin.extractOutputForJid(client, AUTH_TOKEN, OUTPUT_JID, PARAM_MINION_NAME));

        assertThatJobPollAttemptedSuccessfully();
    }

    @Test
    public void testExtractOutputForJidBadResponse() throws Exception {
        setupResponseCode(get, HttpStatus.SC_INTERNAL_SERVER_ERROR);

        Assert.assertNull("Expected null response due to internal server error",
                plugin.extractOutputForJid(client, AUTH_TOKEN, OUTPUT_JID, PARAM_MINION_NAME));

        assertThatJobPollAttemptedSuccessfully();
    }

    @Test
    public void testExtractOutputForJidHostEmptyResponse() throws Exception {
        String emptyHostResponse = "{\"return\":[{" + PARAM_MINION_NAME + ": \"\"}]}";
        setupResponse(get, HttpStatus.SC_OK, emptyHostResponse);

        Assert.assertEquals("Expected empty response for empty minion response", "\"\"",
                plugin.extractOutputForJid(client, AUTH_TOKEN, OUTPUT_JID, PARAM_MINION_NAME));

        assertThatJobPollAttemptedSuccessfully();
    }

    @Test
    public void testExtractOutputForJidNoResponse() throws Exception {
        String noResponse = "{\"return\":[{}]}";
        setupResponse(get, HttpStatus.SC_OK, noResponse);

        Assert.assertNull("Expected no response for no minion response",
                plugin.extractOutputForJid(client, AUTH_TOKEN, OUTPUT_JID, PARAM_MINION_NAME));

        assertThatJobPollAttemptedSuccessfully();
    }

    @Test
    public void testExtractOutputForJidMultipleResponses() throws Exception {
        String multipleResponse = "{\"return\":[{},{}]}";
        setupResponse(get, HttpStatus.SC_OK, multipleResponse);

        try {
            plugin.extractOutputForJid(client, AUTH_TOKEN, OUTPUT_JID, PARAM_MINION_NAME);
            Assert.fail("Expected exception for multiple responses.");
        } catch (SaltApiException e) {
            // expected
        }

        assertThatJobPollAttemptedSuccessfully();
    }

    protected void assertThatJobPollAttemptedSuccessfully() {
        try {
            Assert.assertEquals("Expected correct polling endpoint to be used", JOBS_ENDPOINT, get.getURI().toString());
            Mockito.verify(get, Mockito.times(1)).setHeader(SaltApiNodeStepPlugin.SALT_AUTH_TOKEN_HEADER, AUTH_TOKEN);
            Mockito.verify(get, Mockito.times(1)).setHeader(SaltApiNodeStepPlugin.REQUEST_ACCEPT_HEADER_NAME,
                    SaltApiNodeStepPlugin.JSON_RESPONSE_ACCEPT_TYPE);
            Mockito.verify(retryingExecutor, Mockito.times(1)).execute(Mockito.same(log), Mockito.same(client),
                    Mockito.same(get), Mockito.eq(plugin.numRetries));
            Mockito.verifyZeroInteractions(client);

            Mockito.verify(get, Mockito.times(1)).releaseConnection();
            Mockito.verify(plugin, Mockito.times(1)).closeResource(Mockito.same(responseEntity));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
