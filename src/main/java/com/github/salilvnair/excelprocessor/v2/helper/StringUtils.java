package com.github.salilvnair.excelprocessor.v2.helper;

import java.util.Collection;

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

    public static String findClosestMatch(Collection<String> collection, String target) {
        int distance = Integer.MAX_VALUE;
        String closest = null;
        for (String compareObject : collection) {
            int currentDistance = org.apache.commons.lang.StringUtils.getLevenshteinDistance(compareObject, target);
            if(currentDistance < distance) {
                distance = currentDistance;
                closest = compareObject;
            }
        }
        return closest;
    }
}