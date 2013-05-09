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
