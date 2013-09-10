package org.rundeck.plugin.salt.version;

import org.rundeck.plugin.salt.SaltApiException;
import org.rundeck.plugin.salt.output.SaltApiResponseOutput;

/**
 * Interaction points for dealing with backwards incompatible changes in salt-api
 */
public interface SaltInteractionHandler {
    
    /**
     * Invoked to deserialize a json response for /minions. The response should contain
     * 0 or 1 successful job executions.
     * 
     * @return the SaltApiResponseOutput corresponding to a successful invocation
     * @throws SaltApiException if no response is found. 
     */
    SaltApiResponseOutput extractOutputForJobSubmissionResponse(String json) throws SaltApiException;
}
