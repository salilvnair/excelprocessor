package com.github.salilvnair.excelprocessor.util;


/**
 * @author Salil V Nair
 */
public class ObjectUtil {
    public static boolean isNull(Object value) {
        return value == null;
    }
    public static boolean isNotNull(Object value) {
        return !isNull(value);
    }
    public static boolean isEmptyString(Object value) {
        return "".equals(value);
    }

    public static boolean isNotEmptyString(Object value) {
        return !isEmptyString(value);
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
