package com.salesforce.rundeck.plugin.output;

import org.junit.Assert;
import org.junit.Test;

public class DefaultSaltReturnHandlerTest {

    @Test
    public void testConstructor() {
        DefaultSaltReturnHandler handler = new DefaultSaltReturnHandler();
        Assert.assertEquals(0, handler.exitCode.intValue());
    }

    @Test
    public void testConstructorExitCode() {
        Integer exitCode = 127;
        DefaultSaltReturnHandler handler = new DefaultSaltReturnHandler(exitCode);
        Assert.assertEquals(exitCode, handler.exitCode);
    }

    @Test
    public void testSetExitCode() {
        Integer exitCode = 127;
        DefaultSaltReturnHandler handler = new DefaultSaltReturnHandler();
        handler.setExitCode(exitCode);
        Assert.assertEquals(exitCode, handler.exitCode);
    }

    @Test
    public void testExtractResponse() {
        SaltReturnHandler handler = new DefaultSaltReturnHandler();
        String rawResponse = "some random response";
        SaltReturnResponse response = handler.extractResponse(rawResponse);
        Assert.assertTrue(response.isSuccessful());
        Assert.assertEquals(0, response.getExitCode().intValue());
        Assert.assertEquals(1, response.getStandardOutput().size());
        Assert.assertEquals(rawResponse, response.getStandardOutput().get(0));
        Assert.assertTrue(response.getStandardError().isEmpty());
    }

    @Test
    public void testExtractResponseUsesExitCode() {
        Integer exitCode = 127;
        SaltReturnHandler handler = new DefaultSaltReturnHandler(exitCode);
        String rawResponse = "some random response";
        SaltReturnResponse response = handler.extractResponse(rawResponse);
        Assert.assertEquals(exitCode, response.getExitCode());
        Assert.assertEquals(1, response.getStandardOutput().size());
        Assert.assertEquals(rawResponse, response.getStandardOutput().get(0));
        Assert.assertTrue(response.getStandardError().isEmpty());
    }

    @Test
    public void testExtractResponseWithNullInput() {
        SaltReturnHandler handler = new DefaultSaltReturnHandler();
        SaltReturnResponse response = handler.extractResponse(null);
        Assert.assertTrue(response.isSuccessful());
        Assert.assertEquals(0, response.getExitCode().intValue());
        Assert.assertTrue(response.getStandardOutput().isEmpty());
        Assert.assertTrue(response.getStandardError().isEmpty());
    }
}
