package org.rundeck.plugin.salt.version;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import org.rundeck.plugin.salt.SaltApiException;
import org.rundeck.plugin.salt.output.SaltApiResponseOutput;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * The latest incarnation of the interaction handler.
 */
public class LatestSaltInteractionHandler implements SaltInteractionHandler {

    protected static final String SALT_OUTPUT_RETURN_KEY = "return";
    protected static final Type MINION_RESPONSE_TYPE = new TypeToken<Map<String, Object>>() {}.getType();
    protected static final Type LIST_OF_SALT_API_RESPONSE_TYPE = new TypeToken<List<SaltApiResponseOutput>>() {}.getType();

    @Override
    public SaltApiResponseOutput extractOutputForJobSubmissionResponse(String json) throws SaltApiException {
        /**
         * The response currently looks like: {"_links": {"jobs": [{"href":
         * "/jobs/20130903200912838566"}]}, "return": [{"jid":
         * "20130903200912838566", "minions": ["host1", "host2"]}]}
         */
        Gson gson = new Gson();
        Map<String, Object> responses = gson.fromJson(json, MINION_RESPONSE_TYPE);
        List<SaltApiResponseOutput> saltOutputs = gson.fromJson(responses.get(SALT_OUTPUT_RETURN_KEY).toString(),
                                                                LIST_OF_SALT_API_RESPONSE_TYPE);
        if (saltOutputs.size() != 1) {
            throw new SaltApiException(String.format("Could not understand salt response %s", json));
        }
        
        return saltOutputs.get(0);
    }
}
