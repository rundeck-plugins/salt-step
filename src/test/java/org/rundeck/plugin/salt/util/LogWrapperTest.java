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
