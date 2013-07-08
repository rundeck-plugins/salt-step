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

import java.util.Comparator;
import java.util.SortedMap;

import org.apache.http.HttpStatus;
import org.rundeck.plugin.salt.version.SaltApiCapability.Builder;
import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;

/**
 * Central management for different versions of salt-api
 */
@Component
public class SaltApiVersionCapabilityRegistry {
    public static final String VERSION_0_7_5_NAME = "0.7.5";
    public static final SaltApiCapability VERSION_0_7_5 = new SaltApiCapability.Builder()
            .withLoginSuccessResponseCode(HttpStatus.SC_MOVED_TEMPORARILY)
            .withLoginFailureResponseCode(HttpStatus.SC_INTERNAL_SERVER_ERROR).build();

    public static final String VERSION_0_8_0_NAME = "0.8.0";
    public static final SaltApiCapability VERSION_0_8_0 = Builder.from(VERSION_0_7_5)
            .withLoginFailureResponseCode(HttpStatus.SC_UNAUTHORIZED).withLoginSuccessResponseCode(HttpStatus.SC_OK).supportsLogout()
            .build();

    protected SortedMap<String, SaltApiCapability> versionRegistry;

    public SaltApiVersionCapabilityRegistry() {
        versionRegistry = Maps.newTreeMap(new Comparator<String>() {
            @Override
            public int compare(String arg0, String arg1) {
                return arg1.compareTo(arg0);
            }
        });
        register(VERSION_0_7_5_NAME, VERSION_0_7_5);
        register(VERSION_0_8_0_NAME, VERSION_0_8_0);
    }

    /**
     * @return the lowest versioned registered capability greater than the given version
     */
    public SaltApiCapability getCapability(String version) {
        SortedMap<String, SaltApiCapability> tail = versionRegistry.tailMap(normalizeVersion(version));
        String key = tail.size() == 0 ? versionRegistry.lastKey() : tail.firstKey();
        return versionRegistry.get(key);
    }

    /**
     * @return the highest versioned registered capability
     */
    public SaltApiCapability getLatest() {
        return versionRegistry.get(versionRegistry.firstKey());
    }

    protected void register(String version, SaltApiCapability capability) {
        versionRegistry.put(normalizeVersion(version), capability);
    }

    protected String normalizeVersion(String version) {
        String[] split = version.split("_|\\.");
        StringBuilder sb = new StringBuilder();
        for (String s : split) {
            sb.append(String.format("%10s", s));
        }
        return sb.toString();
    }    
}
