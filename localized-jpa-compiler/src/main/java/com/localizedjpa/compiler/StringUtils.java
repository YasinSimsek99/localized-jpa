package com.localizedjpa.compiler;

/**
 * Utility methods for string manipulation during code generation.
 * 
 * <p>These methods are shared across generators to avoid duplication.
 */
public final class StringUtils {

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
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (Character.isUpperCase(c)) {
                if (i > 0) {
                    result.append('_');
                }
                result.append(Character.toLowerCase(c));
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }
}
