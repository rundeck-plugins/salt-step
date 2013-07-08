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
