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
import org.junit.Before;
import org.junit.Test;
import org.rundeck.plugin.salt.version.SaltApiCapability;
import org.rundeck.plugin.salt.version.SaltApiVersionCapabilityRegistry;

public class SaltApiVersionCapabilityRegistryTest {

    protected SaltApiVersionCapabilityRegistry registry;
    protected SaltApiCapability v_0_7_0 = new SaltApiCapability();
    protected SaltApiCapability v_0_8_0 = new SaltApiCapability();
    protected SaltApiCapability v_0_9_0 = new SaltApiCapability();

    @Before
    public void setup() {
        registry = new SaltApiVersionCapabilityRegistry();
        registry.versionRegistry.clear();
        registry.register("0.7.0", v_0_7_0);
        registry.register("0.8.0", v_0_8_0);
        registry.register("0.9.0", v_0_9_0);
    }

    @Test
    public void testGetExact() {
        Assert.assertSame("Expected exact match for version", v_0_7_0, registry.getCapability("0.7.0"));
    }

    @Test
    public void testGetLowerThanBound() {
        Assert.assertSame("Expected lower than bounds gets lowest known version", v_0_7_0,
                registry.getCapability("0.6.0"));
    }

    @Test
    public void testGetBetweenBounds() {
        Assert.assertSame("Expected between versions to get highest version lower than passed in version", v_0_8_0,
                registry.getCapability("0.8.5"));
    }

    @Test
    public void testGetHigherThanBound() {
        Assert.assertSame("Expected higher than bounds gets highest known version", v_0_9_0,
                registry.getCapability("0.10.0"));
    }

    @Test
    public void testGetLatest() {
        Assert.assertSame("Expected latest to be highest known version", v_0_9_0, registry.getLatest());
    }
}
