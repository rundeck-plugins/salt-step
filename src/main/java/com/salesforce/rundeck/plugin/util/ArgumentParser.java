package com.salesforce.rundeck.plugin.util;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

/**
 * Utility for parsing argument strings respecting quotes and
 * escape of quotes into individual strings.
 */
public class ArgumentParser {

    public static final ArgumentParser DEFAULT_ARGUMENT_SPLITTER = new ArgumentParser("\\s", new char[] { '\'', '"' },
            '\\');

    protected static final char DEFAULT_ESCAPE_CHARACTER = '\\';

    protected final String separatorCharSetRegex;
    protected final char[] quoteCharacters;
    protected final char escapeCharacter;

    public ArgumentParser(String separatorCharSetRegex) {
        this(separatorCharSetRegex, null);
    }

    public ArgumentParser(String separatorCharSetRegex, char[] quoteRegex) {
        this(separatorCharSetRegex, quoteRegex, DEFAULT_ESCAPE_CHARACTER);
    }

    /**
     * @param separatorCharSetRegex
     *            A regex that describes the character set (matches single character at a time) for allowable
     *            separators.
     * @param quoteCharacters
     *            Characters that count as quotes
     * @param escapeCharacter
     *            Character that allows for escaping of quotes
     */
    public ArgumentParser(String separatorCharSetRegex, char[] quoteCharacters, char escapeCharacter) {
        Preconditions.checkArgument(StringUtils.isNotEmpty(separatorCharSetRegex));
        if (quoteCharacters == null) {
            quoteCharacters = new char[0];
        }
        Arrays.sort(quoteCharacters);
        this.separatorCharSetRegex = separatorCharSetRegex;
        this.quoteCharacters = quoteCharacters;
        this.escapeCharacter = escapeCharacter;

    }

    /**
     * Parses the given line and returns all non-empty segments.
     * 
     * @throws IllegalArgumentException
     *             if the quotes are unbalanced.
     */
    public List<String> parse(String line) {
        boolean inQuote = false;
        Character lastQuote = null;

        List<String> results = Lists.newLinkedList();
        StringBuilder currentSegment = new StringBuilder();

        for (int i = 0; i < line.length(); i++) {
            char currentChar = line.charAt(i);
            // If this character is an escape and the next character is a valid quote, then add the quote character to the current segment
            if (currentChar == escapeCharacter && i + 1 < line.length() && isQuote(line.charAt(i + 1))) {
                currentSegment.append(line.charAt(++i));
            } else if (isQuote(currentChar)) {
                if (inQuote) {
                    // If segment is quoted and this is a closing quote, then close off this segment
                    if (lastQuote == currentChar) {
                        lastQuote = null;
                        inQuote = false;
                        if (currentSegment.length() > 0) {
                            results.add(currentSegment.toString());
                            currentSegment = new StringBuilder();
                        }
                    } else {
                        // Otherwise, this is a quote character that wasn't used to open this segment so just add it.
                        currentSegment.append(currentChar);
                    }
                } else {
                    // If not currently in a quote, open a new segment
                    if (currentSegment.length() > 0) {
                        results.add(currentSegment.toString());
                        currentSegment = new StringBuilder();
                    }
                    lastQuote = currentChar;
                    inQuote = true;
                }
            } else if (String.valueOf(currentChar).matches(separatorCharSetRegex)) {
                // If this is a separator, separate the segment if not in quotes
                if (inQuote) {
                    currentSegment.append(currentChar);
                } else {
                    if (currentSegment.length() > 0) {
                        results.add(currentSegment.toString());
                        currentSegment = new StringBuilder();
                    }
                }
            } else {
                // Otherwise, this is a regular character, just add to current segment
                currentSegment.append(currentChar);
            }
        }
        
        if (currentSegment.length() > 0) {
            results.add(currentSegment.toString());
        }
        
        if (inQuote) {
            throw new IllegalArgumentException("Quotes are unbalanced.");
        }

        return results;
    }

    protected boolean isQuote(char c) {
        return Arrays.binarySearch(quoteCharacters, c) >= 0;
    }
}
