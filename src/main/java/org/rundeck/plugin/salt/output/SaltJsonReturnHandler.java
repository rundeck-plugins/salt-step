/**
 * Copyright (c) 2013, salesforce.com, inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided
 * that the following conditions are met:
 *
 *    Redistributions of source code must retain the above copyright notice, this list of conditions and the
 *    following disclaimer.
 *
 *    Redistributions in binary form must reproduce the above copyright notice, this list of conditions and
 *    the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 *    Neither the name of salesforce.com, inc. nor the names of its contributors may be used to endorse or
 *    promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package org.rundeck.plugin.salt.output;

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
