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
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.rundeck.plugin.salt.version.SaltApiCapability;
import org.rundeck.plugin.salt.version.SaltApiVersionCapabilityRegistry;

public class SaltApiNodeStepPlugin_CapabilityTest extends AbstractSaltApiNodeStepPluginTest {

    protected SaltApiVersionCapabilityRegistry registry;

    @Before
    public void setup() {
        registry = Mockito.mock(SaltApiVersionCapabilityRegistry.class);
        plugin.capabilityRegistry = registry;
    }

    @Test
    public void testGetCapabilityWithNoVersionSupplied() {
        SaltApiCapability capability = new SaltApiCapability();
        Mockito.when(registry.getLatest()).thenReturn(capability);

        Assert.assertSame("Expected unset version to return latest known capability", capability,
                plugin.getSaltApiCapability());
    }

    @Test
    public void testGetCapabilityWithBlankVersionSupplied() {
        plugin.saltApiVersion = "   ";
        SaltApiCapability capability = new SaltApiCapability();
        Mockito.when(registry.getLatest()).thenReturn(capability);

        Assert.assertSame("Expected blank version to return latest known capability", capability,
                plugin.getSaltApiCapability());
    }

    @Test
    public void testGetCapabilityWithVersionSupplied() {
        String version = "someversion";
        plugin.saltApiVersion = version;
        SaltApiCapability capability = new SaltApiCapability();
        Mockito.when(registry.getCapability(version)).thenReturn(capability);

        Assert.assertSame("Expected set version to return registered capability", capability,
                plugin.getSaltApiCapability());
    }
}
