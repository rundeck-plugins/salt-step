package org.rundeck.plugin.salt.output;

import java.util.Collections;
import java.util.List;

/**
 * GSON wrapper for salt-api job dispatch return.
 */
public class SaltApiResponseOutput {
    protected String jid;
    protected List<String> minions;

    public String getJid() {
        return jid;
    }

    public List<String> getMinions() {
        return minions == null ? Collections.<String>emptyList() : Collections.unmodifiableList(minions);
    }
}
