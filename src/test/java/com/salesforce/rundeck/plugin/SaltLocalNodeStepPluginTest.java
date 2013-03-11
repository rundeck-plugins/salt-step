package com.salesforce.rundeck.plugin;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.Executor;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.dtolabs.rundeck.core.Constants;
import com.dtolabs.rundeck.core.common.INodeEntry;
import com.dtolabs.rundeck.core.execution.workflow.steps.node.NodeStepException;
import com.dtolabs.rundeck.plugins.PluginLogger;
import com.dtolabs.rundeck.plugins.step.PluginStepContext;
import com.salesforce.rundeck.plugin.SaltLocalNodeStepPlugin.ExecutorFactory;
import com.salesforce.rundeck.plugin.SaltLocalNodeStepPlugin.SaltLocalNodeStepFailureReason;

public class SaltLocalNodeStepPluginTest {
    protected static final String PARAM_SALT = "salt";
    protected static final String PARAM_MINION_NAME = "minion";
    protected static final String PARAM_FUNCTION = "some.function";

    protected static final String HOST_RESPONSE = "some response";
    protected static final String JSON_RESPONSE = "{" + PARAM_MINION_NAME + ":\"" + HOST_RESPONSE + "\"}";

    protected SaltLocalNodeStepPlugin plugin;
    protected Executor executor;
    protected PluginStepContext pluginContext;
    protected PluginLogger logger;
    protected INodeEntry node;
    protected Map<String, Object> configuration;

    @Before
    public void setup() {
        plugin = new SaltLocalNodeStepPlugin();
        plugin.saltExecutable = PARAM_SALT;
        plugin.function = PARAM_FUNCTION;
        executor = Mockito.mock(Executor.class);
        plugin.executorFactory = new ExecutorFactory() {
            public Executor createExecutor() {
                return executor;
            }
        };
        
        plugin = Mockito.spy(plugin);
        Mockito.when(plugin.getOutput()).thenReturn(JSON_RESPONSE);

        pluginContext = Mockito.mock(PluginStepContext.class);
        logger = Mockito.mock(PluginLogger.class);
        Mockito.when(pluginContext.getLogger()).thenReturn(logger);
        node = Mockito.mock(INodeEntry.class);
        Mockito.when(node.getNodename()).thenReturn(PARAM_MINION_NAME);
        configuration = new HashMap<String, Object>();
    }

    @Test
    public void testExecute() throws Exception {
        setExecutorToAnswerWith(0);

        plugin.executeNodeStep(pluginContext, configuration, node);

        ArgumentCaptor<CommandLine> captor = ArgumentCaptor.forClass(CommandLine.class);
        Mockito.verify(executor, Mockito.times(1)).execute(captor.capture(),
                Mockito.any(DefaultExecuteResultHandler.class));
        CommandLine commandLine = captor.getValue();
        Assert.assertEquals(PARAM_SALT, commandLine.getExecutable());
        String[] args = commandLine.getArguments();
        Assert.assertEquals(4, args.length);
        Assert.assertEquals(SaltLocalNodeStepPlugin.OUTPUT_MODE, args[0]);
        Assert.assertEquals(SaltLocalNodeStepPlugin.JSON_OUTPUT, args[1]);
        Assert.assertEquals(PARAM_MINION_NAME, args[2]);
        Assert.assertEquals(PARAM_FUNCTION, args[3]);

        Mockito.verify(logger, Mockito.times(1)).log(Mockito.eq(Constants.INFO_LEVEL), Mockito.eq(HOST_RESPONSE));
    }
    
    @Test
    public void testExecuteWithArgs() throws Exception {
        setExecutorToAnswerWith(0);
        
        String arg0 = "\"some really long arg\"";
        String arg1 = "second";
        plugin.function = PARAM_FUNCTION + " " + arg0 + " " + arg1;

        plugin.executeNodeStep(pluginContext, configuration, node);

        ArgumentCaptor<CommandLine> captor = ArgumentCaptor.forClass(CommandLine.class);
        Mockito.verify(executor, Mockito.times(1)).execute(captor.capture(),
                Mockito.any(DefaultExecuteResultHandler.class));
        CommandLine commandLine = captor.getValue();
        Assert.assertEquals(PARAM_SALT, commandLine.getExecutable());
        String[] args = commandLine.getArguments();
        Assert.assertEquals(6, args.length);
        Assert.assertEquals(SaltLocalNodeStepPlugin.OUTPUT_MODE, args[0]);
        Assert.assertEquals(SaltLocalNodeStepPlugin.JSON_OUTPUT, args[1]);
        Assert.assertEquals(PARAM_MINION_NAME, args[2]);
        Assert.assertEquals(PARAM_FUNCTION, args[3]);
        Assert.assertEquals(arg0, args[4]);
        Assert.assertEquals(arg1, args[5]);

        Mockito.verify(logger, Mockito.times(1)).log(Mockito.eq(Constants.INFO_LEVEL), Mockito.eq(HOST_RESPONSE));
    }

    @Test
    public void testExecuteWithSaltHome() throws Exception {
        String saltHome = "salt/home/location";
        plugin.saltHome = saltHome;
        setExecutorToAnswerWith(0);

        plugin.executeNodeStep(pluginContext, configuration, node);

        ArgumentCaptor<CommandLine> captor = ArgumentCaptor.forClass(CommandLine.class);
        Mockito.verify(executor, Mockito.times(1)).execute(captor.capture(),
                Mockito.any(DefaultExecuteResultHandler.class));
        CommandLine commandLine = captor.getValue();
        Assert.assertEquals(PARAM_SALT, commandLine.getExecutable());
        String[] args = commandLine.getArguments();
        Assert.assertEquals(6, args.length);
        Assert.assertEquals(SaltLocalNodeStepPlugin.OUTPUT_MODE, args[0]);
        Assert.assertEquals(SaltLocalNodeStepPlugin.JSON_OUTPUT, args[1]);
        Assert.assertEquals("-c", args[2]);
        Assert.assertEquals(saltHome, args[3]);
        Assert.assertEquals(PARAM_MINION_NAME, args[4]);
        Assert.assertEquals(PARAM_FUNCTION, args[5]);

        Mockito.verify(logger, Mockito.times(1)).log(Mockito.eq(Constants.INFO_LEVEL), Mockito.eq(HOST_RESPONSE));
    }

    @Test
    public void testAppendAndGetOutput() {
        plugin = new SaltLocalNodeStepPlugin();
        String data = "abc";
        String moreData = "bcd";
        plugin.appendOutput(data);
        plugin.appendOutput(moreData);
        Assert.assertEquals(data + moreData, plugin.getOutput());
    }

    @Test
    public void testExecuteNoTargetsMatched() throws Exception {
        setExecutorToAnswerWith(0);
        String empty = "";
        Mockito.when(plugin.getOutput()).thenReturn(empty);

        try {
            plugin.executeNodeStep(pluginContext, configuration, node);
            Assert.fail("Expected node step exception.");
        } catch (NodeStepException e) {
            Assert.assertEquals(SaltLocalNodeStepFailureReason.SALT_TARGET_MISMATCH, e.getFailureReason());
        }
        
        ArgumentCaptor<CommandLine> captor = ArgumentCaptor.forClass(CommandLine.class);
        Mockito.verify(executor, Mockito.times(1)).execute(captor.capture(),
                Mockito.any(DefaultExecuteResultHandler.class));
        CommandLine commandLine = captor.getValue();
        Assert.assertEquals(PARAM_SALT, commandLine.getExecutable());
        String[] args = commandLine.getArguments();
        Assert.assertEquals(4, args.length);
        Assert.assertEquals(SaltLocalNodeStepPlugin.OUTPUT_MODE, args[0]);
        Assert.assertEquals(SaltLocalNodeStepPlugin.JSON_OUTPUT, args[1]);
        Assert.assertEquals(PARAM_MINION_NAME, args[2]);
        Assert.assertEquals(PARAM_FUNCTION, args[3]);
    }

    @Test
    public void testExecuteWithBadExitCode() throws Exception {
        setExecutorToAnswerWith(1);
        try {
            plugin.executeNodeStep(pluginContext, configuration, node);
            Assert.fail("Expected node step failure.");
        } catch (NodeStepException e) {
            Assert.assertEquals(SaltLocalNodeStepFailureReason.EXIT_CODE, e.getFailureReason());
        }
    }

    protected void setExecutorToAnswerWith(final int exitCode) throws Exception {
        Mockito.doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                DefaultExecuteResultHandler handler = (DefaultExecuteResultHandler) invocation.getArguments()[1];
                handler.onProcessComplete(exitCode);
                return null;
            }
        }).when(executor).execute(Mockito.any(CommandLine.class), Mockito.any(DefaultExecuteResultHandler.class));
    }
}
