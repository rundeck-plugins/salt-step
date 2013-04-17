package com.salesforce.rundeck.plugin;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.junit.Before;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import com.dtolabs.rundeck.core.common.INodeEntry;
import com.dtolabs.rundeck.plugins.PluginLogger;
import com.dtolabs.rundeck.plugins.step.PluginStepContext;
import com.salesforce.rundeck.plugin.output.SaltReturnHandler;
import com.salesforce.rundeck.plugin.output.SaltReturnHandlerRegistry;
import com.salesforce.rundeck.plugin.util.HttpFactory;

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
    public void setUp() {
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

    protected AbstractSaltApiNodeStepPluginTest spyPlugin() throws Exception {
        plugin = Mockito.spy(plugin);
        return this;
    }

    protected AbstractSaltApiNodeStepPluginTest setupAuthenticate() throws Exception {
        return setupAuthenticate(AUTH_TOKEN);
    }
    
    protected AbstractSaltApiNodeStepPluginTest setupAuthenticate(String authToken) throws Exception {
        Mockito.doReturn(authToken).when(plugin)
                .authenticate(Mockito.same(client), Mockito.eq(PARAM_USER), Mockito.eq(PARAM_PASSWORD));
        return this;
    }

    protected AbstractSaltApiNodeStepPluginTest setupResponseCode(HttpMethodBase method, int code) throws Exception {
        Mockito.when(method.getStatusCode()).thenReturn(code);
        return this;
    }

    protected AbstractSaltApiNodeStepPluginTest setupResponse(HttpMethodBase method, int code, String responseBody)
            throws Exception {
        Mockito.when(method.getStatusCode()).thenReturn(code);
        Mockito.when(method.getResponseBodyAsString()).thenReturn(responseBody);
        return this;
    }

    protected void assertPostBody(String dataTemplate, String... args) throws UnsupportedEncodingException {
        Object[] encodedArgs = new String[args.length];
        for (int i = 0; i < args.length; i++) {
            encodedArgs[i] = URLEncoder.encode(args[i], SaltApiNodeStepPlugin.CHAR_SET_ENCODING);
        }
        
        ArgumentCaptor<StringRequestEntity> captor = ArgumentCaptor.forClass(StringRequestEntity.class);
        Mockito.verify(postMethod, Mockito.times(1)).setRequestEntity(captor.capture());
        Assert.assertEquals(String.format(dataTemplate, encodedArgs), captor.getValue().getContent());
        Assert.assertEquals(SaltApiNodeStepPlugin.CHAR_SET_ENCODING, captor.getValue().getCharset());
        Assert.assertTrue(captor.getValue().getContentType().startsWith(SaltApiNodeStepPlugin.REQUEST_CONTENT_TYPE));
    }
}
