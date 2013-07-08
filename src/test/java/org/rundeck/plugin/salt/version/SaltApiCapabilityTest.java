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

package org.rundeck.plugin.salt.version;

import org.junit.Assert;
import org.junit.Test;
import org.rundeck.plugin.salt.version.SaltApiCapability.Builder;

public class SaltApiCapabilityTest {

    @Test
    public void testWithLoginFailureResponseCode() {
        int loginFailureCode = 2;
        SaltApiCapability capability = new SaltApiCapability.Builder().withLoginFailureResponseCode(loginFailureCode)
                .build();
        Assert.assertEquals("Expected login failure response code to be passed in value", loginFailureCode,
                capability.getLoginFailureResponseCode());
    }

    @Test
    public void testWithLoginSuccessResponseCode() {
        int loginSuccessCode = 1;
        SaltApiCapability capability = new SaltApiCapability.Builder().withLoginSuccessResponseCode(loginSuccessCode)
                .build();
        Assert.assertEquals("Expected login success response code to be passed in value", loginSuccessCode,
                capability.getLoginSuccessResponseCode());
    }

    @Test
    public void testWithSupportsLogout() {
        SaltApiCapability capability = new SaltApiCapability.Builder().build();
        Assert.assertFalse("Expected default logout support to be off", capability.getSupportsLogout());
        capability = Builder.from(capability).supportsLogout().build();
        Assert.assertTrue("Expected logout support to be turned on.", capability.getSupportsLogout());
    }
}
