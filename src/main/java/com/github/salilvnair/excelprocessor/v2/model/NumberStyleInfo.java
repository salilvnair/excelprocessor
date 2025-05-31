package com.github.salilvnair.excelprocessor.v2.model;

import com.github.salilvnair.excelprocessor.v2.type.DateFormatPattern;
import com.github.salilvnair.excelprocessor.v2.type.NumberCategoryFormat;
import com.github.salilvnair.excelprocessor.v2.type.NumberPrecisionFormat;
import lombok.*;
import org.apache.poi.ss.usermodel.IndexedColors;

@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class NumberStyleInfo {
    private boolean general;
    private boolean number;
    private boolean currency;
    private boolean accounting;
    private boolean date;
    private boolean time;
    private boolean percentage;
    private boolean fraction;
    private boolean scientific;
    private boolean text;
    private boolean custom;

    private NumberCategoryFormat categoryFormat = NumberCategoryFormat.GENERAL;

    private String customFormat = "";

    private DateFormatPattern dateFormat = DateFormatPattern.SLASH_MM_DD_YYYY;

    private NumberPrecisionFormat numberFormat = NumberPrecisionFormat.TWO_DECIMAL;

    public boolean general() {
        return general;
    }
    public boolean number() {
        return number;
    }
    public boolean currency() {
        return currency;
    }
    public boolean accounting() {
        return accounting;
    }
    public boolean date() {
        return date;
    }
    public boolean time() {
        return time;
    }
    public boolean percentage() {
        return percentage;
    }
    public boolean fraction() {
        return fraction;
    }
    public boolean scientific() {
        return scientific;
    }
    public boolean text() {
        return text;
    }
    public boolean custom() {
        return custom;
    }
    public NumberCategoryFormat categoryFormat() {
        return categoryFormat;
    }
    public String customFormat() {
        return customFormat;
    }
    public DateFormatPattern dateFormat() {
        return dateFormat;
    }
    public NumberPrecisionFormat numberFormat() {
        return numberFormat;
    }
}
