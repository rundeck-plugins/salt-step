package org.rundeck.plugin.salt.version;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import org.rundeck.plugin.salt.SaltApiException;
import org.rundeck.plugin.salt.output.SaltApiResponseOutput;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * The interaction handler responsible for dealing with salt-api responses pre
 * 0.8.2
 * 
 * Backwards incompatible end points: - POST to /minions
 */
public class Pre082SaltInteractionHandler implements SaltInteractionHandler {
    
    protected static final String SALT_OUTPUT_RETURN_KEY = "return";
    protected static final Type MINION_RESPONSE_TYPE = new TypeToken<List<Map<String, Object>>>() {}.getType();
    
    @Override
    public SaltApiResponseOutput extractOutputForJobSubmissionResponse(String json) throws SaltApiException {
        /**
         * The response looks like: 
         * [ { "return": { "jid" : "<jid>", "minions" : ["host1", "host2"] } } ]
         */
        Gson gson = new Gson();
        List<Map<String, Object>> responses = gson.fromJson(json, MINION_RESPONSE_TYPE);
        if (responses.size() != 1) {
            throw new SaltApiException(String.format("Could not understand salt response %s", json));
        }
        Map<String, Object> responseMap = responses.get(0);
        return gson.fromJson(responseMap.get(SALT_OUTPUT_RETURN_KEY).toString(), SaltApiResponseOutput.class);
    }
}
