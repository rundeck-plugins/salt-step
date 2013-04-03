package com.salesforce.rundeck.plugin;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.Assert;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.dtolabs.rundeck.core.Constants;
import com.dtolabs.rundeck.core.common.INodeEntry;
import com.dtolabs.rundeck.core.execution.workflow.steps.node.NodeStepException;
import com.dtolabs.rundeck.plugins.PluginLogger;
import com.dtolabs.rundeck.plugins.step.PluginStepContext;
import com.salesforce.rundeck.plugin.SaltApiNodeStepPlugin.SaltApiNodeStepFailureReason;
import com.salesforce.rundeck.plugin.validation.SaltStepValidationException;
import com.salesforce.rundeck.plugin.validation.Validators;
import com.salesforce.rundeck.plugin.output.SaltReturnHandler;
import com.salesforce.rundeck.plugin.output.SaltReturnHandlerRegistry;
import com.salesforce.rundeck.plugin.output.SaltReturnResponse;
import com.salesforce.rundeck.plugin.output.SaltReturnResponseParseException;
import com.salesforce.rundeck.plugin.util.HttpFactory;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Validators.class)
public class SaltApiNodeStepPluginTest {
    protected static final String PARAM_ENDPOINT = "http://localhost";
    protected static final String PARAM_EAUTH = "pam";
    protected static final String PARAM_MINION_NAME = "minion";
    protected static final String PARAM_FUNCTION = "some.function";
    protected static final String PARAM_USER = "user";
    protected static final String PARAM_PASSWORD = "password&!@$*";

    protected static final String MINIONS_ENDPOINT = PARAM_ENDPOINT + "/minions";
    protected static final String OUTPUT_JID = "20130213093536481553";
    protected static final String JOBS_ENDPOINT = PARAM_ENDPOINT + "/jobs/" + OUTPUT_JID;
    protected static final String HOST_RESPONSE = "\"some response\"";
    protected static final String MINION_JSON_RESPONSE = "[{\"return\": {\"jid\": \"" + OUTPUT_JID
            + "\", \"minions\": [\"" + PARAM_MINION_NAME + "\"]}}]";
    protected static final String HOST_JSON_RESPONSE = "{\"return\":[{" + PARAM_MINION_NAME + ":" + HOST_RESPONSE
            + "}]}";

    protected SaltApiNodeStepPlugin plugin;
    protected HttpClient client;
    protected GetMethod getMethod;
    protected PostMethod postMethod;
    protected PluginStepContext pluginContext;
    protected PluginLogger logger;
    protected INodeEntry node;
    protected Map<String, Object> configuration;
    protected Map<String, Map<String, String>> dataContext;
    protected Map<String, String> optionContext;
    protected SaltReturnHandlerRegistry returnHandlerRegistry;
    protected SaltReturnHandler returnHandler;

    @Before
    public void setup() {
        DependencyManagedNodeStepPlugin.CONFIG_FILE_LOCATION = "/beans-unittest.xml";

        plugin = new SaltApiNodeStepPlugin();
        plugin.saltEndpoint = PARAM_ENDPOINT;
        plugin.eAuth = PARAM_EAUTH;
        plugin.function = PARAM_FUNCTION;
        client = Mockito.mock(HttpClient.class);
        postMethod = Mockito.mock(PostMethod.class);
        getMethod = Mockito.mock(GetMethod.class);

        // Http dependencies
        plugin.httpFactory = new HttpFactory() {
            @Override
            public HttpClient createHttpClient() {
                return client;
            }

            @Override
            public PostMethod createPostMethod(String uri) {
                try {
                    Mockito.when(postMethod.getURI()).thenReturn(new URI(uri, true));
                } catch (Exception e) {
                    throw new IllegalStateException();
                }
                return postMethod;
            }

            @Override
            public GetMethod createGetMethod(String uri) {
                try {
                    Mockito.when(getMethod.getURI()).thenReturn(new URI(uri, true));
                } catch (Exception e) {
                    throw new IllegalStateException();
                }
                return getMethod;
            }
        };

        // Return handler dependencies
        returnHandlerRegistry = Mockito.mock(SaltReturnHandlerRegistry.class);
        plugin.returnHandlerRegistry = returnHandlerRegistry;
        returnHandler = Mockito.mock(SaltReturnHandler.class);
        Mockito.when(returnHandlerRegistry.getHandlerFor(Mockito.anyString(), Mockito.any(SaltReturnHandler.class)))
                .thenReturn(returnHandler);

        // Setup execute method's arguments
        pluginContext = Mockito.mock(PluginStepContext.class);
        logger = Mockito.mock(PluginLogger.class);
        Mockito.when(pluginContext.getLogger()).thenReturn(logger);
        node = Mockito.mock(INodeEntry.class);
        Mockito.when(node.getNodename()).thenReturn(PARAM_MINION_NAME);
        configuration = new HashMap<String, Object>();

        dataContext = new HashMap<String, Map<String, String>>();
        optionContext = new HashMap<String, String>();
        optionContext.put(SaltApiNodeStepPlugin.SALT_USER_OPTION_NAME, PARAM_USER);
        optionContext.put(SaltApiNodeStepPlugin.SALT_PASSWORD_OPTION_NAME, PARAM_PASSWORD);     
        dataContext.put(SaltApiNodeStepPlugin.RUNDECK_DATA_CONTEXT_OPTION_KEY, optionContext);
        Mockito.when(pluginContext.getDataContext()).thenReturn(dataContext);
    }

    @Test
    public void testAuthenticateWithRedirectResponseCode() throws Exception {
        String authToken = "123qwe";
        setupResponseCode(postMethod, HttpStatus.SC_MOVED_TEMPORARILY);
        Mockito.when(postMethod.getResponseHeader(SaltApiNodeStepPlugin.SALT_AUTH_TOKEN_HEADER)).thenReturn(
                new Header(SaltApiNodeStepPlugin.SALT_AUTH_TOKEN_HEADER, authToken));

        Assert.assertEquals(authToken, plugin.authenticate(client, PARAM_USER, PARAM_PASSWORD));

        Assert.assertEquals(PARAM_ENDPOINT + "/login", postMethod.getURI().toString());
        String expectedAuthString = String.format("username=%s&password=%s&eauth=%s",
                URLEncoder.encode(PARAM_USER, SaltApiNodeStepPlugin.CHAR_SET_ENCODING),
                URLEncoder.encode(PARAM_PASSWORD, SaltApiNodeStepPlugin.CHAR_SET_ENCODING), PARAM_EAUTH);
        assertPostBody(expectedAuthString);
        Mockito.verify(client, Mockito.times(1)).executeMethod(Mockito.same(postMethod));

        Mockito.verify(postMethod, Mockito.times(1)).releaseConnection();
    }
    
    @Test
    public void testAuthenticateWithOkResponseCode() throws Exception {
        String authToken = "123qwe";
        setupResponseCode(postMethod, HttpStatus.SC_OK);
        Mockito.when(postMethod.getResponseHeader(SaltApiNodeStepPlugin.SALT_AUTH_TOKEN_HEADER)).thenReturn(
                new Header(SaltApiNodeStepPlugin.SALT_AUTH_TOKEN_HEADER, authToken));

        Assert.assertEquals(authToken, plugin.authenticate(client, PARAM_USER, PARAM_PASSWORD));

        Assert.assertEquals(PARAM_ENDPOINT + "/login", postMethod.getURI().toString());
        String expectedAuthString = String.format("username=%s&password=%s&eauth=%s",
                URLEncoder.encode(PARAM_USER, SaltApiNodeStepPlugin.CHAR_SET_ENCODING),
                URLEncoder.encode(PARAM_PASSWORD, SaltApiNodeStepPlugin.CHAR_SET_ENCODING), PARAM_EAUTH);
        assertPostBody(expectedAuthString);
        Mockito.verify(client, Mockito.times(1)).executeMethod(Mockito.same(postMethod));

        Mockito.verify(postMethod, Mockito.times(1)).releaseConnection();
    }

    @Test
    public void testAuthenticateFailure() throws Exception {
        setupResponseCode(postMethod, HttpStatus.SC_INTERNAL_SERVER_ERROR);

        Assert.assertNull(plugin.authenticate(client, PARAM_USER, PARAM_PASSWORD));

        Assert.assertEquals(PARAM_ENDPOINT + "/login", postMethod.getURI().toString());
        String expectedAuthString = String.format("username=%s&password=%s&eauth=%s",
                URLEncoder.encode(PARAM_USER, SaltApiNodeStepPlugin.CHAR_SET_ENCODING),
                URLEncoder.encode(PARAM_PASSWORD, SaltApiNodeStepPlugin.CHAR_SET_ENCODING), PARAM_EAUTH);
        assertPostBody(expectedAuthString);
        Mockito.verify(client, Mockito.times(1)).executeMethod(Mockito.same(postMethod));

        Mockito.verify(postMethod, Mockito.times(1)).releaseConnection();
    }

    @Test
    public void testSubmitJob() throws Exception {
        String authToken = "some token";
        spyPlugin().setupAuthenticate(authToken)
                .setupResponse(postMethod, HttpStatus.SC_ACCEPTED, MINION_JSON_RESPONSE);

        Assert.assertEquals(OUTPUT_JID, plugin.submitJob(pluginContext, client, authToken, PARAM_MINION_NAME));

        Assert.assertEquals(MINIONS_ENDPOINT, postMethod.getURI().toString());
        String expectedBody = String.format("fun=%s&tgt=%s", PARAM_FUNCTION, PARAM_MINION_NAME);
        assertPostBody(expectedBody);
        Mockito.verify(postMethod, Mockito.times(1)).setRequestHeader(SaltApiNodeStepPlugin.SALT_AUTH_TOKEN_HEADER,
                authToken);
        Mockito.verify(postMethod, Mockito.times(1)).setRequestHeader(SaltApiNodeStepPlugin.REQUEST_ACCEPT_HEADER_NAME,
                SaltApiNodeStepPlugin.JSON_RESPONSE_ACCEPT_TYPE);
        Mockito.verify(client, Mockito.times(1)).executeMethod(Mockito.same(postMethod));

        Mockito.verify(logger, Mockito.times(1)).log(Mockito.eq(Constants.DEBUG_LEVEL), Mockito.anyString());

        Mockito.verify(postMethod, Mockito.times(1)).releaseConnection();
    }

    @Test
    public void testSubmitJobWithArgs() throws Exception {
        String authToken = "some token";
        spyPlugin().setupAuthenticate(authToken)
                .setupResponse(postMethod, HttpStatus.SC_ACCEPTED, MINION_JSON_RESPONSE);

        String arg1 = "sdf%33&";
        String arg2 = "adsf asdf";
        plugin.function = PARAM_FUNCTION + " " + arg1 + " \"" + arg2 + "\"";
        Assert.assertEquals(OUTPUT_JID, plugin.submitJob(pluginContext, client, authToken, PARAM_MINION_NAME));

        Assert.assertEquals(MINIONS_ENDPOINT, postMethod.getURI().toString());
        String expectedBody = String.format("fun=%s&tgt=%s&arg=%s&arg=%s", PARAM_FUNCTION, PARAM_MINION_NAME,
                URLEncoder.encode(arg1, SaltApiNodeStepPlugin.CHAR_SET_ENCODING),
                URLEncoder.encode(arg2, SaltApiNodeStepPlugin.CHAR_SET_ENCODING));
        assertPostBody(expectedBody);
        Mockito.verify(postMethod, Mockito.times(1)).setRequestHeader(SaltApiNodeStepPlugin.SALT_AUTH_TOKEN_HEADER,
                authToken);
        Mockito.verify(postMethod, Mockito.times(1)).setRequestHeader(SaltApiNodeStepPlugin.REQUEST_ACCEPT_HEADER_NAME,
                SaltApiNodeStepPlugin.JSON_RESPONSE_ACCEPT_TYPE);
        Mockito.verify(client, Mockito.times(1)).executeMethod(Mockito.same(postMethod));

        Mockito.verify(logger, Mockito.times(1)).log(Mockito.eq(Constants.DEBUG_LEVEL), Mockito.anyString());

        Mockito.verify(postMethod, Mockito.times(1)).releaseConnection();
    }

    @Test
    public void testSubmitJobResponseCodeError() throws Exception {
        String authToken = "some token";
        spyPlugin().setupAuthenticate(authToken).setupResponse(postMethod, 302, MINION_JSON_RESPONSE);

        try {
            plugin.submitJob(pluginContext, client, authToken, PARAM_MINION_NAME);
            Assert.fail("Expected http exception due to bad response code.");
        } catch (HttpException e) {
            // expected
        }

        Assert.assertEquals(MINIONS_ENDPOINT, postMethod.getURI().toString());
        String expectedBody = String.format("fun=%s&tgt=%s", PARAM_FUNCTION, PARAM_MINION_NAME);
        assertPostBody(expectedBody);
        Mockito.verify(postMethod, Mockito.times(1)).setRequestHeader(SaltApiNodeStepPlugin.SALT_AUTH_TOKEN_HEADER,
                authToken);
        Mockito.verify(postMethod, Mockito.times(1)).setRequestHeader(SaltApiNodeStepPlugin.REQUEST_ACCEPT_HEADER_NAME,
                SaltApiNodeStepPlugin.JSON_RESPONSE_ACCEPT_TYPE);
        Mockito.verify(client, Mockito.times(1)).executeMethod(Mockito.same(postMethod));

        Mockito.verify(postMethod, Mockito.times(1)).releaseConnection();
    }

    @Test
    public void testSubmitJobMinionCountMismatch() throws Exception {
        String authToken = "some token";
        String output = "[{\"return\": {\"jid\": \"" + OUTPUT_JID + "\", \"minions\": []}}]";
        spyPlugin().setupAuthenticate(authToken).setupResponse(postMethod, HttpStatus.SC_ACCEPTED, output);

        try {
            plugin.submitJob(pluginContext, client, authToken, PARAM_MINION_NAME);
            Assert.fail("Expected targetting mismatch exception.");
        } catch (SaltTargettingMismatchException e) {
            // expected
        }

        Assert.assertEquals(MINIONS_ENDPOINT, postMethod.getURI().toString());
        String expectedBody = String.format("fun=%s&tgt=%s", PARAM_FUNCTION, PARAM_MINION_NAME);
        assertPostBody(expectedBody);
        Mockito.verify(postMethod, Mockito.times(1)).setRequestHeader(SaltApiNodeStepPlugin.SALT_AUTH_TOKEN_HEADER,
                authToken);
        Mockito.verify(postMethod, Mockito.times(1)).setRequestHeader(SaltApiNodeStepPlugin.REQUEST_ACCEPT_HEADER_NAME,
                SaltApiNodeStepPlugin.JSON_RESPONSE_ACCEPT_TYPE);
        Mockito.verify(client, Mockito.times(1)).executeMethod(Mockito.same(postMethod));

        Mockito.verify(logger, Mockito.times(1)).log(Mockito.eq(Constants.DEBUG_LEVEL), Mockito.anyString());

        Mockito.verify(postMethod, Mockito.times(1)).releaseConnection();
    }

    @Test
    public void testSubmitJobMultipleResponses() throws Exception {
        String authToken = "some token";
        String output = "[{\"return\": {\"jid\": \"" + OUTPUT_JID + "\", \"minions\": [\"SomeOtherMinion\"]}},{}]";
        spyPlugin().setupAuthenticate(authToken).setupResponse(postMethod, HttpStatus.SC_ACCEPTED, output);

        try {
            plugin.submitJob(pluginContext, client, authToken, PARAM_MINION_NAME);
            Assert.fail("Expected salt-api response exception.");
        } catch (SaltApiException e) {
            // expected
        }

        Assert.assertEquals(MINIONS_ENDPOINT, postMethod.getURI().toString());
        String expectedBody = String.format("fun=%s&tgt=%s", PARAM_FUNCTION, PARAM_MINION_NAME);
        assertPostBody(expectedBody);
        Mockito.verify(postMethod, Mockito.times(1)).setRequestHeader(SaltApiNodeStepPlugin.SALT_AUTH_TOKEN_HEADER,
                authToken);
        Mockito.verify(postMethod, Mockito.times(1)).setRequestHeader(SaltApiNodeStepPlugin.REQUEST_ACCEPT_HEADER_NAME,
                SaltApiNodeStepPlugin.JSON_RESPONSE_ACCEPT_TYPE);
        Mockito.verify(client, Mockito.times(1)).executeMethod(Mockito.same(postMethod));

        Mockito.verify(logger, Mockito.times(1)).log(Mockito.eq(Constants.DEBUG_LEVEL), Mockito.anyString());

        Mockito.verify(postMethod, Mockito.times(1)).releaseConnection();
    }

    @Test
    public void testSubmitJobMinionIdMismatch() throws Exception {
        String authToken = "some token";
        String output = "[{\"return\": {\"jid\": \"" + OUTPUT_JID + "\", \"minions\": [\"SomeOtherMinion\"]}}]";
        spyPlugin().setupAuthenticate(authToken).setupResponse(postMethod, HttpStatus.SC_ACCEPTED, output);

        try {
            plugin.submitJob(pluginContext, client, authToken, PARAM_MINION_NAME);
            Assert.fail("Expected targetting mismatch exception.");
        } catch (SaltTargettingMismatchException e) {
            // expected
        }

        Assert.assertEquals(MINIONS_ENDPOINT, postMethod.getURI().toString());
        String expectedBody = String.format("fun=%s&tgt=%s", PARAM_FUNCTION, PARAM_MINION_NAME);
        assertPostBody(expectedBody);
        Mockito.verify(postMethod, Mockito.times(1)).setRequestHeader(SaltApiNodeStepPlugin.SALT_AUTH_TOKEN_HEADER,
                authToken);
        Mockito.verify(postMethod, Mockito.times(1)).setRequestHeader(SaltApiNodeStepPlugin.REQUEST_ACCEPT_HEADER_NAME,
                SaltApiNodeStepPlugin.JSON_RESPONSE_ACCEPT_TYPE);
        Mockito.verify(client, Mockito.times(1)).executeMethod(Mockito.same(postMethod));

        Mockito.verify(logger, Mockito.times(1)).log(Mockito.eq(Constants.DEBUG_LEVEL), Mockito.anyString());

        Mockito.verify(postMethod, Mockito.times(1)).releaseConnection();
    }

    @Test
    public void testExecuteWithAuthenticationFailure() throws Exception {
        spyPlugin().setupAuthenticate(null).setValidationSuccessful();

        try {
            plugin.executeNodeStep(pluginContext, configuration, node);
            Assert.fail("Expected authentication failure");
        } catch (NodeStepException e) {
            Assert.assertEquals(SaltApiNodeStepFailureReason.AUTHENTICATION_FAILURE, e.getFailureReason());
        }

        Mockito.verifyZeroInteractions(client, postMethod);
    }

    @Test
    public void testExecuteWithValidationFailure() throws Exception {
        spyPlugin();
        SaltStepValidationException e = new SaltStepValidationException("some property", "Some message",
                SaltApiNodeStepFailureReason.ARGUMENTS_INVALID, node.getNodename());
        Mockito.doThrow(e).when(plugin)
                .validate(Mockito.eq(PARAM_USER), Mockito.eq(PARAM_PASSWORD), Mockito.same(node));
        try {
            plugin.executeNodeStep(pluginContext, configuration, node);
            Assert.fail("Expected exception");
        } catch (SaltStepValidationException ne) {
            Assert.assertSame(e, ne);
        }
    }

    @Test
    public void testExecuteWithDataContextMissing() {
        dataContext.clear();
        try {
            plugin.executeNodeStep(pluginContext, configuration, node);
            Assert.fail("Expected exception");
        } catch (NodeStepException e) {
            Assert.assertEquals(SaltApiNodeStepFailureReason.ARGUMENTS_MISSING, e.getFailureReason());
        }
    }

    public void testExecuteWithSuccessfulExitCode() throws Exception {
        String authToken = "some token";
        spyPlugin().setupAuthenticate(authToken).setValidationSuccessful();

        Mockito.doReturn(OUTPUT_JID)
                .when(plugin)
                .submitJob(Mockito.same(pluginContext), Mockito.same(client), Mockito.eq(authToken),
                        Mockito.eq(PARAM_MINION_NAME));

        Mockito.doReturn(HOST_RESPONSE)
                .when(plugin)
                .waitForJidResponse(Mockito.same(pluginContext), Mockito.same(client), Mockito.eq(authToken),
                        Mockito.eq(OUTPUT_JID), Mockito.eq(PARAM_MINION_NAME));

        SaltReturnResponse response = new SaltReturnResponse();
        String output1 = "line 1 of output";
        String output2 = "line 2 of output";
        response.addOutput(output1);
        response.addOutput(output2);
        String error1 = "line 1 of error";
        String error2 = "line 2 of error";
        response.addError(error1);
        response.addError(error2);
        response.setExitCode(0);
        Mockito.when(returnHandler.extractResponse(Mockito.anyString())).thenReturn(response);
        
        plugin.function += " arg1 arg2";

        plugin.executeNodeStep(pluginContext, configuration, node);

        Mockito.verify(returnHandlerRegistry, Mockito.times(1)).getHandlerFor(Mockito.eq(PARAM_FUNCTION),
                Mockito.same(plugin.defaultReturnHandler));
        Mockito.verify(returnHandler, Mockito.times(1)).extractResponse(Mockito.eq(HOST_RESPONSE));

        Mockito.verify(logger, Mockito.times(1)).log(Mockito.eq(Constants.INFO_LEVEL), Mockito.eq(output1));
        Mockito.verify(logger, Mockito.times(1)).log(Mockito.eq(Constants.INFO_LEVEL), Mockito.eq(output2));
        Mockito.verify(logger, Mockito.times(1)).log(Mockito.eq(Constants.ERR_LEVEL), Mockito.eq(error1));
        Mockito.verify(logger, Mockito.times(1)).log(Mockito.eq(Constants.ERR_LEVEL), Mockito.eq(error2));
    }

    @Test
    public void testExecuteWithUnsuccessfulExitCode() throws Exception {
        String authToken = "some token";
        spyPlugin().setupAuthenticate(authToken);

        Mockito.doReturn(OUTPUT_JID)
                .when(plugin)
                .submitJob(Mockito.same(pluginContext), Mockito.same(client), Mockito.eq(authToken),
                        Mockito.eq(PARAM_MINION_NAME));

        Mockito.doReturn(HOST_RESPONSE)
                .when(plugin)
                .waitForJidResponse(Mockito.same(pluginContext), Mockito.same(client), Mockito.eq(authToken),
                        Mockito.eq(OUTPUT_JID), Mockito.eq(PARAM_MINION_NAME));

        SaltReturnResponse response = new SaltReturnResponse();
        String output1 = "line 1 of output";
        String output2 = "line 2 of output";
        response.addOutput(output1);
        response.addOutput(output2);
        String error1 = "line 1 of error";
        String error2 = "line 2 of error";
        response.addError(error1);
        response.addError(error2);
        response.setExitCode(1);
        Mockito.when(returnHandler.extractResponse(Mockito.anyString())).thenReturn(response);

        plugin.function += " arg1 arg2";
        
        try {
            plugin.executeNodeStep(pluginContext, configuration, node);
            Assert.fail("Expected failure due to exit code.");
        } catch (NodeStepException e) {
            Assert.assertEquals(SaltApiNodeStepFailureReason.EXIT_CODE, e.getFailureReason());
        }

        Mockito.verify(returnHandlerRegistry, Mockito.times(1)).getHandlerFor(Mockito.eq(PARAM_FUNCTION),
                Mockito.same(plugin.defaultReturnHandler));
        Mockito.verify(returnHandler, Mockito.times(1)).extractResponse(Mockito.eq(HOST_RESPONSE));

        Mockito.verify(logger, Mockito.times(1)).log(Mockito.eq(Constants.INFO_LEVEL), Mockito.eq(output1));
        Mockito.verify(logger, Mockito.times(1)).log(Mockito.eq(Constants.INFO_LEVEL), Mockito.eq(output2));
        Mockito.verify(logger, Mockito.times(1)).log(Mockito.eq(Constants.ERR_LEVEL), Mockito.eq(error1));
        Mockito.verify(logger, Mockito.times(1)).log(Mockito.eq(Constants.ERR_LEVEL), Mockito.eq(error2));
    }
    
    @Test
    public void testExecuteWithSaltResponseParseException() throws Exception {
        String authToken = "some token";
        spyPlugin().setupAuthenticate(authToken);

        Mockito.doReturn(OUTPUT_JID)
                .when(plugin)
                .submitJob(Mockito.same(pluginContext), Mockito.same(client), Mockito.eq(authToken),
                        Mockito.eq(PARAM_MINION_NAME));

        Mockito.doReturn(HOST_RESPONSE)
                .when(plugin)
                .waitForJidResponse(Mockito.same(pluginContext), Mockito.same(client), Mockito.eq(authToken),
                        Mockito.eq(OUTPUT_JID), Mockito.eq(PARAM_MINION_NAME));

        SaltReturnResponseParseException pe = new SaltReturnResponseParseException("message");
        Mockito.when(returnHandler.extractResponse(Mockito.anyString())).thenThrow(pe);

        plugin.function += " arg1 arg2";
        
        try {
            plugin.executeNodeStep(pluginContext, configuration, node);
            Assert.fail("Expected failure due to response parse exception.");
        } catch (NodeStepException e) {
            Assert.assertEquals(SaltApiNodeStepFailureReason.SALT_API_FAILURE, e.getFailureReason());
            Assert.assertSame(pe, e.getCause());
        }

        Mockito.verify(returnHandlerRegistry, Mockito.times(1)).getHandlerFor(Mockito.eq(PARAM_FUNCTION),
                Mockito.same(plugin.defaultReturnHandler));
        Mockito.verify(returnHandler, Mockito.times(1)).extractResponse(Mockito.eq(HOST_RESPONSE));
    }

    @Test
    public void testExecuteWithSaltApiException() throws Exception {
        String authToken = "some token";
        spyPlugin().setupAuthenticate(authToken).setValidationSuccessful();
        String exceptionMessage = "some message";
        Mockito.doThrow(new SaltApiException(exceptionMessage))
                .when(plugin)
                .submitJob(Mockito.same(pluginContext), Mockito.same(client), Mockito.eq(authToken),
                        Mockito.eq(PARAM_MINION_NAME));

        try {
            plugin.executeNodeStep(pluginContext, configuration, node);
            Assert.fail("Expected node step failure.");
        } catch (NodeStepException e) {
            Assert.assertEquals(SaltApiNodeStepFailureReason.SALT_API_FAILURE, e.getFailureReason());
        }
    }

    @Test
    public void testExecuteWithSaltTargettingException() throws Exception {
        String authToken = "some token";
        spyPlugin().setupAuthenticate(authToken).setValidationSuccessful();
        String exceptionMessage = "some message";
        Mockito.doThrow(new SaltTargettingMismatchException(exceptionMessage))
                .when(plugin)
                .submitJob(Mockito.same(pluginContext), Mockito.same(client), Mockito.eq(authToken),
                        Mockito.eq(PARAM_MINION_NAME));

        try {
            plugin.executeNodeStep(pluginContext, configuration, node);
            Assert.fail("Expected node step failure.");
        } catch (NodeStepException e) {
            Assert.assertEquals(SaltApiNodeStepFailureReason.SALT_TARGET_MISMATCH, e.getFailureReason());
        }
    }

    @Test
    public void testExecuteWithHttpException() throws Exception {
        String authToken = "some token";
        spyPlugin().setupAuthenticate(authToken).setValidationSuccessful();
        String exceptionMessage = "some message";
        Mockito.doThrow(new HttpException(exceptionMessage))
                .when(plugin)
                .submitJob(Mockito.same(pluginContext), Mockito.same(client), Mockito.eq(authToken),
                        Mockito.eq(PARAM_MINION_NAME));

        try {
            plugin.executeNodeStep(pluginContext, configuration, node);
            Assert.fail("Expected node step failure.");
        } catch (NodeStepException e) {
            Assert.assertEquals(SaltApiNodeStepFailureReason.COMMUNICATION_FAILURE, e.getFailureReason());
        }
    }

    @Test
    public void testExecuteWithInterruptedException() throws Exception {
        String authToken = "some token";
        spyPlugin().setupAuthenticate(authToken).setValidationSuccessful();

        Mockito.doReturn(OUTPUT_JID)
                .when(plugin)
                .submitJob(Mockito.same(pluginContext), Mockito.same(client), Mockito.eq(authToken),
                        Mockito.eq(PARAM_MINION_NAME));

        Mockito.doThrow(new InterruptedException())
                .when(plugin)
                .waitForJidResponse(Mockito.same(pluginContext), Mockito.same(client), Mockito.eq(authToken),
                        Mockito.eq(OUTPUT_JID), Mockito.eq(PARAM_MINION_NAME));

        try {
            plugin.executeNodeStep(pluginContext, configuration, node);
            Assert.fail("Expected node step failure.");
        } catch (NodeStepException e) {
            Assert.assertEquals(SaltApiNodeStepFailureReason.INTERRUPTED, e.getFailureReason());
        }
    }

    @Test
    public void testWaitForJidResponse() throws Exception {
        String authToken = "some token";
        spyPlugin();
        plugin.pollFrequency = 1L;

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
                .extractOutputForJid(Mockito.same(pluginContext), Mockito.same(client), Mockito.eq(authToken),
                        Mockito.eq(OUTPUT_JID), Mockito.eq(PARAM_MINION_NAME));

        Assert.assertEquals(HOST_RESPONSE,
                plugin.waitForJidResponse(pluginContext, client, authToken, OUTPUT_JID, PARAM_MINION_NAME));

        Mockito.verify(plugin, Mockito.times(2)).extractOutputForJid(Mockito.same(pluginContext), Mockito.same(client),
                Mockito.eq(authToken), Mockito.eq(OUTPUT_JID), Mockito.eq(PARAM_MINION_NAME));
    }

    @Test(expected = InterruptedException.class)
    public void testWaitForJidResponseInterrupted() throws Exception {
        String authToken = "some token";
        spyPlugin();
        Mockito.doReturn(null)
                .when(plugin)
                .extractOutputForJid(Mockito.same(pluginContext), Mockito.same(client), Mockito.eq(authToken),
                        Mockito.eq(OUTPUT_JID), Mockito.eq(PARAM_MINION_NAME));

        Thread.currentThread().interrupt();
        Assert.assertNull(plugin.waitForJidResponse(pluginContext, client, authToken, OUTPUT_JID, PARAM_MINION_NAME));

        Mockito.verify(plugin, Mockito.times(1)).extractOutputForJid(Mockito.same(pluginContext), Mockito.same(client),
                Mockito.eq(authToken), Mockito.eq(OUTPUT_JID), Mockito.eq(PARAM_MINION_NAME));
        Assert.assertTrue(Thread.interrupted());
    }

    @Test
    public void testExtractOutputForJid() throws Exception {
        String authToken = "some token";
        setupResponse(getMethod, HttpStatus.SC_OK, HOST_JSON_RESPONSE);

        Assert.assertEquals(HOST_RESPONSE,
                plugin.extractOutputForJid(pluginContext, client, authToken, OUTPUT_JID, PARAM_MINION_NAME));

        Assert.assertEquals(JOBS_ENDPOINT, getMethod.getURI().toString());
        Mockito.verify(getMethod, Mockito.times(1)).setRequestHeader(SaltApiNodeStepPlugin.SALT_AUTH_TOKEN_HEADER,
                authToken);
        Mockito.verify(getMethod, Mockito.times(1)).setRequestHeader(SaltApiNodeStepPlugin.REQUEST_ACCEPT_HEADER_NAME,
                SaltApiNodeStepPlugin.JSON_RESPONSE_ACCEPT_TYPE);
        Mockito.verify(client, Mockito.times(1)).executeMethod(Mockito.same(getMethod));
        Mockito.verify(getMethod, Mockito.times(1)).releaseConnection();
    }

    @Test
    public void testExtractOutputForJidBadResponse() throws Exception {
        String authToken = "some token";
        setupResponseCode(getMethod, HttpStatus.SC_INTERNAL_SERVER_ERROR);

        Assert.assertNull(plugin.extractOutputForJid(pluginContext, client, authToken, OUTPUT_JID, PARAM_MINION_NAME));

        Assert.assertEquals(JOBS_ENDPOINT, getMethod.getURI().toString());
        Mockito.verify(getMethod, Mockito.times(1)).setRequestHeader(SaltApiNodeStepPlugin.SALT_AUTH_TOKEN_HEADER,
                authToken);
        Mockito.verify(getMethod, Mockito.times(1)).setRequestHeader(SaltApiNodeStepPlugin.REQUEST_ACCEPT_HEADER_NAME,
                SaltApiNodeStepPlugin.JSON_RESPONSE_ACCEPT_TYPE);
        Mockito.verify(client, Mockito.times(1)).executeMethod(Mockito.same(getMethod));

        Mockito.verify(getMethod, Mockito.never()).getResponseBodyAsString();
        Mockito.verify(getMethod, Mockito.times(1)).releaseConnection();
    }

    @Test
    public void testExtractOutputForJidHostEmptyResponse() throws Exception {
        String authToken = "some token";
        String emptyHostResponse = "{\"return\":[{" + PARAM_MINION_NAME + ": \"\"}]}";
        setupResponse(getMethod, HttpStatus.SC_OK, emptyHostResponse);

        Assert.assertEquals("\"\"",
                plugin.extractOutputForJid(pluginContext, client, authToken, OUTPUT_JID, PARAM_MINION_NAME));

        Assert.assertEquals(JOBS_ENDPOINT, getMethod.getURI().toString());
        Mockito.verify(getMethod, Mockito.times(1)).setRequestHeader(SaltApiNodeStepPlugin.SALT_AUTH_TOKEN_HEADER,
                authToken);
        Mockito.verify(getMethod, Mockito.times(1)).setRequestHeader(SaltApiNodeStepPlugin.REQUEST_ACCEPT_HEADER_NAME,
                SaltApiNodeStepPlugin.JSON_RESPONSE_ACCEPT_TYPE);
        Mockito.verify(client, Mockito.times(1)).executeMethod(Mockito.same(getMethod));
        Mockito.verify(getMethod, Mockito.times(1)).releaseConnection();
    }

    @Test
    public void testExtractOutputForJidNoResponse() throws Exception {
        String authToken = "some token";
        String noResponse = "{\"return\":[{}]}";
        setupResponse(getMethod, HttpStatus.SC_OK, noResponse);

        Assert.assertNull(plugin.extractOutputForJid(pluginContext, client, authToken, OUTPUT_JID, PARAM_MINION_NAME));

        Assert.assertEquals(JOBS_ENDPOINT, getMethod.getURI().toString());
        Mockito.verify(getMethod, Mockito.times(1)).setRequestHeader(SaltApiNodeStepPlugin.SALT_AUTH_TOKEN_HEADER,
                authToken);
        Mockito.verify(getMethod, Mockito.times(1)).setRequestHeader(SaltApiNodeStepPlugin.REQUEST_ACCEPT_HEADER_NAME,
                SaltApiNodeStepPlugin.JSON_RESPONSE_ACCEPT_TYPE);
        Mockito.verify(client, Mockito.times(1)).executeMethod(Mockito.same(getMethod));
        Mockito.verify(getMethod, Mockito.times(1)).releaseConnection();
    }

    @Test
    public void testExtractOutputForJidMultipleResponses() throws Exception {
        String authToken = "some token";
        String multipleResponse = "{\"return\":[{},{}]}";
        setupResponse(getMethod, HttpStatus.SC_OK, multipleResponse);

        try {
            plugin.extractOutputForJid(pluginContext, client, authToken, OUTPUT_JID, PARAM_MINION_NAME);
            Assert.fail("Expected exception for multiple responses.");
        } catch (SaltApiException e) {
            // expected
        }

        Assert.assertEquals(JOBS_ENDPOINT, getMethod.getURI().toString());
        Mockito.verify(getMethod, Mockito.times(1)).setRequestHeader(SaltApiNodeStepPlugin.SALT_AUTH_TOKEN_HEADER,
                authToken);
        Mockito.verify(getMethod, Mockito.times(1)).setRequestHeader(SaltApiNodeStepPlugin.REQUEST_ACCEPT_HEADER_NAME,
                SaltApiNodeStepPlugin.JSON_RESPONSE_ACCEPT_TYPE);
        Mockito.verify(client, Mockito.times(1)).executeMethod(Mockito.same(getMethod));
        Mockito.verify(getMethod, Mockito.times(1)).releaseConnection();
    }

    @Test
    public void testValidateAllArguments() throws SaltStepValidationException {
        PowerMockito.mockStatic(Validators.class);
        PowerMockito.doNothing().when(Validators.class);
        Validators.checkNotEmpty(Mockito.anyString(), Mockito.anyString(),
                Mockito.any(SaltApiNodeStepFailureReason.class), Mockito.same(node));

        plugin.validate(PARAM_USER, PARAM_PASSWORD, node);

        PowerMockito.verifyStatic();
        Validators.checkNotEmpty(SaltApiNodeStepPlugin.SALT_API_END_POINT_OPTION_NAME, PARAM_ENDPOINT,
                SaltApiNodeStepFailureReason.ARGUMENTS_MISSING, node);
        PowerMockito.verifyStatic();
        Validators.checkNotEmpty(SaltApiNodeStepPlugin.SALT_API_FUNCTION_OPTION_NAME, PARAM_FUNCTION,
                SaltApiNodeStepFailureReason.ARGUMENTS_MISSING, node);
        PowerMockito.verifyStatic();
        Validators.checkNotEmpty(SaltApiNodeStepPlugin.SALT_API_EAUTH_OPTION_NAME, PARAM_EAUTH,
                SaltApiNodeStepFailureReason.ARGUMENTS_MISSING, node);
        PowerMockito.verifyStatic();
        Validators.checkNotEmpty(SaltApiNodeStepPlugin.SALT_USER_OPTION_NAME, PARAM_USER,
                SaltApiNodeStepFailureReason.ARGUMENTS_MISSING, node);
        PowerMockito.verifyStatic();
        Validators.checkNotEmpty(SaltApiNodeStepPlugin.SALT_PASSWORD_OPTION_NAME, PARAM_PASSWORD,
                SaltApiNodeStepFailureReason.ARGUMENTS_MISSING, node);
        PowerMockito.verifyNoMoreInteractions(Validators.class);
    }

    @Test
    public void testValidateThrowsIfValidatorThrows() throws SaltStepValidationException {
        SaltStepValidationException e = new SaltStepValidationException("some property", "Some message",
                SaltApiNodeStepFailureReason.ARGUMENTS_INVALID, node.getNodename());
        PowerMockito.mockStatic(Validators.class);
        PowerMockito.doThrow(e).when(Validators.class);
        Validators.checkNotEmpty(Mockito.anyString(), Mockito.anyString(),
                Mockito.any(SaltApiNodeStepFailureReason.class), Mockito.same(node));

        try {
            plugin.validate(PARAM_USER, PARAM_PASSWORD, node);
            Assert.fail("Expected exception");
        } catch (SaltStepValidationException ne) {
            Assert.assertSame(e, ne);
        }
    }

    @Test
    public void testValidateChecksValidEndpointHttpUrl() throws NodeStepException {
        plugin.saltEndpoint = "http://some.machine.com";
        plugin.validate(PARAM_USER, PARAM_PASSWORD, node);
    }

    @Test
    public void testValidateChecksValidEndpointHttpsUrl() throws NodeStepException {
        plugin.saltEndpoint = "https://some.machine.com";
        plugin.validate(PARAM_USER, PARAM_PASSWORD, node);
    }

    @Test
    public void testValidateChecksInvalidEndpointUrl() throws NodeStepException {
        plugin.saltEndpoint = "ftp://some.machine.com";
        try {
            plugin.validate(PARAM_USER, PARAM_PASSWORD, node);
            Assert.fail("Expected failure.");
        } catch (SaltStepValidationException e) {
            Assert.assertEquals(SaltApiNodeStepFailureReason.ARGUMENTS_INVALID, e.getFailureReason());
            Assert.assertEquals(SaltApiNodeStepPlugin.SALT_API_END_POINT_OPTION_NAME, e.getFieldName());
        }
    }

    protected SaltApiNodeStepPluginTest spyPlugin() throws Exception {
        plugin = Mockito.spy(plugin);
        return this;
    }

    protected SaltApiNodeStepPluginTest setupAuthenticate(String authToken) throws Exception {
        Mockito.doReturn(authToken).when(plugin)
                .authenticate(Mockito.same(client), Mockito.eq(PARAM_USER), Mockito.eq(PARAM_PASSWORD));
        return this;
    }

    protected SaltApiNodeStepPluginTest setupResponseCode(HttpMethodBase method, int code) throws Exception {
        Mockito.when(method.getStatusCode()).thenReturn(code);
        return this;
    }

    protected SaltApiNodeStepPluginTest setupResponse(HttpMethodBase method, int code, String responseBody)
            throws Exception {
        Mockito.when(method.getStatusCode()).thenReturn(code);
        Mockito.when(method.getResponseBodyAsString()).thenReturn(responseBody);
        return this;
    }

    protected void assertPostBody(String urlencodedData) {
        ArgumentCaptor<StringRequestEntity> captor = ArgumentCaptor.forClass(StringRequestEntity.class);
        Mockito.verify(postMethod, Mockito.times(1)).setRequestEntity(captor.capture());
        Assert.assertEquals(urlencodedData, captor.getValue().getContent());
        Assert.assertEquals(SaltApiNodeStepPlugin.CHAR_SET_ENCODING, captor.getValue().getCharset());
        Assert.assertTrue(captor.getValue().getContentType().startsWith(SaltApiNodeStepPlugin.REQUEST_CONTENT_TYPE));
    }

    protected SaltApiNodeStepPluginTest setValidationSuccessful() throws NodeStepException {
        Mockito.doNothing().when(plugin)
                .validate(Mockito.eq(PARAM_USER), Mockito.eq(PARAM_PASSWORD), Mockito.same(node));
        return this;
    }
}
