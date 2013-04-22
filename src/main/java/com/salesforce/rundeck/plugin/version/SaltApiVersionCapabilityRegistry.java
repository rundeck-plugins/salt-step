package com.salesforce.rundeck.plugin.version;

import java.util.Comparator;
import java.util.SortedMap;

import org.apache.http.HttpStatus;
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
    public static final SaltApiCapability VERSION_0_8_0 = new SaltApiCapability.Builder().from(VERSION_0_7_5)
            .withLoginFailureResponseCode(HttpStatus.SC_UNAUTHORIZED).withLoginSuccessResponseCode(HttpStatus.SC_OK)
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
