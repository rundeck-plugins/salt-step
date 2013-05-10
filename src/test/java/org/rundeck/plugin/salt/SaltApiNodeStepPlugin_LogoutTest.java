package org.rundeck.plugin.salt;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class SaltApiNodeStepPlugin_LogoutTest extends AbstractSaltApiNodeStepPluginTest {

    @Test
    public void testLogout() throws Exception {
        plugin.logoutQuietly(client, AUTH_TOKEN);

        Assert.assertEquals("Expected correct logout endpoint to be used", PARAM_ENDPOINT + "/logout", get.getURI()
                .toString());
        Mockito.verify(get, Mockito.times(1)).setHeader(Mockito.eq(SaltApiNodeStepPlugin.SALT_AUTH_TOKEN_HEADER),
                Mockito.eq(AUTH_TOKEN));
        Mockito.verify(retryingExecutor, Mockito.times(1)).execute(Mockito.same(log), Mockito.same(client),
                Mockito.same(get), Mockito.eq(plugin.numRetries));
    }

    @Test
    public void testLogoutThrowsIOExceptionRemainsQuiet() throws Exception {
        setupDoThrowOnRetryingExecutor(new IOException());

        plugin.logoutQuietly(client, AUTH_TOKEN);
    }

    @Test
    public void testLogoutThrowsInterruptedExceptionRemainsQuiet() throws Exception {
        setupDoThrowOnRetryingExecutor(new InterruptedException());

        plugin.logoutQuietly(client, AUTH_TOKEN);

        // Check needs to remain in place to clear interrupted flag.
        Assert.assertTrue("Expected interrupted flag to be set.", Thread.interrupted());
    }

    protected void setupDoThrowOnRetryingExecutor(Exception e) throws Exception {
        Mockito.doThrow(e).when(retryingExecutor)
                .execute(Mockito.same(log), Mockito.same(client), Mockito.same(get), Mockito.eq(plugin.numRetries));
    }
}
