package com.salesforce.rundeck.plugin.output;

import java.lang.reflect.Type;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

/**
 * Handler for generating {@link SaltReturnResponse} from minion json responses.
 */
public class SaltJsonReturnHandler implements SaltReturnHandler {

    protected static final Type RETURN_RESPONSE_TYPE = new TypeToken<Map<String, String>>() {}.getType();

    protected String exitCodeKey;
    protected String standardOutputKey;
    protected String standardErrorKey;

    public void setExitCodeKey(String exitCodeKey) {
        this.exitCodeKey = exitCodeKey;
    }

    public void setStandardOutputKey(String standardOutputKey) {
        this.standardOutputKey = standardOutputKey;
    }

    public void setStandardErrorKey(String standardErrorKey) {
        this.standardErrorKey = standardErrorKey;
    }

    @Override
    public String toString() {
        return "SaltJsonReturnHandler [exitCodeKey=" + exitCodeKey + ", standardOutputKey=" + standardOutputKey
                + ", standardErrorKey=" + standardErrorKey + "]";
    }

    /**
     * Deserializes a {@link SaltReturnResponse} from a salt minion json response using
     * the specified exit code, standard output, and standard error keys.
     * 
     * @param rawResponse
     *            a minion's json response.
     * 
     * @throws SaltReturnResponseParseException
     *             if there was an error interpreting the response
     */
    @Override
    public SaltReturnResponse extractResponse(String rawResponse) throws SaltReturnResponseParseException {
        try {
            Gson gson = new Gson();
            Map<String, String> result = gson.fromJson(rawResponse, RETURN_RESPONSE_TYPE);
            SaltReturnResponse response = new SaltReturnResponse();

            if (exitCodeKey != null) {
                String exitCodeStringValue = extractOrDie(exitCodeKey, result);
                Integer exitCode = Double.valueOf(exitCodeStringValue).intValue();
                response.setExitCode(exitCode);
            }

            if (standardOutputKey != null) {
                String output = extractOrDie(standardOutputKey, result);
                response.addOutput(output);
            }

            if (standardErrorKey != null) {
                String error = extractOrDie(standardErrorKey, result);
                response.addError(error);
            }

            return response;
        } catch (JsonSyntaxException e) {
            throw new SaltReturnResponseParseException(e);
        }
    }

    protected String extractOrDie(String key, Map<String, String> data) throws SaltReturnResponseParseException {
        if (!data.containsKey(key)) {
            throw new SaltReturnResponseParseException(String.format("Expected key %s in %s, found none.", key, data));
        } else {
            return data.get(key);
        }
    }
}
