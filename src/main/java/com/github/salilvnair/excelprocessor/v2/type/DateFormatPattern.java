package com.github.salilvnair.excelprocessor.v2.type;

public enum DateFormatPattern {
    SLASH_MM_DD_YYYY("MM/dd/yyyy"),
    SLASH_DD_MM_YYYY("dd/MM/yyyy"),
    DASH_YYYY_MM_DD("yyyy-MM-dd"),
    FULL_DATE("MMMM d, yyyy"),
    MONTH_DAY_YEAR("MMM dd, yyyy");

    private final String format;

    DateFormatPattern(String format) {
        this.format = format;
    }

    public String format() {
        return format;
    }
}

