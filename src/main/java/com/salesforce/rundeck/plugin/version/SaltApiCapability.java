package com.salesforce.rundeck.plugin.version;


/**
 * Configuration information for the capability of a specific version of salt-api
 */
public class SaltApiCapability implements Cloneable {

    public static class Builder {
        SaltApiCapability origin = new SaltApiCapability();

        public Builder from(SaltApiCapability capability) {
            try {
                origin = (SaltApiCapability) capability.clone();
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
            return this;
        }

        public Builder withLoginFailureResponseCode(int newLoginFailureResponseCode) {
            origin.loginFailureResponseCode = newLoginFailureResponseCode;
            return this;
        }

        public Builder withLoginSuccessResponseCode(int newLoginSuccessResponseCode) {
            origin.loginSuccessResponseCode = newLoginSuccessResponseCode;
            return this;
        }

        public SaltApiCapability build() {
            return origin;
        }
    }

    private int loginSuccessResponseCode;
    private int loginFailureResponseCode;

    public int getLoginFailureResponseCode() {
        return loginFailureResponseCode;
    }

    public int getLoginSuccessResponseCode() {
        return loginSuccessResponseCode;
    }

    @Override
    public String toString() {
        return "SaltApiCapability [loginSuccessResponseCode=" + loginSuccessResponseCode
                + ", loginFailureResponseCode=" + loginFailureResponseCode + "]";
    }
}
