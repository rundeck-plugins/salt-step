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

import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class SaltApiNodeStepPlugin_PollSaltResponseTest extends AbstractSaltApiNodeStepPluginTest {

    @Before
    public void setup() throws Exception {
        spyPlugin();
    }

    @Test
    public void testWaitForJidResponse() throws Exception {
        // Workaround for mockito spy stubbing and vararg returns.
        final AtomicInteger counter = new AtomicInteger(2);
        Mockito.doAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                if (counter.decrementAndGet() == 0) {
                    return HOST_RESPONSE;
                }
                return null;
            }
        })
                .when(plugin)
                .extractOutputForJid(Mockito.same(client), Mockito.eq(AUTH_TOKEN), Mockito.eq(OUTPUT_JID),
                        Mockito.eq(PARAM_MINION_NAME));

        Assert.assertEquals("Expected mocked host response to be returned", HOST_RESPONSE,
                plugin.waitForJidResponse(client, AUTH_TOKEN, OUTPUT_JID, PARAM_MINION_NAME));

        Mockito.verify(plugin, Mockito.times(2)).extractOutputForJid(Mockito.same(client), Mockito.eq(AUTH_TOKEN),
                Mockito.eq(OUTPUT_JID), Mockito.eq(PARAM_MINION_NAME));
        Mockito.verify(timer, Mockito.times(1)).waitForNext();
    }

    @Test
    public void testWaitForJidResponseInterrupted() throws Exception {
        Mockito.doReturn(null)
                .when(plugin)
                .extractOutputForJid(Mockito.same(client), Mockito.eq(AUTH_TOKEN), Mockito.eq(OUTPUT_JID),
                        Mockito.eq(PARAM_MINION_NAME));

        Mockito.doThrow(new InterruptedException()).when(timer).waitForNext();
        try {
            Assert.assertNull("Expected no response due to thread interruption",
                    plugin.waitForJidResponse(client, AUTH_TOKEN, OUTPUT_JID, PARAM_MINION_NAME));
            Assert.fail("Expected to be interrupted.");
        } catch (InterruptedException e) {
            // expected
        }

        Mockito.verify(plugin, Mockito.times(1)).extractOutputForJid(Mockito.same(client), Mockito.eq(AUTH_TOKEN),
                Mockito.eq(OUTPUT_JID), Mockito.eq(PARAM_MINION_NAME));
    }
}
