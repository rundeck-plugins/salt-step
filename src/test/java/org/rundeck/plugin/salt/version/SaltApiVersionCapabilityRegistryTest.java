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
