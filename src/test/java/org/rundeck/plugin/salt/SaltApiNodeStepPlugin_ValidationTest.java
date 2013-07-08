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

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.rundeck.plugin.salt.SaltApiNodeStepPlugin;
import org.rundeck.plugin.salt.SaltApiNodeStepPlugin.SaltApiNodeStepFailureReason;
import org.rundeck.plugin.salt.validation.SaltStepValidationException;
import org.rundeck.plugin.salt.validation.Validators;

import com.dtolabs.rundeck.core.execution.workflow.steps.node.NodeStepException;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Validators.class)
public class SaltApiNodeStepPlugin_ValidationTest extends AbstractSaltApiNodeStepPluginTest {

    @Test
    public void testValidateAllArguments() throws SaltStepValidationException {
        PowerMockito.mockStatic(Validators.class);
        PowerMockito.doNothing().when(Validators.class);
        Validators.checkNotEmpty(Mockito.anyString(), Mockito.anyString(),
                Mockito.any(SaltApiNodeStepFailureReason.class), Mockito.same(node));

        plugin.validate(PARAM_USER, PARAM_PASSWORD, node);

        PowerMockito.verifyStatic();
        Validators.checkNotEmpty(SaltApiNodeStepPlugin.SALT_API_END_POINT_OPTION_NAME, PARAM_ENDPOINT,
                SaltApiNodeStepFailureReason.ARGUMENTS_MISSING, node);
        PowerMockito.verifyStatic();
        Validators.checkNotEmpty(SaltApiNodeStepPlugin.SALT_API_FUNCTION_OPTION_NAME, PARAM_FUNCTION,
                SaltApiNodeStepFailureReason.ARGUMENTS_MISSING, node);
        PowerMockito.verifyStatic();
        Validators.checkNotEmpty(SaltApiNodeStepPlugin.SALT_API_EAUTH_OPTION_NAME, PARAM_EAUTH,
                SaltApiNodeStepFailureReason.ARGUMENTS_MISSING, node);
        PowerMockito.verifyStatic();
        Validators.checkNotEmpty(SaltApiNodeStepPlugin.SALT_USER_OPTION_NAME, PARAM_USER,
                SaltApiNodeStepFailureReason.ARGUMENTS_MISSING, node);
        PowerMockito.verifyStatic();
        Validators.checkNotEmpty(SaltApiNodeStepPlugin.SALT_PASSWORD_OPTION_NAME, PARAM_PASSWORD,
                SaltApiNodeStepFailureReason.ARGUMENTS_MISSING, node);
        PowerMockito.verifyNoMoreInteractions(Validators.class);
    }

    @Test
    public void testValidateThrowsIfValidatorThrows() throws SaltStepValidationException {
        SaltStepValidationException e = new SaltStepValidationException("some property", "Some message",
                SaltApiNodeStepFailureReason.ARGUMENTS_INVALID, node.getNodename());
        PowerMockito.mockStatic(Validators.class);
        PowerMockito.doThrow(e).when(Validators.class);
        Validators.checkNotEmpty(Mockito.anyString(), Mockito.anyString(),
                Mockito.any(SaltApiNodeStepFailureReason.class), Mockito.same(node));

        try {
            plugin.validate(PARAM_USER, PARAM_PASSWORD, node);
            Assert.fail("Expected exception");
        } catch (SaltStepValidationException ne) {
            Assert.assertSame("Expected mocked exception to be thrown", e, ne);
        }
    }

    @Test
    public void testValidateChecksValidEndpointHttpUrl() throws NodeStepException {
        plugin.saltEndpoint = "http://some.machine.com";
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
