package com.github.salilvnair.excelprocessor.v2.helper;

/**
 * @author Salil V Nair
 */

public abstract class StringUtils {

    public static boolean isEmpty(String val) {
        return val == null || "".equals(val);
    }

    public static boolean isNotEmpty(String val) {
        return !isEmpty(val);
    }

    public static String trimAllWhitespace(String str) {
        if (!hasLength(str)) {
            return str;
        }

        int len = str.length();
        StringBuilder sb = new StringBuilder(str.length());
        for (int i = 0; i < len; i++) {
            char c = str.charAt(i);
            if (!Character.isWhitespace(c)) {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static boolean hasLength(String str) {
        return (str != null && !str.isEmpty());
    }
}