package com.salesforce.rundeck.plugin;

import junit.framework.Assert;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpStatus;
import org.junit.Test;
import org.mockito.Mockito;

public class SaltApiNodeStepPlugin_AuthenticateTest extends AbstractSaltApiNodeStepPluginTest {

    @Test
    public void testAuthenticateWithRedirectResponseCode() throws Exception {
        setupAuthenticationHeadersOnPost().setupResponseCode(postMethod, HttpStatus.SC_MOVED_TEMPORARILY);

        Assert.assertEquals(AUTH_TOKEN, plugin.authenticate(client, PARAM_USER, PARAM_PASSWORD));

        assertThatAuthenticationAttemptedSuccessfully();
    }

    @Test
    public void testAuthenticateWithOkResponseCode() throws Exception {
        setupAuthenticationHeadersOnPost().setupResponseCode(postMethod, HttpStatus.SC_OK);

        Assert.assertEquals(AUTH_TOKEN, plugin.authenticate(client, PARAM_USER, PARAM_PASSWORD));

        assertThatAuthenticationAttemptedSuccessfully();
    }

    @Test
    public void testAuthenticateFailure() throws Exception {
        setupResponseCode(postMethod, HttpStatus.SC_INTERNAL_SERVER_ERROR);

        Assert.assertNull(plugin.authenticate(client, PARAM_USER, PARAM_PASSWORD));

        assertThatAuthenticationAttemptedSuccessfully();
    }

    protected SaltApiNodeStepPlugin_AuthenticateTest setupAuthenticationHeadersOnPost() {
        Mockito.when(postMethod.getResponseHeader(SaltApiNodeStepPlugin.SALT_AUTH_TOKEN_HEADER)).thenReturn(
                new Header(SaltApiNodeStepPlugin.SALT_AUTH_TOKEN_HEADER, AUTH_TOKEN));
        return this;
    }

    protected void assertThatAuthenticationAttemptedSuccessfully() throws Exception {
        Assert.assertEquals(PARAM_ENDPOINT + "/login", postMethod.getURI().toString());
        assertPostBody("username=%s&password=%s&eauth=%s", PARAM_USER, PARAM_PASSWORD, PARAM_EAUTH);
        Mockito.verify(client, Mockito.times(1)).executeMethod(Mockito.same(postMethod));
        Mockito.verify(postMethod, Mockito.times(1)).releaseConnection();
    }
}
