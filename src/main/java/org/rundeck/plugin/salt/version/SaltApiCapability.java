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
        
        public Builder withId(String id) {
            origin.id = id;
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

        public Builder supportsLogout() {
            origin.supportsLogout = true;
            return this;
        }
        
        public Builder withSaltInteractionHandler(SaltInteractionHandler interactionHandler) {
            origin.interactionHandler = interactionHandler;
            return this;
        }
        
        public SaltApiCapability build() {
            return origin;
        }
    }

    private String id;
    private int loginSuccessResponseCode;
    private int loginFailureResponseCode;
    private boolean supportsLogout = false;
    private SaltInteractionHandler interactionHandler;
    
    public String getId() {
        return id;
    }

    public int getLoginFailureResponseCode() {
        return loginFailureResponseCode;
    }

    public int getLoginSuccessResponseCode() {
        return loginSuccessResponseCode;
    }
    
    public boolean getSupportsLogout() {
        return supportsLogout;
    }
    
    public SaltInteractionHandler getSaltInteractionHandler() {
        return interactionHandler;
    }
}
