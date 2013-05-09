package org.rundeck.plugin.salt;

import junit.framework.Assert;

import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;

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
}
