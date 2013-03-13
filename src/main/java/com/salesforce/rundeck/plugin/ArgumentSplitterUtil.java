package com.salesforce.rundeck.plugin;

import java.util.List;

import org.apache.commons.lang.text.StrMatcher;
import org.apache.commons.lang.text.StrTokenizer;

import com.google.common.base.Preconditions;

public class ArgumentSplitterUtil { 
    /**
     * Splits the given string using whitespace delimiter respecting quotes.
     * 
     * @return a list of substrings
     * @throws NullPointerException if the input string is null
     */
    @SuppressWarnings("unchecked")
    public static List<String> split(String line) {
        Preconditions.checkNotNull(line);
        StrTokenizer tokenizer = new StrTokenizer(line, StrMatcher.splitMatcher(), StrMatcher.quoteMatcher());
        return tokenizer.getTokenList();
    }
}
