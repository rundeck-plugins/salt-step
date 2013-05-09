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
