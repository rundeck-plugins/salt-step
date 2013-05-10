package org.rundeck.plugin.salt.version;

/**
 * Configuration information for the capability of a specific version of salt-api
 */
public class SaltApiCapability implements Cloneable {

    public static class Builder {
        SaltApiCapability origin = new SaltApiCapability();

        public static Builder from(SaltApiCapability capability) {
            Builder builder = new Builder();
            try {
                builder.origin = (SaltApiCapability) capability.clone();
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
            return builder;
        }

        public Builder withLoginFailureResponseCode(int newLoginFailureResponseCode) {
            origin.loginFailureResponseCode = newLoginFailureResponseCode;
            return this;
        }

        public Builder withLoginSuccessResponseCode(int newLoginSuccessResponseCode) {
            origin.loginSuccessResponseCode = newLoginSuccessResponseCode;
            return this;
        }

        public Builder supportsLogout() {
            origin.supportsLogout = true;
            return this;
        }
        
        public SaltApiCapability build() {
            return origin;
        }
    }

    private int loginSuccessResponseCode;
    private int loginFailureResponseCode;
    private boolean supportsLogout = false;

    public int getLoginFailureResponseCode() {
        return loginFailureResponseCode;
    }

    public int getLoginSuccessResponseCode() {
        return loginSuccessResponseCode;
    }
    
    public boolean getSupportsLogout() {
        return supportsLogout;
    }
}
