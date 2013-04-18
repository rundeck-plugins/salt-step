package com.salesforce.rundeck.plugin;

import static com.salesforce.rundeck.plugin.validation.Validators.checkNotEmpty;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import org.apache.commons.validator.routines.UrlValidator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;

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
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.salesforce.rundeck.plugin.output.SaltApiResponseOutput;
import com.salesforce.rundeck.plugin.output.SaltReturnHandler;
import com.salesforce.rundeck.plugin.output.SaltReturnHandlerRegistry;
import com.salesforce.rundeck.plugin.output.SaltReturnResponse;
import com.salesforce.rundeck.plugin.output.SaltReturnResponseParseException;
import com.salesforce.rundeck.plugin.util.ArgumentParser;
import com.salesforce.rundeck.plugin.util.DependencyInjectionUtil;
import com.salesforce.rundeck.plugin.util.HttpFactory;
import com.salesforce.rundeck.plugin.validation.SaltStepValidationException;

/**
 * This plugin allows salt execution on a specific minion using the salt-api
 * interface.
 * 
 * Pre-requisites:
 * <ul>
 * <li>Salt-api must be installed.</li>
 * <li>Project node resources must be configured with the name as the salt minion's name as configured on the salt
 * master.</li>
 * <li>SALT_USER and SALT_PASSWORD options must be configured and provided on the job.</li>
 * </ul>
 */
@Plugin(name = SaltApiNodeStepPlugin.SERVICE_PROVIDER_NAME, service = ServiceNameConstants.WorkflowNodeStep)
@PluginDescription(title = "Remote Salt Execution", description = "Run a command on a remote salt master through salt-api.")
public class SaltApiNodeStepPlugin implements NodeStepPlugin {
    public enum SaltApiNodeStepFailureReason implements FailureReason {
        EXIT_CODE, ARGUMENTS_MISSING, ARGUMENTS_INVALID, AUTHENTICATION_FAILURE, COMMUNICATION_FAILURE, SALT_API_FAILURE, SALT_TARGET_MISMATCH, INTERRUPTED;
    }

    public static final String SERVICE_PROVIDER_NAME = "salt-api-exec";

    protected static final String[] VALID_SALT_API_END_POINT_SCHEMES = { "http", "https" };

    protected static final String LOGIN_RESOURCE = "/login";
    protected static final String MINION_RESOURCE = "/minions";
    protected static final String JOBS_RESOURCE = "/jobs";
    protected static final String SALT_AUTH_TOKEN_HEADER = "X-Auth-Token";
    protected static final String CHAR_SET_ENCODING = "UTF-8";
    protected static final String REQUEST_CONTENT_TYPE = "application/x-www-form-urlencoded";
    protected static final String REQUEST_ACCEPT_HEADER_NAME = "Accept";
    protected static final String JSON_RESPONSE_ACCEPT_TYPE = "application/json";
    protected static final String YAML_RESPONSE_ACCEPT_TYPE = "application/x-yaml";

    protected static final String SALT_OUTPUT_RETURN_KEY = "return";
    protected static final Type MINION_RESPONSE_TYPE = new TypeToken<List<Map<String, Object>>>() {}.getType();
    protected static final Type JOB_RESPONSE_TYPE = new TypeToken<Map<String, List<Object>>>() {}.getType();

    // -- Parameter names for REST calls to salt-api --
    protected static final String SALT_API_FUNCTION_PARAM_NAME = "fun";
    protected static final String SALT_API_ARGUMENTS_PARAM_NAME = "arg";
    protected static final String SALT_API_TARGET_PARAM_NAME = "tgt";
    protected static final String SALT_API_USERNAME_PARAM_NAME = "username";
    protected static final String SALT_API_PASSWORD_PARAM_NAME = "password";
    protected static final String SALT_API_EAUTH_PARAM_NAME = "eauth";

    // -- Option names expected to be passed in from rundeck --
    protected static final String RUNDECK_DATA_CONTEXT_OPTION_KEY = "option";
    protected static final String SALT_API_END_POINT_OPTION_NAME = "SALT_API_END_POINT";
    protected static final String SALT_API_FUNCTION_OPTION_NAME = "Function";
    protected static final String SALT_API_EAUTH_OPTION_NAME = "SALT_API_EAUTH";
    protected static final String SALT_USER_OPTION_NAME = "SALT_USER";
    protected static final String SALT_PASSWORD_OPTION_NAME = "SALT_PASSWORD";

    protected long pollFrequency = 15000L;

    @PluginProperty(title = SALT_API_END_POINT_OPTION_NAME, description = "Salt Api end point", required = true, defaultValue = "${option."
            + SALT_API_END_POINT_OPTION_NAME + "}")
    protected String saltEndpoint;

    @PluginProperty(title = SALT_API_FUNCTION_OPTION_NAME, description = "Function (including args) to invoke on salt minions", required = true)
    protected String function;

    @PluginProperty(title = SALT_API_EAUTH_OPTION_NAME, description = "Salt Master's external authentication system", required = true, defaultValue = "${option."
            + SALT_API_EAUTH_OPTION_NAME + "}")
    protected String eAuth;

    @Autowired
    protected SaltReturnHandler defaultReturnHandler;

    @Autowired
    protected HttpFactory httpFactory;

    @Autowired
    protected SaltReturnHandlerRegistry returnHandlerRegistry;

    public SaltApiNodeStepPlugin() {
        new DependencyInjectionUtil().inject(this);
    }

    @Override
    public void executeNodeStep(PluginStepContext context, Map<String, Object> configuration, INodeEntry entry)
            throws NodeStepException {
        Map<String, String> optionData = context.getDataContext().get(RUNDECK_DATA_CONTEXT_OPTION_KEY);
        if (optionData == null) {
            throw new NodeStepException("Missing data context.", SaltApiNodeStepFailureReason.ARGUMENTS_MISSING,
                    entry.getNodename());
        }
        String user = optionData.get(SALT_USER_OPTION_NAME);
        String password = optionData.get(SALT_PASSWORD_OPTION_NAME);

        validate(user, password, entry);

        try {
            HttpClient client = httpFactory.createHttpClient();
            String authToken = authenticate(client, user, password);

            if (authToken == null) {
                throw new NodeStepException("Authentication failure",
                        SaltApiNodeStepFailureReason.AUTHENTICATION_FAILURE, entry.getNodename());
            }

            String dispatchedJid = submitJob(context, client, authToken, entry.getNodename());
            String jobOutput = waitForJidResponse(context, client, authToken, dispatchedJid, entry.getNodename());
            SaltReturnHandler handler = returnHandlerRegistry.getHandlerFor(function.split(" ", 2)[0],
                    defaultReturnHandler);
            SaltReturnResponse response = handler.extractResponse(jobOutput);

            for (String out : response.getStandardOutput()) {
                context.getLogger().log(Constants.INFO_LEVEL, out);
            }
            for (String err : response.getStandardError()) {
                context.getLogger().log(Constants.ERR_LEVEL, err);
            }
            if (!response.isSuccessful()) {
                throw new NodeStepException(String.format("Execution failed on minion with exit code %d",
                        response.getExitCode()), SaltApiNodeStepFailureReason.EXIT_CODE, entry.getNodename());
            }
        } catch (SaltReturnResponseParseException e) {
            throw new NodeStepException(e, SaltApiNodeStepFailureReason.SALT_API_FAILURE, entry.getNodename());
        } catch (InterruptedException e) {
            throw new NodeStepException(e, SaltApiNodeStepFailureReason.INTERRUPTED, entry.getNodename());
        } catch (SaltTargettingMismatchException e) {
            throw new NodeStepException(e, SaltApiNodeStepFailureReason.SALT_TARGET_MISMATCH, entry.getNodename());
        } catch (SaltApiException e) {
            throw new NodeStepException(e, SaltApiNodeStepFailureReason.SALT_API_FAILURE, entry.getNodename());
        } catch (HttpException e) {
            throw new NodeStepException(e, SaltApiNodeStepFailureReason.COMMUNICATION_FAILURE, entry.getNodename());
        } catch (IOException e) {
            throw new NodeStepException(e, SaltApiNodeStepFailureReason.COMMUNICATION_FAILURE, entry.getNodename());
        }
    }

    /**
     * Submits the job to salt-api using the class function and args.
     * 
     * @return the jid of the submitted job
     * @throws HttpException
     *             if there was a communication failure with salt-api
     */
    protected String submitJob(PluginStepContext context, HttpClient client, String authToken, String minionId)
            throws HttpException, IOException, SaltApiException, SaltTargettingMismatchException {
        List<NameValuePair> params = Lists.newArrayList();
        List<String> args = ArgumentParser.DEFAULT_ARGUMENT_SPLITTER.parse(function);
        params.add(new BasicNameValuePair(SALT_API_FUNCTION_PARAM_NAME, args.get(0)));
        params.add(new BasicNameValuePair(SALT_API_TARGET_PARAM_NAME, minionId));
        for (int i = 1; i < args.size(); i++) {
            params.add(new BasicNameValuePair(SALT_API_ARGUMENTS_PARAM_NAME, args.get(i)));
        }
        UrlEncodedFormEntity postEntity = new UrlEncodedFormEntity(params, CHAR_SET_ENCODING);
        postEntity.setContentEncoding(CHAR_SET_ENCODING);
        postEntity.setContentType(REQUEST_CONTENT_TYPE);

        HttpPost post = httpFactory.createHttpPost(saltEndpoint + MINION_RESOURCE);
        post.setHeader(SALT_AUTH_TOKEN_HEADER, authToken);
        post.setHeader(REQUEST_ACCEPT_HEADER_NAME, JSON_RESPONSE_ACCEPT_TYPE);
        post.setEntity(postEntity);
        HttpResponse response = client.execute(post);

        int statusCode = response.getStatusLine().getStatusCode();
        HttpEntity entity = response.getEntity();
        try {
            String entityResponse = EntityUtils.toString(entity);
            if (statusCode != HttpStatus.SC_ACCEPTED) {

                throw new HttpException(String.format("Expected response code %d, received %d. %s",
                        HttpStatus.SC_ACCEPTED, statusCode, entityResponse));
            } else {
                context.getLogger().log(Constants.DEBUG_LEVEL,
                        String.format("Received response for job submission = %s", response));
                Gson gson = new Gson();
                List<Map<String, Object>> responses = gson.fromJson(entityResponse, MINION_RESPONSE_TYPE);
                if (responses.size() != 1) {
                    throw new SaltApiException(String.format("Could not understand salt response %s", response));
                }
                Map<String, Object> responseMap = responses.get(0);
                SaltApiResponseOutput saltOutput = gson.fromJson(responseMap.get(SALT_OUTPUT_RETURN_KEY).toString(),
                        SaltApiResponseOutput.class);
                if (saltOutput.getMinions().size() != 1) {
                    throw new SaltTargettingMismatchException(String.format(
                            "Expected minion delegation count of 1, was %d. Full minion string: (%s)", saltOutput
                                    .getMinions().size(), saltOutput.getMinions()));
                } else if (!saltOutput.getMinions().contains(minionId)) {
                    throw new SaltTargettingMismatchException(String.format(
                            "Minion dispatch mis-match. Expected:%s,  was:%s", minionId, saltOutput.getMinions()
                                    .toString()));
                }
                return saltOutput.getJid();
            }
        } finally {
            EntityUtils.consumeQuietly(entity);
            post.releaseConnection();
        }
    }

    protected void validate(String user, String password, INodeEntry entry) throws SaltStepValidationException {
        checkNotEmpty(SALT_API_END_POINT_OPTION_NAME, saltEndpoint, SaltApiNodeStepFailureReason.ARGUMENTS_MISSING,
                entry);
        checkNotEmpty(SALT_API_FUNCTION_OPTION_NAME, function, SaltApiNodeStepFailureReason.ARGUMENTS_MISSING, entry);
        checkNotEmpty(SALT_API_EAUTH_OPTION_NAME, eAuth, SaltApiNodeStepFailureReason.ARGUMENTS_MISSING, entry);
        checkNotEmpty(SALT_USER_OPTION_NAME, user, SaltApiNodeStepFailureReason.ARGUMENTS_MISSING, entry);
        checkNotEmpty(SALT_PASSWORD_OPTION_NAME, password, SaltApiNodeStepFailureReason.ARGUMENTS_MISSING, entry);

        UrlValidator urlValidator = new UrlValidator(VALID_SALT_API_END_POINT_SCHEMES, UrlValidator.ALLOW_LOCAL_URLS);
        if (!urlValidator.isValid(saltEndpoint)) {
            throw new SaltStepValidationException(SALT_API_END_POINT_OPTION_NAME, String.format(
                    "%s is not a valid endpoint.", saltEndpoint), SaltApiNodeStepFailureReason.ARGUMENTS_INVALID,
                    entry.getNodename());
        }
    }

    protected String waitForJidResponse(PluginStepContext context, HttpClient client, String authToken, String jid,
            String minionId) throws IOException, InterruptedException, SaltApiException {
        do {
            String response = extractOutputForJid(context, client, authToken, jid, minionId);
            if (response != null) {
                return response;
            }
            Thread.sleep(pollFrequency);
            if (Thread.interrupted()) {
                Thread.currentThread().interrupt();
                return null;
            }
        } while (true);
    }

    /**
     * Extracts the minion job response by calling the job resource.
     * 
     * @return the host response or null if none is available encoded in json.
     * @throws SaltApiException
     *             if the salt-api response does not conform to the expected
     *             format.
     */
    protected String extractOutputForJid(PluginStepContext context, HttpClient client, String authToken, String jid,
            String minionId) throws IOException, SaltApiException {
        String jidResource = String.format("%s%s/%s", saltEndpoint, JOBS_RESOURCE, jid);
        HttpGet get = httpFactory.createHttpGet(jidResource);
        get.setHeader(SALT_AUTH_TOKEN_HEADER, authToken);
        get.setHeader(REQUEST_ACCEPT_HEADER_NAME, JSON_RESPONSE_ACCEPT_TYPE);
        HttpResponse response = client.execute(get);

        try {
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                HttpEntity entity = response.getEntity();
                String entityResponse = EntityUtils.toString(entity);
                context.getLogger().log(Constants.DEBUG_LEVEL,
                        String.format("Received response for jobs/%s = %s", jid, response));
                Gson gson = new Gson();
                Map<String, List<Map<String, Object>>> result = gson.fromJson(entityResponse, JOB_RESPONSE_TYPE);
                List<Map<String, Object>> responses = result.get(SALT_OUTPUT_RETURN_KEY);
                if (responses.size() > 1) {
                    throw new SaltApiException("Too many responses received: " + response);
                } else if (responses.size() == 1) {
                    Map<String, Object> minionResponse = responses.get(0);
                    if (minionResponse.containsKey(minionId)) {
                        Object responseObj = minionResponse.get(minionId);
                        return gson.toJson(responseObj);
                    }
                }
                return null;
            } else {
                return null;
            }
        } finally {
            EntityUtils.consumeQuietly(response.getEntity());
            get.releaseConnection();
        }
    }

    /**
     * Authenticates the given username/password with the given eauth system
     * against the salt-api endpoint
     * 
     * @param user
     *            The user to auth with
     * @param password
     *            The password for the given user
     * @return X-Auth-Token for use in subsequent requests
     */
    protected String authenticate(HttpClient client, String user, String password) throws IOException {
        List<NameValuePair> params = Lists.newArrayListWithCapacity(3);
        params.add(new BasicNameValuePair(SALT_API_USERNAME_PARAM_NAME, user));
        params.add(new BasicNameValuePair(SALT_API_PASSWORD_PARAM_NAME, password));
        params.add(new BasicNameValuePair(SALT_API_EAUTH_PARAM_NAME, eAuth));
        UrlEncodedFormEntity postEntity = new UrlEncodedFormEntity(params, CHAR_SET_ENCODING);
        postEntity.setContentEncoding(CHAR_SET_ENCODING);
        postEntity.setContentType(REQUEST_CONTENT_TYPE);

        HttpPost post = httpFactory.createHttpPost(saltEndpoint + LOGIN_RESOURCE);
        post.setEntity(postEntity);
        HttpResponse response = client.execute(post);

        try {
            int responseCode = response.getStatusLine().getStatusCode();
            /**
             * This commit changes the /login behaviour for salt-api
             * https://github.com/saltstack/salt-api/commit/b57b416ece9f6b2b54765d346cce3f5699381003
             */
            return
            // salt-api version <= 0.7.5 release response code
            responseCode == HttpStatus.SC_MOVED_TEMPORARILY ||
            // salt-api version > 0.7.5 release response code
                    responseCode == HttpStatus.SC_OK ? response.getHeaders(SALT_AUTH_TOKEN_HEADER)[0].getValue() : null;
        } finally {
            EntityUtils.consumeQuietly(response.getEntity());
            post.releaseConnection();
        }
    }
}
