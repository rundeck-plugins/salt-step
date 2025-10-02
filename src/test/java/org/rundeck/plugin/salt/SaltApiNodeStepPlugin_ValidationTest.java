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

package org.rundeck.plugin.salt;

import org.junit.Assert;
import org.junit.Test;
import org.rundeck.plugin.salt.SaltApiNodeStepPlugin.SaltApiNodeStepFailureReason;
import org.rundeck.plugin.salt.validation.SaltStepValidationException;

import com.dtolabs.rundeck.core.execution.workflow.steps.node.NodeStepException;

public class SaltApiNodeStepPlugin_ValidationTest extends AbstractSaltApiNodeStepPluginTest {

    @Test
    public void testValidateAllArgumentsWithValidInput() throws SaltStepValidationException {
        // Test with all valid parameters - should not throw exception
        plugin.validate(PARAM_USER, PARAM_PASSWORD, node);
        // If we get here without exception, validation passed
        Assert.assertTrue("Validation should pass with valid parameters", true);
    }

    @Test
    public void testValidateThrowsOnMissingEndpoint() {
        // Test with missing endpoint
        plugin.saltEndpoint = null;
        try {
            plugin.validate(PARAM_USER, PARAM_PASSWORD, node);
            Assert.fail("Expected validation exception for missing endpoint");
        } catch (SaltStepValidationException e) {
            Assert.assertEquals("Expected correct failure reason", 
                SaltApiNodeStepFailureReason.ARGUMENTS_MISSING, e.getFailureReason());
        }
    }

    @Test
    public void testValidateThrowsOnEmptyEndpoint() {
        // Test with empty endpoint
        plugin.saltEndpoint = "";
        try {
            plugin.validate(PARAM_USER, PARAM_PASSWORD, node);
            Assert.fail("Expected validation exception for empty endpoint");
        } catch (SaltStepValidationException e) {
            Assert.assertEquals("Expected correct failure reason", 
                SaltApiNodeStepFailureReason.ARGUMENTS_MISSING, e.getFailureReason());
        }
    }

    @Test
    public void testValidateChecksValidEndpointHttpUrl() throws NodeStepException {
        plugin.saltEndpoint = "https://some.machine.com";
        plugin.validate(PARAM_USER, PARAM_PASSWORD, node);
    }

    @Test
    public void testValidateChecksValidEndpointHttpsUrl() throws NodeStepException {
        plugin.saltEndpoint = "https://some.machine.com";
        plugin.validate(PARAM_USER, PARAM_PASSWORD, node);
    }

    @Test
    public void testValidateChecksInvalidEndpointUrl() throws NodeStepException {
        plugin.saltEndpoint = "ftp://some.machine.com";
        try {
            plugin.validate(PARAM_USER, PARAM_PASSWORD, node);
            Assert.fail("Expected failure.");
        } catch (SaltStepValidationException e) {
            Assert.assertEquals("Expected correct failure type due to validation failure",
                    SaltApiNodeStepFailureReason.ARGUMENTS_INVALID, e.getFailureReason());
            Assert.assertEquals("Expected field name to be filled out properly",
                    SaltApiNodeStepPlugin.SALT_API_END_POINT_OPTION_NAME, e.getFieldName());
        }
    }
}
