package com.salesforce.rundeck.plugin.output;

import org.junit.Assert;
import org.junit.Test;

public class DefaultSaltReturnHandlerTest {

    @Test
    public void testConstructor() {
        DefaultSaltReturnHandler handler = new DefaultSaltReturnHandler();
        Assert.assertEquals("Didn't get default response code", 0, handler.exitCode.intValue());
    }

    @Test
    public void testConstructorExitCode() {
        Integer exitCode = 127;
        DefaultSaltReturnHandler handler = new DefaultSaltReturnHandler(exitCode);
        Assert.assertEquals("Didn't get passed in response code", exitCode, handler.exitCode);
    }

    @Test
    public void testSetExitCode() {
        Integer exitCode = 127;
        DefaultSaltReturnHandler handler = new DefaultSaltReturnHandler();
        handler.setExitCode(exitCode);
        Assert.assertEquals("Didn't get explicity set response code", exitCode, handler.exitCode);
    }

    @Test
    public void testExtractResponse() {
        SaltReturnHandler handler = new DefaultSaltReturnHandler();
        String rawResponse = "some random response";
        SaltReturnResponse response = handler.extractResponse(rawResponse);
        Assert.assertTrue("Didn't interpret default response code as success", response.isSuccessful());
        Assert.assertEquals("Didn't get default response code", 0, response.getExitCode().intValue());
        Assert.assertEquals("Didn't get exactly one stdout line", 1, response.getStandardOutput().size());
        Assert.assertEquals("Stdout line didn't match expected input", rawResponse, response.getStandardOutput().get(0));
        Assert.assertTrue("Expected stderr to be empty", response.getStandardError().isEmpty());
    }

    @Test
    public void testExtractResponseUsesExitCode() {
        Integer exitCode = 127;
        SaltReturnHandler handler = new DefaultSaltReturnHandler(exitCode);
        String rawResponse = "some random response";
        SaltReturnResponse response = handler.extractResponse(rawResponse);
        Assert.assertEquals("Didn't get passed in response code", exitCode, response.getExitCode());
        Assert.assertEquals("Didn't get exactly one stdout line", 1, response.getStandardOutput().size());
        Assert.assertEquals("Stdout line didn't match expected input", rawResponse, response.getStandardOutput().get(0));
        Assert.assertTrue("Expected stderr to be empty", response.getStandardError().isEmpty());
    }

    @Test
    public void testExtractResponseWithNullInput() {
        SaltReturnHandler handler = new DefaultSaltReturnHandler();
        SaltReturnResponse response = handler.extractResponse(null);
        Assert.assertTrue("Didn't interpret default response code as success", response.isSuccessful());
        Assert.assertEquals("Didn't get default response code", 0, response.getExitCode().intValue());
        Assert.assertTrue("Expected stdout to be empty", response.getStandardOutput().isEmpty());
        Assert.assertTrue("Expected stderr to be empty", response.getStandardError().isEmpty());
    }
}
