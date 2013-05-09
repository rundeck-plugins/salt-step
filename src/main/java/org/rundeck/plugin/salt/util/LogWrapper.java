package org.rundeck.plugin.salt.util;

import com.dtolabs.rundeck.core.Constants;
import com.dtolabs.rundeck.plugins.PluginLogger;
import com.google.common.annotations.VisibleForTesting;

/**
 * Abstracts away the PluginLogger.
 */
public class LogWrapper {

    protected final PluginLogger logger;

    public LogWrapper(PluginLogger logger) {
        this.logger = logger;
    }

    public void info(String string, Object... args) {
        logger.log(Constants.INFO_LEVEL, String.format(string, args));
    }

    public void debug(String string, Object... args) {
        logger.log(Constants.DEBUG_LEVEL, String.format(string, args));
    }

    public void warn(String string, Object... args) {
        logger.log(Constants.WARN_LEVEL, String.format(string, args));
    }

    public void error(String string, Object... args) {
        logger.log(Constants.ERR_LEVEL, String.format(string, args));
    }

    @VisibleForTesting
    public PluginLogger getUnderlyingLogger() {
        return logger;
    }
}
