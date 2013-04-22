package com.salesforce.rundeck.plugin.version;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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
        Assert.assertSame(v_0_7_0, registry.getCapability("0.7.0"));
    }

    @Test
    public void testGetLowerThanBound() {
        Assert.assertSame(v_0_7_0, registry.getCapability("0.6.0"));
    }
    
    @Test
    public void testGetBetweenBounds() {
        Assert.assertSame(v_0_8_0, registry.getCapability("0.8.5"));
    }

    @Test
    public void testGetHigherThanBound() {
        Assert.assertSame(v_0_9_0, registry.getCapability("0.10.0"));
    }

    @Test
    public void testGetLatest() {
        Assert.assertSame(v_0_9_0, registry.getLatest());
    }
}
