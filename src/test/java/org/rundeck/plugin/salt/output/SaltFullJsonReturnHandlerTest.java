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

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Maps;

public class SaltFullJsonReturnHandlerTest {

    protected static final String EXIT_CODE_KEY = "result";
    protected static final String ERR_KEY = "comment";
    protected static final String OUT_KEY = "comment";
    protected static final String SAMPLE_JSON_TEMPLATE = "{\"service_\":{\"" + OUT_KEY + "\":\"%s\",\"start_time\":\"10:03:59.046156\",\"" + EXIT_CODE_KEY + "\":%s,\"duration\":144.388\"}}";

    @Test
    public void testExtractResponse() {
        SaltFullJsonReturnHandler handler = new SaltFullJsonReturnHandler();
        handler.setExitCodeKey(EXIT_CODE_KEY);
        handler.setStandardOutputKey(OUT_KEY);
        handler.setStandardErrorKey(ERR_KEY);

        String result = "true";
        String comment = "some comment";
        String json = String.format(SAMPLE_JSON_TEMPLATE, comment, result, comment, result);
        SaltReturnResponse response = handler.extractResponse(json);
        Assert.assertEquals("Expected passed in exit code", new Integer(0), response.getExitCode());
        Assert.assertEquals("Expected single stdout line", 1, response.getStandardOutput().size());
        Assert.assertEquals("Expected passed in stdout line", "[" + comment + "]", response.getStandardOutput().get(0));
        Assert.assertEquals("Expected single stderr line", 1, response.getStandardError().size());
        Assert.assertEquals("Expected passed in stderr line", "[" + comment + "]", response.getStandardError().get(0));
    }
    
    @Test
    public void testExtractResponseWithoutKeys() {
        SaltFullJsonReturnHandler handler = new SaltFullJsonReturnHandler();
        
        String result = "true";
        String comment = "some comment";
        String json = String.format(SAMPLE_JSON_TEMPLATE, comment, result, comment, result);
        SaltReturnResponse response = handler.extractResponse(json);
        Assert.assertNull("Expected not to parse exit code", response.getExitCode());
        Assert.assertTrue("Expected not to parse stdout", response.getStandardOutput().isEmpty());
        Assert.assertTrue("Expected not to parse stderr", response.getStandardError().isEmpty());
    }
    
    @Test(expected = SaltReturnResponseParseException.class)
    public void testExtractResponseExitCodeKeySpecifiedButNoExitCode() {
        SaltFullJsonReturnHandler handler = new SaltFullJsonReturnHandler();
        handler.setExitCodeKey("someotherkey");
        handler.setStandardOutputKey(OUT_KEY);
        handler.setStandardErrorKey(ERR_KEY);
        
        String result = "true";
        String comment = "some comment";
        String json = String.format(SAMPLE_JSON_TEMPLATE, comment, result, comment, result);
        handler.extractResponse(json);
    }
    
    @Test(expected = SaltReturnResponseParseException.class)
    public void testExtractResponseStandardOutputKeySpecifiedButNoStandardOutput() {
        SaltFullJsonReturnHandler handler = new SaltFullJsonReturnHandler();
        handler.setExitCodeKey(EXIT_CODE_KEY);
        handler.setStandardOutputKey("someotherkey");
        handler.setStandardErrorKey(ERR_KEY);
        
        String result = "true";
        String comment = "some comment";
        String json = String.format(SAMPLE_JSON_TEMPLATE, comment, result, comment, result);
        handler.extractResponse(json);
    }
    
    @Test(expected = SaltReturnResponseParseException.class)
    public void testExtractResponseStandardErrorKeySpecifiedButNoStandardError() {
        SaltFullJsonReturnHandler handler = new SaltFullJsonReturnHandler();
        handler.setExitCodeKey(EXIT_CODE_KEY);
        handler.setStandardOutputKey(OUT_KEY);
        handler.setStandardErrorKey("someotherkey");
        
        String result = "true";
        String comment = "some comment";
        String json = String.format(SAMPLE_JSON_TEMPLATE, comment, result, comment, result);
        handler.extractResponse(json);
    }
}
