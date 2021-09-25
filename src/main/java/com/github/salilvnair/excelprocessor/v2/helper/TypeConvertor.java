package com.github.salilvnair.excelprocessor.v2.helper;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.poi.ss.usermodel.DateUtil;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

/**
 * @author Salil V Nair
 */
public class TypeConvertor {

    @SuppressWarnings("unchecked")
    public static <T> T convert(Object value, Class<T> sourceType) {
         return (T)convert(value, sourceType, sourceType);
    }

    @SuppressWarnings("unchecked")
    public static <T> T convert(Class<?> sourceType, Class<T> destinationType, Object value) {
        return (T)convert(value, sourceType, destinationType);
    }

    public static Object convert(Object value, Type sourceType, Type destinationType) {
        Object convertedValue = value;
        if(value instanceof Double && destinationType==Date.class) {
            convertedValue = DateUtil.getJavaDate((Double) value);
        }
        else if(value instanceof Number) {
            convertedValue = convertNumber(value, sourceType, destinationType);
        }
        return convertedValue;
    }

    public static Object convertNumber(Object value, Type sourceType, Type destinationType) {
        return _convertNumber(value, sourceType, destinationType);
    }

    private static Object _convertNumber(Object value, Type sourceType, Type destinationType) {
        if(value == null) {
            return null;
        }
        BigDecimal bdWrapper = null;
        int intValue = 0;
        long longValue = 0;
        float floatValue = 0;
        double doubleValue = 0;
        BigInteger bigIntValue = BigInteger.ZERO;
        BigDecimal bigDecimalValue = BigDecimal.ZERO;
        if(sourceType == Double.class) {
            bdWrapper = BigDecimal.valueOf((Double) value);
        }
        else if(sourceType == Integer.class) {
            bdWrapper = BigDecimal.valueOf((Integer) value);
        }
        else if(sourceType == Long.class) {
            bdWrapper = BigDecimal.valueOf((Long) value);
        }
        else if(sourceType == Float.class) {
            bdWrapper = BigDecimal.valueOf((Float) value);
        }
        else if(sourceType == BigInteger.class) {
            bdWrapper = new BigDecimal((BigInteger) value);
        }
        else if(sourceType == BigDecimal.class) {
            bdWrapper = (BigDecimal)value;
        }
        if(bdWrapper!=null) {
            intValue = bdWrapper.intValue();
            longValue = bdWrapper.longValue();
            floatValue = bdWrapper.floatValue();
            doubleValue = bdWrapper.doubleValue();
            bigIntValue = bdWrapper.toBigInteger();
            bigDecimalValue = bdWrapper;
        }
        if(destinationType == String.class) {
            String stringVal = value+"";
            if(bdWrapper != null && NumberUtils.isNumber(stringVal)) {
                double numericStringVal = bdWrapper.doubleValue();
                if ((numericStringVal == Math.floor(numericStringVal)) && !Double.isInfinite(numericStringVal)) {
                    stringVal = bdWrapper.longValue()+"";
                }
            }
            return stringVal;
        }
        else if(destinationType == Double.class) {
            return doubleValue;
        }
        else if(destinationType == Integer.class) {
            return intValue;
        }
        else if(destinationType == Long.class) {
            return longValue;
        }
        else if(destinationType == Float.class) {
            return floatValue;
        }
        else if(destinationType == BigInteger.class) {
            return bigIntValue;
        }
        else if(destinationType == BigDecimal.class) {
            return bigDecimalValue;
        }
        return value;
    }
}
