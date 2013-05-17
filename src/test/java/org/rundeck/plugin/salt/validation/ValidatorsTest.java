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

package org.rundeck.plugin.salt.validation;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.rundeck.plugin.salt.SaltApiNodeStepPlugin.SaltApiNodeStepFailureReason;
import org.rundeck.plugin.salt.validation.SaltStepValidationException;
import org.rundeck.plugin.salt.validation.Validators;

import com.dtolabs.rundeck.core.common.INodeEntry;
import com.dtolabs.rundeck.core.execution.workflow.steps.FailureReason;
import com.dtolabs.rundeck.core.execution.workflow.steps.node.NodeStepException;

public class ValidatorsTest {
    protected String NODE_NAME = "node";
    protected INodeEntry entry;

    @Before
    public void setup() {
        entry = Mockito.mock(INodeEntry.class);
        Mockito.when(entry.getNodename()).thenReturn(NODE_NAME);
    }

    @Test
    public void testCheckNotEmptyWithNullString() {
        FailureReason reason = SaltApiNodeStepFailureReason.ARGUMENTS_MISSING;
        try {
            Validators.checkNotEmpty("some prop", null, reason, entry);
        } catch (NodeStepException e) {
            Assert.assertEquals(reason, e.getFailureReason());
            Assert.assertEquals(NODE_NAME, e.getNodeName());
        }
    }

    @Test
    public void testCheckNotEmptyWithEmptyString() {
        FailureReason reason = SaltApiNodeStepFailureReason.ARGUMENTS_MISSING;
        try {
            Validators.checkNotEmpty("some prop", "", reason, entry);
        } catch (NodeStepException e) {
            Assert.assertEquals(reason, e.getFailureReason());
            Assert.assertEquals(NODE_NAME, e.getNodeName());
        }
    }

    @Test
    public void testCheckNotEmptyWithNonEmptyString() throws SaltStepValidationException {
        FailureReason reason = SaltApiNodeStepFailureReason.ARGUMENTS_MISSING;
        Validators.checkNotEmpty("some prop", "value", reason, entry);
    }

}
