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

import junit.framework.Assert;

import org.junit.Test;
import org.rundeck.plugin.salt.output.SaltJsonReturnHandler;
import org.rundeck.plugin.salt.output.SaltReturnResponse;
import org.rundeck.plugin.salt.output.SaltReturnResponseParseException;

import com.google.common.collect.Maps;

public class SaltJsonReturnHandlerTest {

    protected static final String EXIT_CODE_KEY = "retcode";
    protected static final String ERR_KEY = "stderr";
    protected static final String OUT_KEY = "stdout";
    protected static final String SAMPLE_JSON_TEMPLATE = "{\"pid\": 18347, \"" + EXIT_CODE_KEY + "\":%d, \"" + OUT_KEY
            + "\":\"%s\",\"" + ERR_KEY + "\":\"%s\"}";

    @Test
    public void testExtractResponse() {
        SaltJsonReturnHandler handler = new SaltJsonReturnHandler();
        handler.setExitCodeKey(EXIT_CODE_KEY);
        handler.setStandardOutputKey(OUT_KEY);
        handler.setStandardErrorKey(ERR_KEY);

        Integer exitCode = 127;
        String out = "some output";
        String err = "some error";
        String json = String.format(SAMPLE_JSON_TEMPLATE, exitCode, out, err);
        SaltReturnResponse response = handler.extractResponse(json);
        Assert.assertEquals("Expected passed in exit code", exitCode, response.getExitCode());
        Assert.assertEquals("Expected single stdout line.", 1, response.getStandardOutput().size());
        Assert.assertEquals("Expected passed in stdout line", out, response.getStandardOutput().get(0));
        Assert.assertEquals("Expected single stderr line", 1, response.getStandardError().size());
        Assert.assertEquals("Expected passed in stderr line", err, response.getStandardError().get(0));
    }
    
    @Test
    public void testExtractResponseWithoutKeys() {
        SaltJsonReturnHandler handler = new SaltJsonReturnHandler();
        
        Integer exitCode = 127;
        String out = "some output";
        String err = "some error";
        String json = String.format(SAMPLE_JSON_TEMPLATE, exitCode, out, err);
        SaltReturnResponse response = handler.extractResponse(json);
        Assert.assertNull("Expected not to parse exit code", response.getExitCode());
        Assert.assertTrue("Expected not to parse stdout", response.getStandardOutput().isEmpty());
        Assert.assertTrue("Expected not to parse stderr", response.getStandardError().isEmpty());
    }
    
    @Test(expected = SaltReturnResponseParseException.class)
    public void testExtractResponseExitCodeKeySpecifiedButNoExitCode() {
        SaltJsonReturnHandler handler = new SaltJsonReturnHandler();
        handler.setExitCodeKey("someotherkey");
        handler.setStandardOutputKey(OUT_KEY);
        handler.setStandardErrorKey(ERR_KEY);
        
        Integer exitCode = 127;
        String out = "some output";
        String err = "some error";
        String json = String.format(SAMPLE_JSON_TEMPLATE, exitCode, out, err);
        handler.extractResponse(json);
    }
    
    @Test(expected = SaltReturnResponseParseException.class)
    public void testExtractResponseStandardOutputKeySpecifiedButNoStandardOutput() {
        SaltJsonReturnHandler handler = new SaltJsonReturnHandler();
        handler.setExitCodeKey(EXIT_CODE_KEY);
        handler.setStandardOutputKey("someotherkey");
        handler.setStandardErrorKey(ERR_KEY);
        
        Integer exitCode = 127;
        String out = "some output";
        String err = "some error";
        String json = String.format(SAMPLE_JSON_TEMPLATE, exitCode, out, err);
        handler.extractResponse(json);
    }
    
    @Test(expected = SaltReturnResponseParseException.class)
    public void testExtractResponseStandardErrorKeySpecifiedButNoStandardError() {
        SaltJsonReturnHandler handler = new SaltJsonReturnHandler();
        handler.setExitCodeKey(EXIT_CODE_KEY);
        handler.setStandardOutputKey(OUT_KEY);
        handler.setStandardErrorKey("someotherkey");
        
        Integer exitCode = 127;
        String out = "some output";
        String err = "some error";
        String json = String.format(SAMPLE_JSON_TEMPLATE, exitCode, out, err);
        handler.extractResponse(json);
    }

    @Test
    public void testExtractOrDieExtractsProperly() {
        SaltJsonReturnHandler handler = new SaltJsonReturnHandler();
        Map<String, String> data = Maps.newHashMap();
        String key = "key";
        String value = "value";
        data.put(key, value);
        Assert.assertEquals("Expected handler to extract keyed value", value, handler.extractOrDie(key, data));
    }

    @Test(expected = SaltReturnResponseParseException.class)
    public void testExtractOrDieDies() {
        SaltJsonReturnHandler handler = new SaltJsonReturnHandler();
        Map<String, String> data = Maps.newHashMap();
        handler.extractOrDie("key", data);
    }
}
