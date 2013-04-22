package com.salesforce.rundeck.plugin;

import java.net.URI;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.junit.Before;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import com.dtolabs.rundeck.core.common.INodeEntry;
import com.dtolabs.rundeck.plugins.PluginLogger;
import com.dtolabs.rundeck.plugins.step.PluginStepContext;
import com.salesforce.rundeck.plugin.output.SaltReturnHandler;
import com.salesforce.rundeck.plugin.output.SaltReturnHandlerRegistry;
import com.salesforce.rundeck.plugin.util.HttpFactory;
import com.salesforce.rundeck.plugin.version.SaltApiCapability;

public abstract class AbstractSaltApiNodeStepPluginTest {

    protected static final String PARAM_ENDPOINT = "http://localhost";
    protected static final String PARAM_EAUTH = "pam";
    protected static final String PARAM_MINION_NAME = "minion";
    protected static final String PARAM_FUNCTION = "some.function";
    protected static final String PARAM_USER = "user";
    protected static final String PARAM_PASSWORD = "password&!@$*";

    protected static final String AUTH_TOKEN = "123qwe";
    protected static final String OUTPUT_JID = "20130213093536481553";
    protected static final String HOST_RESPONSE = "\"some response\"";

    protected SaltApiNodeStepPlugin plugin;
    protected SaltApiCapability latestCapability;
    protected HttpClient client;
    protected HttpGet get;
    protected HttpPost post;
    protected HttpEntity responseEntity;
    protected HttpResponse response;
    protected PluginStepContext pluginContext;
    protected PluginLogger logger;
    protected INodeEntry node;
    protected Map<String, Object> configuration;
    protected Map<String, Map<String, String>> dataContext;
    protected Map<String, String> optionContext;
    protected SaltReturnHandlerRegistry returnHandlerRegistry;
    protected SaltReturnHandler returnHandler;

    @Before
    public void setUp() {
        plugin = new SaltApiNodeStepPlugin();
        plugin.saltEndpoint = PARAM_ENDPOINT;
        plugin.eAuth = PARAM_EAUTH;
        plugin.function = PARAM_FUNCTION;
        latestCapability = plugin.capabilityRegistry.getLatest();
        client = Mockito.mock(HttpClient.class);
        post = Mockito.mock(HttpPost.class);
        get = Mockito.mock(HttpGet.class);

        // Http dependencies
        plugin.httpFactory = new HttpFactory() {
            @Override
            public HttpClient createHttpClient() {
                return client;
            }

            @Override
            public HttpPost createHttpPost(String uri) {
                try {
                    Mockito.when(post.getURI()).thenReturn(new URI(uri));
                } catch (Exception e) {
                    throw new IllegalStateException();
                }
                return post;
            }

            @Override
            public HttpGet createHttpGet(String uri) {
                try {
                    Mockito.when(get.getURI()).thenReturn(new URI(uri));
                } catch (Exception e) {
                    throw new IllegalStateException();
                }
                return get;
            }
        };
        responseEntity = Mockito.mock(HttpEntity.class);
        response = Mockito.mock(HttpResponse.class);
        Mockito.when(response.getEntity()).thenReturn(responseEntity);

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

    protected AbstractSaltApiNodeStepPluginTest spyPlugin() {
        try {
            plugin = Mockito.spy(plugin);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    protected AbstractSaltApiNodeStepPluginTest setupAuthenticate() {
        return setupAuthenticate(AUTH_TOKEN);
    }

    protected AbstractSaltApiNodeStepPluginTest setupAuthenticate(String authToken) {
        try {
            Mockito.doReturn(authToken)
                    .when(plugin)
                    .authenticate(Mockito.any(SaltApiCapability.class), Mockito.same(client), Mockito.eq(PARAM_USER),
                            Mockito.eq(PARAM_PASSWORD));
            return this;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected AbstractSaltApiNodeStepPluginTest setupResponseCode(HttpRequestBase method, int code) {
        return setupResponse(method, code, null);
    }

    protected AbstractSaltApiNodeStepPluginTest setupResponse(HttpRequestBase method, int code, String responseBody) {
        try {
            StatusLine statusLine = Mockito.mock(StatusLine.class);
            Mockito.when(response.getStatusLine()).thenReturn(statusLine);
            Mockito.when(statusLine.getStatusCode()).thenReturn(code);
            Mockito.doReturn(responseBody).when(plugin).extractBodyFromEntity(Mockito.same(responseEntity));
            Mockito.when(client.execute(Mockito.same(method))).thenReturn(response);
            return this;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected void assertPostBody(String dataTemplate, String... args) {
        try {
            Object[] encodedArgs = new String[args.length];
            for (int i = 0; i < args.length; i++) {
                encodedArgs[i] = URLEncoder.encode(args[i], SaltApiNodeStepPlugin.CHAR_SET_ENCODING);
            }

            ArgumentCaptor<StringEntity> captor = ArgumentCaptor.forClass(StringEntity.class);
            Mockito.verify(post, Mockito.times(1)).setEntity(captor.capture());
            try {
                Assert.assertEquals(String.format(dataTemplate, encodedArgs),
                        IOUtils.toString(captor.getValue().getContent()));
                Assert.assertEquals(SaltApiNodeStepPlugin.CHAR_SET_ENCODING, captor.getValue().getContentEncoding()
                        .getValue());
                Assert.assertEquals(SaltApiNodeStepPlugin.REQUEST_CONTENT_TYPE, captor.getValue().getContentType()
                        .getValue());
            } finally {
                IOUtils.closeQuietly(captor.getValue().getContent());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected AbstractSaltApiNodeStepPluginTest setupAuthenticationHeadersOnPost(int statusCode) {
        setupResponse(post, statusCode, null);
        Mockito.when(response.getHeaders(Mockito.eq(SaltApiNodeStepPlugin.SALT_AUTH_TOKEN_HEADER))).thenReturn(
                new Header[] { new BasicHeader(SaltApiNodeStepPlugin.SALT_AUTH_TOKEN_HEADER, AUTH_TOKEN) });
        return this;
    }

    protected void assertThatAuthenticationAttemptedSuccessfully() {
        try {
            Assert.assertEquals(PARAM_ENDPOINT + "/login", post.getURI().toString());
            assertPostBody("username=%s&password=%s&eauth=%s", PARAM_USER, PARAM_PASSWORD, PARAM_EAUTH);
            Mockito.verify(client, Mockito.times(1)).execute(Mockito.same(post));

            Mockito.verify(plugin, Mockito.times(1)).closeResource(Mockito.same(responseEntity));
            Mockito.verify(post, Mockito.times(1)).releaseConnection();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
