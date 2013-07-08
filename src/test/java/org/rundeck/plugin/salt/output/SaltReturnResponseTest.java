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

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.rundeck.plugin.salt.output.SaltReturnResponse;

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
