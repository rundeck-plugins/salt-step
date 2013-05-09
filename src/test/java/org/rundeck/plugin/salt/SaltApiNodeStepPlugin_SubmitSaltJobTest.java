package org.rundeck.plugin.salt;

import junit.framework.Assert;

import org.apache.http.HttpException;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.rundeck.plugin.salt.SaltApiException;
import org.rundeck.plugin.salt.SaltApiNodeStepPlugin;
import org.rundeck.plugin.salt.SaltTargettingMismatchException;

import com.google.common.base.Predicate;

public class SaltApiNodeStepPlugin_SubmitSaltJobTest extends AbstractSaltApiNodeStepPluginTest {

    protected static final String MINIONS_ENDPOINT = String.format("%s/minions", PARAM_ENDPOINT);
    protected static final String MINION_JSON_RESPONSE = String.format(
            "[{\"return\": {\"jid\": \"%s\", \"minions\": [\"%s\"]}}]", OUTPUT_JID, PARAM_MINION_NAME);

    @Before
    public void setup() throws Exception {
        spyPlugin();
    }

    @Test
    public void testSubmitJob() throws Exception {
        setupResponse(post, HttpStatus.SC_ACCEPTED, MINION_JSON_RESPONSE);

        Assert.assertEquals("Expected mocked jid after submitting job", OUTPUT_JID,
                plugin.submitJob(client, AUTH_TOKEN, PARAM_MINION_NAME));

        assertThatSubmitSaltJobAttemptedSuccessfully();
    }

    @Test
    public void testSubmitJobWithArgs() throws Exception {
        setupResponse(post, HttpStatus.SC_ACCEPTED, MINION_JSON_RESPONSE);

        String arg1 = "sdf%33&";
        String arg2 = "adsf asdf";
        plugin.function = String.format("%s \"%s\" \"%s\"", PARAM_FUNCTION, arg1, arg2);

        Assert.assertEquals("Expected mocked jid after submitting job", OUTPUT_JID,
                plugin.submitJob(client, AUTH_TOKEN, PARAM_MINION_NAME));

        assertThatSubmitSaltJobAttemptedSuccessfully("fun=%s&tgt=%s&arg=%s&arg=%s", PARAM_FUNCTION, PARAM_MINION_NAME,
                arg1, arg2);
    }

    @Test
    public void testSubmitJobResponseCodeError() throws Exception {
        setupResponse(post, HttpStatus.SC_TEMPORARY_REDIRECT, MINION_JSON_RESPONSE);

        try {
            plugin.submitJob(client, AUTH_TOKEN, PARAM_MINION_NAME);
            Assert.fail("Expected http exception due to bad response code.");
        } catch (HttpException e) {
            // expected
        }

        assertThatSubmitSaltJobAttemptedSuccessfully();
    }

    @Test
    public void testSubmitJobNoMinionsMatched() throws Exception {
        String output = "[{\"return\": {}}]";
        setupResponse(post, HttpStatus.SC_ACCEPTED, output);

        try {
            plugin.submitJob(client, AUTH_TOKEN, PARAM_MINION_NAME);
            Assert.fail("Expected targetting mismatch exception.");
        } catch (SaltTargettingMismatchException e) {
            // expected
        }

        assertThatSubmitSaltJobAttemptedSuccessfully();
    }

    @Test
    public void testSubmitJobMinionCountMismatch() throws Exception {
        String output = String.format("[{\"return\": {\"jid\": \"%s\", \"minions\": []}}]", OUTPUT_JID);
        setupResponse(post, HttpStatus.SC_ACCEPTED, output);

        try {
            plugin.submitJob(client, AUTH_TOKEN, PARAM_MINION_NAME);
            Assert.fail("Expected targetting mismatch exception.");
        } catch (SaltTargettingMismatchException e) {
            // expected
        }

        assertThatSubmitSaltJobAttemptedSuccessfully();
    }

    @Test
    public void testSubmitJobMultipleResponses() throws Exception {
        String output = String.format("[{\"return\": {\"jid\": \"%s\", \"minions\": [\"SomeOtherMinion\"]}},{}]",
                OUTPUT_JID);
        setupResponse(post, HttpStatus.SC_ACCEPTED, output);

        try {
            plugin.submitJob(client, AUTH_TOKEN, PARAM_MINION_NAME);
            Assert.fail("Expected salt-api response exception.");
        } catch (SaltApiException e) {
            // expected
        }

        assertThatSubmitSaltJobAttemptedSuccessfully();
    }

    @Test
    public void testSubmitJobMinionIdMismatch() throws Exception {
        String output = String.format("[{\"return\": {\"jid\": \"%s\", \"minions\": [\"SomeOtherMinion\"]}}]",
                OUTPUT_JID);
        setupResponse(post, HttpStatus.SC_ACCEPTED, output);

        try {
            plugin.submitJob(client, AUTH_TOKEN, PARAM_MINION_NAME);
            Assert.fail("Expected targetting mismatch exception.");
        } catch (SaltTargettingMismatchException e) {
            // expected
        }

        assertThatSubmitSaltJobAttemptedSuccessfully();
    }

    protected void assertThatSubmitSaltJobAttemptedSuccessfully() {
        assertThatSubmitSaltJobAttemptedSuccessfully("fun=%s&tgt=%s", PARAM_FUNCTION, PARAM_MINION_NAME);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected void assertThatSubmitSaltJobAttemptedSuccessfully(String template, String... args) {
        try {
            Assert.assertEquals("Expected correct job submission endpoint to be used", MINIONS_ENDPOINT, post.getURI()
                    .toString());
            assertPostBody(template, args);
            Mockito.verify(post, Mockito.times(1)).setHeader(SaltApiNodeStepPlugin.SALT_AUTH_TOKEN_HEADER, AUTH_TOKEN);
            Mockito.verify(post, Mockito.times(1)).setHeader(SaltApiNodeStepPlugin.REQUEST_ACCEPT_HEADER_NAME,
                    SaltApiNodeStepPlugin.JSON_RESPONSE_ACCEPT_TYPE);
            ArgumentCaptor<Predicate> captor = ArgumentCaptor.forClass(Predicate.class);
            Mockito.verify(retryingExecutor, Mockito.times(1)).execute(Mockito.same(log), Mockito.same(client),
                    Mockito.same(post), Mockito.eq(plugin.numRetries), captor.capture());
            Predicate<Integer> submitJobPredicate = captor.getValue();
            // Need this predicate to return always false.
            for (int i = 400; i < 600; i++) {
                Assert.assertFalse("Expected submitJob predicate to always return false.", submitJobPredicate.apply(i));
            }

            Mockito.verify(plugin, Mockito.times(1)).closeResource(Mockito.same(responseEntity));
            Mockito.verify(post, Mockito.times(1)).releaseConnection();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
