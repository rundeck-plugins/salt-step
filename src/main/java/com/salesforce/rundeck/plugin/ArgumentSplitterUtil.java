package com.salesforce.rundeck.plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ArgumentSplitterUtil { 
    protected static final Pattern PATTERN = Pattern.compile("([^\"]\\S*|\".+?\")\\s*");

    /**
     * Splits the given string using whitespace delimiter respecting quotes.
     * 
     * @return a list of substrings
     * @throws NullPointerException if the input string is null
     */
    public static List<String> split(String line) {
        List<String> list = new ArrayList<String>();
        Matcher m = PATTERN.matcher(line);
        while (m.find()) {
            list.add(m.group(1).replace("\"", ""));
        }
        return Collections.unmodifiableList(list);
    }
}
