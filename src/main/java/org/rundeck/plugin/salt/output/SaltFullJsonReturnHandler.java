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
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

/**
* Handler for generating {@link SaltReturnResponse} from minion json responses.
*/
public class SaltFullJsonReturnHandler implements SaltReturnHandler {

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
        return "SaltFullJsonReturnHandler [exitCodeKey=" + exitCodeKey + ", standardOutputKey=" + standardOutputKey
                + ", standardErrorKey=" + standardErrorKey + "]";
    }

    /**
     * Deserializes a {@link SaltReturnResponse} from a salt minion json response using
     * the specified exit code, standard output, and standard error keys.
     * 
     * @param rawResponse
     *            a minion's full json response.
     * 
     * @throws SaltReturnResponseParseException
     *             if there was an error interpreting the response
     */
    @Override
    public SaltReturnResponse extractResponse(String rawResponse) throws SaltReturnResponseParseException {
        try {
            Gson gson = new Gson();
	    Object result = gson.fromJson(rawResponse, Object.class);
            SaltReturnResponse response = new SaltReturnResponse();

            if (exitCodeKey != null) {
	        List<String> results=getValues(result, exitCodeKey);
		if (results.size() == 0) {
		    throw new SaltReturnResponseParseException("No " + exitCodeKey + " attribute in JSON output");
		}
		Integer exitCode=new Integer(0);
		for (String tmp : results) {
		if (!"true".equalsIgnoreCase(tmp))
		    exitCode=1;
		}
		response.setExitCode(exitCode);
            }

            if (standardOutputKey != null) {
	        List<String> results=getValues(result, standardOutputKey);
		if (results.size() == 0) {
		   throw new SaltReturnResponseParseException("No " + standardOutputKey + " attribute in JSON output");
		}
		response.addOutput(results.toString());
            }

            if (standardErrorKey != null) {
		List<String> results=getValues(result, standardErrorKey);			
		if (results.size() == 0) {
		    throw new SaltReturnResponseParseException("No " + standardErrorKey + " attribute in JSON output");
		}			
		response.addError(results.toString());
            }
     
            return response; 
        } catch (JsonSyntaxException e) {
            throw new SaltReturnResponseParseException(e);
        }
    }

    private List getValues(Object object, String attribute) {
        ArrayList<String> attributeValues = new ArrayList<String>();
	if (object instanceof Map) {
  	    Map map = (Map) object;
	    for ( String key : (Set<String>)map.keySet() ) { 
	        if (attribute.equalsIgnoreCase(key)) {
		    attributeValues.add(map.get(key).toString());
	        } 
            }
	    Collection values = map.values();
	    for (Object value : values)
	        attributeValues.addAll(getValues(value, attribute));
	    }
	else if (object instanceof Collection) {
	    Collection values = (Collection) object;
 	     for (Object value : values) {
	         attributeValues.addAll(getValues(value, attribute));
	     }
	}
        return attributeValues;
    }	
}
