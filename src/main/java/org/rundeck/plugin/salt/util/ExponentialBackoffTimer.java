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

import org.springframework.stereotype.Component;

/**
 * Utility timer to assist with exponential backoffs
 */
public class ExponentialBackoffTimer {

    @Component
    public static class Factory {
        public ExponentialBackoffTimer newTimer(long delayStep, long maximumDelay) {
            return new ExponentialBackoffTimer(delayStep, maximumDelay);
        }
    }

    protected final long delayStep;
    protected final long maximumDelay;

    protected int count = 2;
    protected long nextSleepAmount;

    /**
     * Creates a backoff timer using the given delayStep and maximumDelay
     * 
     * @param delayStep A multiplier value (in ms) for the amount to sleep for.
     * @param maximumDelay The maximum amount for the sleep value (in ms)
     */
    public ExponentialBackoffTimer(long delayStep, long maximumDelay) {
        this.delayStep = delayStep;
        this.maximumDelay = maximumDelay;
        this.nextSleepAmount = delayStep;
    }

    /**
     * Calls thread.sleep for an appropriate length of time depending on how many 
     * times this method has already been invoked.
     * 
     *  Uses the default E(c) = (2^x-1)/2 formula.
     *  
     *  @throws InterruptedException if the thread is interrupted.
     */
    public void waitForNext() throws InterruptedException {
        sleep(nextSleepAmount);
        if (nextSleepAmount < maximumDelay) {
            nextSleepAmount = (long) ((Math.pow(2, ++count) - 1) / 2D * delayStep);
        }
        nextSleepAmount = Math.min(maximumDelay, nextSleepAmount);
        if (Thread.interrupted()) {
            Thread.currentThread().interrupt();
            throw new InterruptedException();
        }
    }

    protected void sleep(long l) throws InterruptedException {
        Thread.sleep(l);
    }
}
