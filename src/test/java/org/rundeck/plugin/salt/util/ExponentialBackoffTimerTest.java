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

package org.rundeck.plugin.salt.util;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.rundeck.plugin.salt.util.ExponentialBackoffTimer;
import org.rundeck.plugin.salt.util.ExponentialBackoffTimer.Factory;

import com.google.common.collect.Lists;

public class ExponentialBackoffTimerTest {

    @Test
    public void testInitialize() {
        long delayStep = 1;
        long maximumDelay = 30;
        ExponentialBackoffTimer timer = new ExponentialBackoffTimer(delayStep, maximumDelay);

        Assert.assertEquals("Timer's delay step should be initialized to constructor argument.", delayStep,
                timer.delayStep);
        Assert.assertEquals("Timer's maximum delay should be initialized to constructor argument.", maximumDelay,
                timer.maximumDelay);
    }
    
    @Test
    public void testInitializeUsingFactory() {
        long delayStep = 1;
        long maximumDelay = 30;
        ExponentialBackoffTimer timer = new Factory().newTimer(delayStep, maximumDelay);

        Assert.assertEquals("Timer's delay step should be initialized to constructor argument.", delayStep,
                timer.delayStep);
        Assert.assertEquals("Timer's maximum delay should be initialized to constructor argument.", maximumDelay,
                timer.maximumDelay);
    }

    @Test
    public void testWaitForNext() throws Exception {
        long delayStep = 1;
        long maximumDelay = 300;
        List<Long> expectedBackoff = Lists.newArrayList(1L, 3L, 7L, 15L, 31L, 63L, 127L, 255L, 300L, 300L);
        ExponentialBackoffTimer timer = new ExponentialBackoffTimer(delayStep, maximumDelay);
        timer = Mockito.spy(timer);
        Mockito.doNothing().when(timer).sleep(Mockito.anyLong());

        for (int i = 0; i < 10; i++) {
            timer.waitForNext();
        }

        ArgumentCaptor<Long> captor = ArgumentCaptor.forClass(Long.class);
        Mockito.verify(timer, Mockito.times(10)).sleep(captor.capture());
        List<Long> sleepValues = captor.getAllValues();
        Assert.assertEquals("Sleep values don't match expected exponential backoff values.", expectedBackoff,
                sleepValues);
    }

    @Test
    public void testInterrupted() throws Exception {
        ExponentialBackoffTimer timer = new ExponentialBackoffTimer(1, 300);
        timer = Mockito.spy(timer);
        Mockito.doNothing().doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                Thread.currentThread().interrupt();
                return null;
            }
        }).when(timer).sleep(Mockito.anyLong());

        try {
            for (int i = 0; i < 10; i++) {
                timer.waitForNext();
            }
            Assert.fail("Expected interrupted exception");
        } catch (InterruptedException e) {
            // expected
        }

        Mockito.verify(timer, Mockito.times(2)).sleep(Mockito.anyLong());
    }
}
