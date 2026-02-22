package com.localizedjpa.compiler;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Utility methods for string manipulation during code generation.
 * 
 * <p>These methods are shared across generators to avoid duplication.
 */
public final class StringUtils {

    /**
     * Handles transitions from consecutive uppercase sequences to a title-case word.
     * Example: "HTMLParser" → "HTML_Parser"
     */
    private static final Pattern SNAKE_UPPER_SEQ =
            Pattern.compile("([A-Z]+)([A-Z][a-z])");

    /**
     * Handles transitions from a lowercase/digit character to an uppercase character.
     * Example: "camelCase" → "camel_Case"
     */
    private static final Pattern SNAKE_LOWER_TO_UPPER =
            Pattern.compile("([a-z0-9])([A-Z])");

    private StringUtils() {
        // Utility class - no instantiation
    }

    /**
     * Capitalizes the first letter of a string.
     *
     * @param str The string to capitalize
     * @return The string with first letter capitalized, or the original if null/empty
     */
    public static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    /**
     * Converts a camelCase string to snake_case.
     *
     * @param str The camelCase string
     * @return The snake_case string, or the original if null/empty
     */
    public static String toSnakeCase(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }

        // Pass 1: separate consecutive uppercase sequences from the following title-case word
        //   e.g. "HTMLParser" → "HTML_Parser"
        // Pass 2: insert underscore between a lowercase/digit and the next uppercase letter
        //   e.g. "camelCase" → "camel_Case", "myHTTPSUrl" → "my_HTTPS_Url"
        String result = SNAKE_UPPER_SEQ.matcher(str).replaceAll("$1_$2");
        result = SNAKE_LOWER_TO_UPPER.matcher(result).replaceAll("$1_$2");
        return result.toLowerCase(Locale.ROOT);
    }
}
