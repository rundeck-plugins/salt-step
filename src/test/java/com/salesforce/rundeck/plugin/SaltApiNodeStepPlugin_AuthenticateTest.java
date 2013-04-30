package com.salesforce.rundeck.plugin;

import junit.framework.Assert;

import org.apache.http.HttpException;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;

public class SaltApiNodeStepPlugin_AuthenticateTest extends AbstractSaltApiNodeStepPluginTest {

    @Before
    public void setup() {
        spyPlugin();
    }

    @Test
    public void testAuthenticateWithOkResponseCode() throws Exception {
        setupAuthenticationHeadersOnPost(HttpStatus.SC_OK);

        Assert.assertEquals("Expected authentication to return correct auth token", AUTH_TOKEN,
                plugin.authenticate(latestCapability, client, PARAM_USER, PARAM_PASSWORD));

        assertThatAuthenticationAttemptedSuccessfully();
    }

    @Test
    public void testAuthenticateFailure() throws Exception {
        setupResponseCode(post, HttpStatus.SC_UNAUTHORIZED);

        Assert.assertNull("Expected authentication failure returning null",
                plugin.authenticate(latestCapability, client, PARAM_USER, PARAM_PASSWORD));

        assertThatAuthenticationAttemptedSuccessfully();
    }

    @Test
    public void testAuthenticationFailureOnInternalServerError() throws Exception {
        setupResponseCode(post, HttpStatus.SC_INTERNAL_SERVER_ERROR);
        try {
            plugin.authenticate(latestCapability, client, PARAM_USER, PARAM_PASSWORD);
            Assert.fail("Expected httpexception on internal server error.");
        } catch (HttpException e) {
            // expected
        }

        assertThatAuthenticationAttemptedSuccessfully();
    }
}
