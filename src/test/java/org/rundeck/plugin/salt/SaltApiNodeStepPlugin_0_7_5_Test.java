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
import org.apache.http.client.HttpClient;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.rundeck.plugin.salt.output.SaltReturnResponse;

public class SaltApiNodeStepPlugin_0_7_5_Test extends AbstractSaltApiNodeStepPlugin_BackwardsCompatabilityTest {

    @Override
    protected String getVersion() {
        return "0.7.5";
    }

    @Before
    public void setup() {
        spyPlugin();
    }

    @Test
    public void testAuthenticateWithRedirectResponseCode() throws Exception {
        setupAuthenticationHeadersOnPost(HttpStatus.SC_MOVED_TEMPORARILY);

        Assert.assertEquals("Expected authentication to return correct auth token", AUTH_TOKEN,
                plugin.authenticate(legacyCapability, client, PARAM_USER, PARAM_PASSWORD));

        assertThatAuthenticationAttemptedSuccessfully(legacyCapability);
    }

    @Test
    public void testAuthenticateFailure() throws Exception {
        setupResponseCode(post, HttpStatus.SC_INTERNAL_SERVER_ERROR);

        Assert.assertNull("Expected authentication failure returning null",
                plugin.authenticate(legacyCapability, client, PARAM_USER, PARAM_PASSWORD));

        assertThatAuthenticationAttemptedSuccessfully(legacyCapability);
    }
    
    @Test
    public void testDoesNotAttemptToLogout() throws Exception {
        setupAuthenticate();
        Mockito.doReturn("foo").when(plugin).submitJob(Mockito.any(HttpClient.class), Mockito.anyString(), Mockito.anyString());
        Mockito.doReturn("foo").when(plugin).waitForJidResponse(Mockito.any(HttpClient.class), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
        SaltReturnResponse response = new SaltReturnResponse();
        response.setExitCode(0);
        Mockito.doReturn(response).when(returnHandler).extractResponse(Mockito.anyString());

        plugin.executeNodeStep(pluginContext, configuration, node);
        Mockito.verify(plugin, Mockito.never()).logoutQuietly(Mockito.any(HttpClient.class), Mockito.anyString());
    }
}
