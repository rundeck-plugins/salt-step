package com.salesforce.rundeck.plugin;

import junit.framework.Assert;

import org.apache.http.Header;
import org.apache.http.HttpStatus;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;

public class SaltApiNodeStepPlugin_AuthenticateTest extends AbstractSaltApiNodeStepPluginTest {

    @Test
    public void testAuthenticateWithRedirectResponseCode() throws Exception {
        setupAuthenticationHeadersOnPost(HttpStatus.SC_MOVED_TEMPORARILY);

        Assert.assertEquals(AUTH_TOKEN, plugin.authenticate(client, PARAM_USER, PARAM_PASSWORD));

        assertThatAuthenticationAttemptedSuccessfully();
    }

    @Test
    public void testAuthenticateWithOkResponseCode() throws Exception {
        setupAuthenticationHeadersOnPost(HttpStatus.SC_OK);

        Assert.assertEquals(AUTH_TOKEN, plugin.authenticate(client, PARAM_USER, PARAM_PASSWORD));

        assertThatAuthenticationAttemptedSuccessfully();
    }

    @Test
    public void testAuthenticateFailure() throws Exception {
        setupResponseCode(post, HttpStatus.SC_INTERNAL_SERVER_ERROR);

        Assert.assertNull(plugin.authenticate(client, PARAM_USER, PARAM_PASSWORD));

        assertThatAuthenticationAttemptedSuccessfully();
    }

    protected SaltApiNodeStepPlugin_AuthenticateTest setupAuthenticationHeadersOnPost(int statusCode) throws Exception {
        setupResponse(post, statusCode, null);
        Mockito.when(response.getHeaders(Mockito.eq(SaltApiNodeStepPlugin.SALT_AUTH_TOKEN_HEADER)))
                .thenReturn(new Header[] { new BasicHeader(SaltApiNodeStepPlugin.SALT_AUTH_TOKEN_HEADER, AUTH_TOKEN) });
        return this;
    }

    protected void assertThatAuthenticationAttemptedSuccessfully() throws Exception {
        Assert.assertEquals(PARAM_ENDPOINT + "/login", post.getURI().toString());
        assertPostBody("username=%s&password=%s&eauth=%s", PARAM_USER, PARAM_PASSWORD, PARAM_EAUTH);
        Mockito.verify(client, Mockito.times(1)).execute(Mockito.same(post));
        
        PowerMockito.verifyStatic(Mockito.times(1));
        EntityUtils.consumeQuietly(Mockito.same(responseEntity));
        Mockito.verify(post, Mockito.times(1)).releaseConnection();
    }
}
