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

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.rundeck.plugin.salt.util.LogWrapper;

import com.dtolabs.rundeck.core.Constants;
import com.dtolabs.rundeck.plugins.PluginLogger;
import com.google.common.base.Function;

public class LogWrapperTest {

    protected LogWrapper log;
    protected PluginLogger underlyingLogger;

    @Before
    public void setup() {
        underlyingLogger = Mockito.mock(PluginLogger.class);
        log = new LogWrapper(underlyingLogger);
    }

    @Test
    public void testDebug() {
        assertLineLoggedAt(new Function<String[], Void>() {
            @Override
            public Void apply(String[] input) {
                log.debug(input[0], input[1]);
                return null;
            }
        }, Constants.DEBUG_LEVEL);
    }
    
    @Test
    public void testInfo() {
        assertLineLoggedAt(new Function<String[], Void>() {
            @Override
            public Void apply(String[] input) {
                log.info(input[0], input[1]);
                return null;
            }
        }, Constants.INFO_LEVEL);
    }
    
    @Test
    public void testWarn() {
        assertLineLoggedAt(new Function<String[], Void>() {
            @Override
            public Void apply(String[] input) {
                log.warn(input[0], input[1]);
                return null;
            }
        }, Constants.WARN_LEVEL);
    }
    
    @Test
    public void testError() {
        assertLineLoggedAt(new Function<String[], Void>() {
            @Override
            public Void apply(String[] input) {
                log.error(input[0], input[1]);
                return null;
            }
        }, Constants.ERR_LEVEL);
    }

    protected void assertLineLoggedAt(Function<String[], Void> logFunction, int logLevel) {
        String logLine = "some log line %s";
        String args = "arg!";
        String formattedLine = String.format(logLine, args);
        logFunction.apply(new String[] { logLine, args });
        Mockito.verify(underlyingLogger, Mockito.times(1)).log(Mockito.eq(logLevel), Mockito.eq(formattedLine));
    }
}
