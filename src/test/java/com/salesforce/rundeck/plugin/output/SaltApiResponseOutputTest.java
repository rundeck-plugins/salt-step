package com.salesforce.rundeck.plugin.output;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import com.google.common.collect.Lists;

public class SaltApiResponseOutputTest {

    @Test(expected = UnsupportedOperationException.class)
    public void testGetMinionsUnmodifiable() {
        SaltApiResponseOutput output = new SaltApiResponseOutput();
        List<String> minions = Lists.newArrayList("a", "b");
        output.minions = minions;
        Assert.assertEquals(minions, output.getMinions());
        output.getMinions().add("asdf");
    }
    
    @Test
    public void testGetUnsetMinions() {
        SaltApiResponseOutput output = new SaltApiResponseOutput();
        Assert.assertEquals(0, output.getMinions().size());
    }
}
