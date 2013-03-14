package com.salesforce.rundeck.plugin.util;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import com.salesforce.rundeck.plugin.util.ArgumentParser;

public class ArgumentParserTest {

    @Test
    public void testParse() {
        List<String> args = new ArgumentParser("\\s").parse("1 2 3");
        Assert.assertEquals(3, args.size());
        Assert.assertEquals("1", args.get(0));
        Assert.assertEquals("2", args.get(1));
        Assert.assertEquals("3", args.get(2));
    }

    @Test
    public void testParseWithDoubleQuotedString() {
        List<String> args = new ArgumentParser("\\s", new char[] { '"' }).parse("\"1 2\" 3");
        Assert.assertEquals(2, args.size());
        Assert.assertEquals("1 2", args.get(0));
        Assert.assertEquals("3", args.get(1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseWithDoubleQuotedLeadingUnbalancedString() {
        new ArgumentParser("\\s", new char[] { '"' }).parse("\"1 2");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseWithDoubleQuotedTrailingUnbalancedString() {
        new ArgumentParser("\\s", new char[] { '"' }).parse("1 2\"");
    }

    @Test
    public void testParseWithSingleQuotedString() {
        List<String> args = new ArgumentParser("\\s", new char[] { '\'' }).parse("'1 2' 3");
        Assert.assertEquals(2, args.size());
        Assert.assertEquals("1 2", args.get(0));
        Assert.assertEquals("3", args.get(1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseWithSingleQuotedLeadingUnbalancedString() {
        new ArgumentParser("\\s", new char[] { '\'' }).parse("'1 2");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseWithSingleQuotedTrailingUnbalancedString() {
        new ArgumentParser("\\s", new char[] { '\'' }).parse("1 2'");
    }

    @Test
    public void testParseWithSingleAndDoubleQuotedString() {
        List<String> args = new ArgumentParser("\\s", new char[] { '\'', '"' }).parse("'1 2' \"3 4\"");
        Assert.assertEquals(2, args.size());
        Assert.assertEquals("1 2", args.get(0));
        Assert.assertEquals("3 4", args.get(1));
    }

    @Test
    public void testParseWithSingleAndDoubleQuotedStringWithoutSeparator() {
        List<String> args = new ArgumentParser("\\s", new char[] { '\'', '"' }).parse("'1 2'\"3 4\"");
        Assert.assertEquals(2, args.size());
        Assert.assertEquals("1 2", args.get(0));
        Assert.assertEquals("3 4", args.get(1));
    }

    @Test
    public void testParseWithSingleQuotedStringWithoutSeparator() {
        List<String> args = new ArgumentParser("\\s", new char[] { '\'', '"' }).parse("0'1 2'3");
        Assert.assertEquals(3, args.size());
        Assert.assertEquals("0", args.get(0));
        Assert.assertEquals("1 2", args.get(1));
        Assert.assertEquals("3", args.get(2));
    }

    @Test
    public void testParseWithTabs() {
        List<String> args = new ArgumentParser("\\s", new char[] { '"' }).parse("\"1\t2\"\t3");
        Assert.assertEquals(2, args.size());
        Assert.assertEquals("1\t2", args.get(0));
        Assert.assertEquals("3", args.get(1));
    }

    @Test
    public void testParseWithMultipleSpaces() {
        List<String> args = new ArgumentParser("\\s", new char[] { '"' }).parse("\"1 2\"     3");
        Assert.assertEquals(2, args.size());
        Assert.assertEquals("1 2", args.get(0));
        Assert.assertEquals("3", args.get(1));
    }

    @Test
    public void testParseSingleQuoteWithNestedDoubleQuotes() {
        List<String> args = new ArgumentParser("\\s", new char[] { '\'', '"' }, '\\')
                .parse("cmd.run 'echo \"some message\"'");
        Assert.assertEquals(2, args.size());
        Assert.assertEquals("cmd.run", args.get(0));
        Assert.assertEquals("echo \"some message\"", args.get(1));
    }

    @Test
    public void testParseDoubleQuoteWithNestedSingleQuotes() {
        List<String> args = new ArgumentParser("\\s", new char[] { '\'', '"' }, '\\')
                .parse("cmd.run \"echo 'some message'\"");
        Assert.assertEquals(2, args.size());
        Assert.assertEquals("cmd.run", args.get(0));
        Assert.assertEquals("echo 'some message'", args.get(1));
    }

    @Test
    public void testParseSingleQuoteWithNestedEscapedSingleQuotes() {
        List<String> args = new ArgumentParser("\\s", new char[] { '\'', '"' }, '\\')
                .parse("cmd.run 'echo \\'some message\\''");
        Assert.assertEquals(2, args.size());
        Assert.assertEquals("cmd.run", args.get(0));
        Assert.assertEquals("echo 'some message'", args.get(1));
    }

    @Test
    public void testParseDoubleQuoteWithNestedEscapedDoubleQuotes() {
        List<String> args = new ArgumentParser("\\s", new char[] { '\'', '"' }, '\\')
                .parse("cmd.run \"echo \\\"some message\\\"\"");
        Assert.assertEquals(2, args.size());
        Assert.assertEquals("cmd.run", args.get(0));
        Assert.assertEquals("echo \"some message\"", args.get(1));
    }
    
    @Test
    public void testParseEscapeCharacterWithoutFollowingQuote() {
        List<String> args = new ArgumentParser("\\s", new char[] { '\'', '"' }, '\\')
                .parse("cmd.run \"echo \\\"some m\\essage\\\"\"");
        Assert.assertEquals(2, args.size());
        Assert.assertEquals("cmd.run", args.get(0));
        Assert.assertEquals("echo \"some m\\essage\"", args.get(1));
    }
    
    @Test
    public void testParseTrailingEscapeCharacterWithoutFollowingQuote() {
        List<String> args = new ArgumentParser("\\s", new char[] { '\'', '"' }, '\\')
                .parse("cmd.run \"echo \\\"some message\\\"\"\\");
        Assert.assertEquals(3, args.size());
        Assert.assertEquals("cmd.run", args.get(0));
        Assert.assertEquals("echo \"some message\"", args.get(1));
        Assert.assertEquals("\\", args.get(2));
    }
    
    @Test
    public void testParseEmptyString() {
        Assert.assertEquals(0, new ArgumentParser("\\s").parse("").size());
    }

    @Test(expected = NullPointerException.class)
    public void testParseNullString() {
        new ArgumentParser("\\s").parse(null);
    }

    @Test
    public void testDefaultArgumentSplitter() {
        Assert.assertEquals("\\s", ArgumentParser.DEFAULT_ARGUMENT_SPLITTER.separatorCharSetRegex);
        char[] quoteCharacters = ArgumentParser.DEFAULT_ARGUMENT_SPLITTER.quoteCharacters;
        Assert.assertEquals('"', quoteCharacters[0]);
        Assert.assertEquals('\'', quoteCharacters[1]);
        Assert.assertEquals('\\', ArgumentParser.DEFAULT_ARGUMENT_SPLITTER.escapeCharacter);
    }
}
