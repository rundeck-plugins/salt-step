package com.salesforce.rundeck.plugin;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.LogOutputStream;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.lang.StringUtils;

import com.dtolabs.rundeck.core.Constants;
import com.dtolabs.rundeck.core.common.INodeEntry;
import com.dtolabs.rundeck.core.execution.workflow.steps.FailureReason;
import com.dtolabs.rundeck.core.execution.workflow.steps.node.NodeStepException;
import com.dtolabs.rundeck.core.plugins.Plugin;
import com.dtolabs.rundeck.plugins.ServiceNameConstants;
import com.dtolabs.rundeck.plugins.descriptions.PluginDescription;
import com.dtolabs.rundeck.plugins.descriptions.PluginProperty;
import com.dtolabs.rundeck.plugins.step.NodeStepPlugin;
import com.dtolabs.rundeck.plugins.step.PluginStepContext;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * This plugin allows salt execution on a specific minion by exec'ing on a local
 * master.
 * 
 * Pre-requisites:
 * <ul>
 * <li>Project node resources must be configured with the name as the salt
 * minion's name as configured on the salt master.</li>
 * </ul>
 */
@Plugin(name = SaltLocalNodeStepPlugin.SERVICE_PROVIDER_NAME, service = ServiceNameConstants.WorkflowNodeStep)
@PluginDescription(title = "Local Salt Execution", description = "Run a command on a local salt master.")
public class SaltLocalNodeStepPlugin implements NodeStepPlugin {
    public enum SaltLocalNodeStepFailureReason implements FailureReason {
        FAILED, EXIT_CODE, INTERRUPTED, SALT_TARGET_MISMATCH
    }

    protected static class ExecutorFactory {
        public Executor createExecutor() {
            return new DefaultExecutor();
        }
    }

    public static final String SERVICE_PROVIDER_NAME = "salt-local-exec";

    protected static final String OUTPUT_MODE = "--out";
    protected static final String JSON_OUTPUT = "json";
    protected final StringBuilder execOutput = new StringBuilder();

    protected static final Type STRING_MAP_TYPE = new TypeToken<Map<String, Object>>() {}.getType();

    protected ExecutorFactory executorFactory = new ExecutorFactory();

    @PluginProperty(title = "SALT_EXECUTABLE", description = "Salt executable location", required = true, defaultValue = "salt")
    protected String saltExecutable;

    @PluginProperty(title = "Function", description = "Function (including args) to invoke on salt minions", required = true)
    protected String function;

    @PluginProperty(title = "SALT_HOME", description = "Salt home directory (if not default)", required = false)
    protected String saltHome;

    @Override
    public void executeNodeStep(final PluginStepContext context, Map<String, Object> configuration,
            final INodeEntry entry) throws NodeStepException {
        Executor executor = executorFactory.createExecutor();
        DefaultExecuteResultHandler handler = new DefaultExecuteResultHandler();

        LogOutputStream os = new LogOutputStream() {
            @Override
            protected void processLine(String line, int level) {
                appendOutput(line);
            }
        };
        executor.setStreamHandler(new PumpStreamHandler(os));

        CommandLine commandLine = new CommandLine(saltExecutable);
        commandLine.addArgument(OUTPUT_MODE).addArgument(JSON_OUTPUT);
        if (StringUtils.isNotEmpty(saltHome)) {
            commandLine.addArgument("-c").addArgument(saltHome);
        }
        commandLine.addArgument(entry.getNodename());
        List<String> args = ArgumentSplitterUtil.split(function);
        for (String arg : args) {
            commandLine.addArgument(arg);
        }
        context.getLogger().log(Constants.INFO_LEVEL, "Preparing to exec: " + commandLine.toString());
        try {
            executor.execute(commandLine, handler);
            handler.waitFor();

            Gson gson = new Gson();
            String result = getOutput();
            if (StringUtils.isEmpty(result)) {
                throw new NodeStepException("No targets matched.", SaltLocalNodeStepFailureReason.SALT_TARGET_MISMATCH,
                        entry.getNodename());
            }
            Map<String, Object> responseObject = gson.fromJson(result, STRING_MAP_TYPE);
            String hostResult = responseObject.get(entry.getNodename()).toString();
            context.getLogger().log(Constants.INFO_LEVEL, hostResult);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new NodeStepException("Interrupted.", SaltLocalNodeStepFailureReason.INTERRUPTED, entry.getNodename());
        } catch (IOException e) {
            throw new NodeStepException(e.getMessage(), SaltLocalNodeStepFailureReason.FAILED, entry.getNodename());
        }

        if (handler.getExitValue() != 0) {
            String failureString = String.format("Bad exit code(%d)", handler.getExitValue());
            if (handler.getException() != null) {
                failureString += " " + handler.getException().getMessage();
            }
            throw new NodeStepException(failureString, SaltLocalNodeStepFailureReason.EXIT_CODE, entry.getNodename());
        }
    }

    protected void appendOutput(String line) {
        execOutput.append(line);
    }

    protected String getOutput() {
        return execOutput.toString();
    }
}
