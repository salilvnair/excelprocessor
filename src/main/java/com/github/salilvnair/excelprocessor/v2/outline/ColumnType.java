package com.github.salilvnair.excelprocessor.v2.outline;

import java.util.Date;

/** Type inferred for a column from its sampled values. */
public enum ColumnType {
    /** no non-blank value was sampled */
    BLANK(String.class),
    TEXT(String.class),
    /** whole numbers only */
    INTEGER(Long.class),
    /** numbers with fractions (or a mix of whole and fractional numbers) */
    DECIMAL(Double.class),
    DATE(Date.class),
    BOOLEAN(Boolean.class);

    private final Class<?> javaType;

    ColumnType(Class<?> javaType) {
        this.javaType = javaType;
    }

    /** Suggested Java field type, e.g. for generated sheet classes. */
    public Class<?> javaType() {
        return javaType;
    }

    /** The narrowest type that holds both. */
    public ColumnType merge(ColumnType other) {
        if (this == other || other == BLANK) {
            return this;
        }
        if (this == BLANK) {
            return other;
        }
        if ((this == INTEGER && other == DECIMAL) || (this == DECIMAL && other == INTEGER)) {
            return DECIMAL;
        }
        return TEXT;
    }

    /** INTEGER for whole numbers that fit a long exactly, DECIMAL otherwise. */
    public static ColumnType ofNumber(double value) {
        return value == Math.rint(value) && !Double.isInfinite(value) && Math.abs(value) < 9.0e15 ? INTEGER : DECIMAL;
    }
}
