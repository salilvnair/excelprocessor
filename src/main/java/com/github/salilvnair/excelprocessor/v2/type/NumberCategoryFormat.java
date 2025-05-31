package com.github.salilvnair.excelprocessor.v2.type;

public enum NumberCategoryFormat {
    GENERAL("General"),
    NUMBER_COMMA_DECIMAL("#,##0.00"),
    INTEGER("#,##0"),
    CURRENCY_USD("$#,##0.00"),
    ACCOUNTING("_($* #,##0.00_);_($* (#,##0.00);_($* \"-\"??_);_(@_)"),
    DATE_SHORT("mm/dd/yyyy"),
    DATE_LONG("mmmm d, yyyy"),
    TIME_SHORT("h:mm AM/PM"),
    TIME_LONG("h:mm:ss AM/PM"),
    PERCENTAGE("0%"),
    FRACTION_SIMPLE("# ?/?"),
    SCIENTIFIC("0.00E+00"),
    TEXT("@"),
    ZIP_CODE("00000"),
    ZIP_CODE_PLUS_FOUR("00000-0000"),
    PHONE_NUMBER("(###) ###-####"),
    SSN("000-00-0000");

    private final String format;

    NumberCategoryFormat(String format) {
        this.format = format;
    }

    public String format() {
        return format;
    }
}