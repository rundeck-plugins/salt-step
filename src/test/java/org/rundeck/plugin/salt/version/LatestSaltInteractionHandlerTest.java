package org.rundeck.plugin.salt.version;

import org.junit.Assert;
import org.junit.Test;
import org.rundeck.plugin.salt.SaltApiException;
import org.rundeck.plugin.salt.output.SaltApiResponseOutput;

public class LatestSaltInteractionHandlerTest {

    protected SaltInteractionHandler handler = new LatestSaltInteractionHandler();
    
    @Test
    public void testExtractOutputForJobSubmissionResponse() throws Exception {
        String jid = "20130903200912838566";
        String host1 = "host1";
        String host2 = "host2";
        String template = "{\"_links\": {\"jobs\": [{\"href\": \"/jobs/%s\"}]}, \"return\": [{\"jid\": \"%s\", \"minions\": [\"%s\", \"%s\"]}]}";
        String response = String.format(template, jid, jid, host1, host2);
        SaltApiResponseOutput output = handler.extractOutputForJobSubmissionResponse(response);
        Assert.assertEquals("Expected jid to match", jid, output.getJid());
        Assert.assertEquals("Expected 2 hosts", 2, output.getMinions().size());
        Assert.assertTrue("Expected to contain host1", output.getMinions().contains(host1));
        Assert.assertTrue("Expected to contain host2", output.getMinions().contains(host2));
    }
    
    @Test(expected = SaltApiException.class)
    public void testExtractOutputForJobSubmissionResponseEmptyResponse() throws Exception {
        String response = "{\"_links\": {\"jobs\": [{\"href\": \"/jobs/123\"}]}, \"return\": []}";
        handler.extractOutputForJobSubmissionResponse(response);
    }
    

    @Test(expected = SaltApiException.class)
    public void testExtractOutputForJobSubmissionResponseMultipleResponses() throws Exception {
        String response = "{\"_links\": {\"jobs\": [{\"href\": \"/jobs/123\"}]}, \"return\": [{}, {}]}";
        handler.extractOutputForJobSubmissionResponse(response);
    }
}
