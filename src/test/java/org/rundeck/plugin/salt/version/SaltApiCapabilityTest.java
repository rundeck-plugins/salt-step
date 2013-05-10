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
