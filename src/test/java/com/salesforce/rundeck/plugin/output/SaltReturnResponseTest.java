package com.salesforce.rundeck.plugin.output;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class SaltReturnResponseTest {

    @Test
    public void testAddOutput() {
        SaltReturnResponse response = new SaltReturnResponse();
        String output1 = "line 1 of output";
        String output2 = "line 2 of output";
        response.addOutput(output1);
        response.addOutput(output2);

        List<String> output = response.getStandardOutput();
        Assert.assertEquals("Expected passed in stdout lines", 2, output.size());
        Assert.assertEquals("Expected output line in order", output1, output.get(0));
        Assert.assertEquals("Expected output line in order", output2, output.get(1));
    }
    
    @Test
    public void testAddEmptyOutput() {
        SaltReturnResponse response = new SaltReturnResponse();
        response.addOutput(null);
        response.addOutput("");

        Assert.assertTrue("Expected empty line to be filtered out.", response.getStandardOutput().isEmpty());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetOutputIsUnmodifiable() {
        new SaltReturnResponse().getStandardOutput().add("some string");
    }

    @Test
    public void testAddError() {
        SaltReturnResponse response = new SaltReturnResponse();
        String error1 = "line 1 of error";
        String error2 = "line 2 of error";
        response.addError(error1);
        response.addError(error2);

        List<String> error = response.getStandardError();
        Assert.assertEquals("Expected passed in stderr lines", 2, error.size());
        Assert.assertEquals("Expected error line in order", error1, error.get(0));
        Assert.assertEquals("Expected error line in order", error2, error.get(1));
    }
    
    @Test
    public void testAddEmptyError() {
        SaltReturnResponse response = new SaltReturnResponse();
        response.addError(null);
        response.addError("");

        Assert.assertTrue("Expected empty line to be filtered out.", response.getStandardError().isEmpty());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetErrorIsUnmodifiable() {
        new SaltReturnResponse().getStandardError().add("some string");
    }

    @Test
    public void testSetExitCode() {
        SaltReturnResponse response = new SaltReturnResponse();
        response.setExitCode(0);
        Assert.assertEquals(0, response.getExitCode().intValue());
    }

    @Test
    public void testIsSuccesfulUnset() {
        SaltReturnResponse response = new SaltReturnResponse();
        Assert.assertFalse(response.isSuccessful());
    }
    
    @Test
    public void testIsSuccesfulNonZero() {
        SaltReturnResponse response = new SaltReturnResponse();
        response.setExitCode(127);
        Assert.assertFalse(response.isSuccessful());
    }
    
    @Test
    public void testIsSuccesfulZero() {
        SaltReturnResponse response = new SaltReturnResponse();
        response.setExitCode(0);
        Assert.assertTrue(response.isSuccessful());
    }
}
