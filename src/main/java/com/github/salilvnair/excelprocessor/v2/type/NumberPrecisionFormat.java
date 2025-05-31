package com.github.salilvnair.excelprocessor.v2.type;

public enum NumberPrecisionFormat {
    INTEGER("#,##0"),
    TWO_DECIMAL("#,##0.00"),
    THREE_DECIMAL("#,##0.000"),
    ZERO_DECIMAL("0"),
    PLAIN_TWO_DECIMAL("0.00");

    private final String format;

    NumberPrecisionFormat(String format) {
        this.format = format;
    }

    public String format() {
        return format;
    }
}
