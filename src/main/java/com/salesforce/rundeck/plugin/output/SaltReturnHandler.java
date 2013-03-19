package com.salesforce.rundeck.plugin.output;

/**
 * Handler for generating {@link SaltReturnResponse} from minion responses.
 */
public interface SaltReturnHandler {
    /**
     * Deserializes a {@link SaltReturnResponse} from a salt minion response.
     * 
     * @param rawResponse
     *            a minion's response.
     * 
     * @throws SaltReturnResponseParseException
     *             if there was an error interpreting the response
     */
    SaltReturnResponse extractResponse(String rawResponse) throws SaltReturnResponseParseException;
}
