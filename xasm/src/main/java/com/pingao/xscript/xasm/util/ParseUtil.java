package com.pingao.xscript.xasm.util;

/**
 * Created by pingao on 2017/2/3.
 */
public class ParseUtil {
    public static boolean isCharWhiteSpace(char c) {
        return c == ' ' || c == '\t';
    }

    public static boolean isCharNumberic(char c) {
        return c >= '0' && c <= '9';
    }

    public static boolean isCharIdentifier(char c) {
        return (c >= '0' && c <= '9')
            || (c >= 'a' && c <= 'z')
            || (c >= 'A' && c <= 'Z')
            || c == '_';
    }

    public static boolean isCharDelimiter(char c) {
        return isCharWhiteSpace(c)
            || c == ':'
            || c == ','
            || c == '"'
            || c == '['
            || c == ']'
            || c == '{'
            || c == '}'
            || c == '\n';
    }

    public static boolean isIdentifier(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }

        if (isCharNumberic(str.charAt(0))) {
            return false;
        }

        for (int i = 0; i < str.length(); i++) {
            if (!isCharIdentifier(str.charAt(i))) {
                return false;
            }
        }

        return true;
    }

    public static boolean isStringInteger(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }

        for (int i = 0; i < str.length(); i++) {
            if (i == 0) {
                if (!isCharNumberic(str.charAt(i)) && str.charAt(i) != '-') {
                    return false;
                }
            } else {
                if (!isCharNumberic(str.charAt(i))) {
                    return false;
                }
            }
        }

        return false;
    }

    public static boolean isStringFloat(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }

        for (int i = 0; i < str.length(); i++) {
            if (i == 0) {
                if (!isCharNumberic(str.charAt(i)) && str.charAt(i) != '.' && str.charAt(i) != '-') {
                    return false;
                }
            } else {
                if (!isCharNumberic(str.charAt(i)) && str.charAt(i) != '.') {
                    return false;
                }
            }
        }

        return true;
    }

    public static String stripeComments(String line) {
        boolean isInString = false;
        for (int i = 0; i < line.length(); i++) {
            if (line.charAt(i) == '"') {
                isInString = !isInString;
            }
            if (line.charAt(i) == ';') {
                if (!isInString) {
                    return "\n";
                }
            }
        }
        return line;
    }
}
