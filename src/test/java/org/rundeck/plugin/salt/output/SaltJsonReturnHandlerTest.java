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
