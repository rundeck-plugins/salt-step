package org.rundeck.plugin.salt;

import org.junit.Before;
import org.rundeck.plugin.salt.version.SaltApiCapability;


public abstract class AbstractSaltApiNodeStepPlugin_BackwardsCompatabilityTest extends AbstractSaltApiNodeStepPluginTest {

    protected SaltApiCapability legacyCapability;
    
    @Before
    public void initLegacyCapability() {
        plugin.saltApiVersion = getVersion();
        legacyCapability = plugin.capabilityRegistry.getCapability(plugin.saltApiVersion);
    }
    
    protected abstract String getVersion();
}
