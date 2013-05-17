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

import org.junit.Assert;
import org.junit.Test;
import org.rundeck.plugin.salt.output.DefaultSaltReturnHandler;
import org.rundeck.plugin.salt.output.SaltReturnHandler;
import org.rundeck.plugin.salt.output.SaltReturnResponse;

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
