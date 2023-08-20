package com.github.salilvnair.excelprocessor.v2.type;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;

public enum FieldType {
    STRING(TypeInfo.STRING, String.class),
    INTEGER(TypeInfo.INTEGER, Integer.class),
    LONG(TypeInfo.LONG, Long.class),
    DOUBLE(TypeInfo.DOUBLE, Double.class),
    FLOAT(TypeInfo.FLOAT, Float.class),
    BIG_INTEGER(TypeInfo.BIG_INTEGER, BigInteger.class),
    BIG_DECIMAL(TypeInfo.BIG_DECIMAL, BigDecimal.class),
    DATE(TypeInfo.DATE, Date.class),
    ;

    private final String typeStringValue;
    private final Type type;
    FieldType(String typeStringValue, Type type) {
        this.typeStringValue = typeStringValue;
        this.type = type;
    }

    public Type type() {
        return type;
    }

    public String typeStringValue() {
        return typeStringValue;
    }

    public static class TypeInfo {
        public static final String STRING = "String";
        public static final String LONG = "Long";
        public static final String INTEGER = "Integer";
        public static final String DOUBLE = "Double";
        public static final String FLOAT = "Float";
        public static final String BIG_INTEGER = "BigInteger";
        public static final String BIG_DECIMAL = "BigDecimal";
        public static final String DATE = "Date";
    }

    public static FieldType type(String typeStringValue) {
        Optional<FieldType> typeEnum = Arrays.stream(FieldType.values())
                .filter(comp -> comp.typeStringValue() != null && comp.typeStringValue().equals(typeStringValue)).findFirst();
        return typeEnum.orElse(FieldType.STRING);
    }
}
