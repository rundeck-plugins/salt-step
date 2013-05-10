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
