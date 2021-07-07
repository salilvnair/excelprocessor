package com.github.salilvnair.excelprocessor.v2.helper;

/**
 * @author Salil V Nair
 */
public class ObjectUtils {
    public static boolean isNull(Object value) {
        return value == null;
    }
    public static boolean isEmptyString(Object value) {
        return "".equals(value);
    }

    public static boolean isBoolean(Object value) {
        return value instanceof Boolean;
    }

    public static boolean nonNullOrBooleanTrue(Object value) {
        if((value instanceof Boolean && (Boolean) value)) {
            return true;
        }
        else {
            return value!=null;
        }
    }
}
