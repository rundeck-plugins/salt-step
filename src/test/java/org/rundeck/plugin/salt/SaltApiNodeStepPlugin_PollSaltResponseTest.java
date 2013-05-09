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
