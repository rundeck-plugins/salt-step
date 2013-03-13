package com.salesforce.rundeck.plugin;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

public class ArgumentSplitterUtilTest {

    @Test
    public void testSplit() {
        List<String> args = ArgumentSplitterUtil.split("1 2 3");
        Assert.assertEquals(3, args.size());
        Assert.assertEquals("1", args.get(0));
        Assert.assertEquals("2", args.get(1));
        Assert.assertEquals("3", args.get(2));
    }

    @Test
    public void testSplitWithQuotedString() {
        List<String> args = ArgumentSplitterUtil.split("\"1 2\" 3");
        Assert.assertEquals(2, args.size());
        Assert.assertEquals("1 2", args.get(0));
        Assert.assertEquals("3", args.get(1));
    }
    
    @Test
    public void testSplitWithTabs() {
        List<String> args = ArgumentSplitterUtil.split("\"1\t2\"\t3");
        Assert.assertEquals(2, args.size());
        Assert.assertEquals("1\t2", args.get(0));
        Assert.assertEquals("3", args.get(1));
    }
    
    @Test
    public void testSplitWithMultipleSpaces () {
        List<String> args = ArgumentSplitterUtil.split("\"1 2\"     3");
        Assert.assertEquals(2, args.size());
        Assert.assertEquals("1 2", args.get(0));
        Assert.assertEquals("3", args.get(1));
    }
    
    @Test
    public void testSplitWithEscapes() {
        List<String> args = ArgumentSplitterUtil.split("cmd.run 'echo \"some message\"'");
        Assert.assertEquals(2, args.size());
        Assert.assertEquals("cmd.run", args.get(0));
        Assert.assertEquals("echo \"some message\"", args.get(1));   
    }

    @Test
    public void testSplitEmptyString() {
        Assert.assertEquals(0, ArgumentSplitterUtil.split("").size());
    }

    @Test(expected = NullPointerException.class)
    public void testSplitNullString() {
        ArgumentSplitterUtil.split(null);
    }
}
