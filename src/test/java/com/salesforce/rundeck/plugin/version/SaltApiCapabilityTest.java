package com.salesforce.rundeck.plugin.version;

import org.junit.Assert;
import org.junit.Test;

public class SaltApiCapabilityTest {

    @Test
    public void testWithLoginFailureResponseCode() {
        int loginFailureCode = 2;
        SaltApiCapability capability = new SaltApiCapability.Builder().withLoginFailureResponseCode(loginFailureCode).build();
        Assert.assertEquals("Expected login failure response code to be passed in value", loginFailureCode, capability.getLoginFailureResponseCode());
    }

    @Test
    public void testWithLoginSuccessResponseCode() {
        int loginSuccessCode = 1;
        SaltApiCapability capability = new SaltApiCapability.Builder().withLoginSuccessResponseCode(loginSuccessCode).build();
        Assert.assertEquals("Expected login success response code to be passed in value", loginSuccessCode, capability.getLoginSuccessResponseCode());
    }
}
