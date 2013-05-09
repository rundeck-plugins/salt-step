package org.rundeck.plugin.salt.output;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.rundeck.plugin.salt.output.SaltApiResponseOutput;

import com.google.common.collect.Lists;

public class SaltApiResponseOutputTest {

    @Test(expected = UnsupportedOperationException.class)
    public void testGetMinionsUnmodifiable() {
        SaltApiResponseOutput output = new SaltApiResponseOutput();
        List<String> minions = Lists.newArrayList("a", "b");
        output.minions = minions;
        Assert.assertEquals("Expected passed in minions", minions, output.getMinions());
        output.getMinions().add("asdf");
    }
    
    @Test
    public void testGetUnsetMinions() {
        SaltApiResponseOutput output = new SaltApiResponseOutput();
        Assert.assertEquals("Expected no minions if unset", 0, output.getMinions().size());
    }
}
