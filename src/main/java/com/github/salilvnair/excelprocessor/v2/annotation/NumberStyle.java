package com.github.salilvnair.excelprocessor.v2.annotation;

import com.github.salilvnair.excelprocessor.v2.type.DateFormatPattern;
import com.github.salilvnair.excelprocessor.v2.type.NumberCategoryFormat;
import com.github.salilvnair.excelprocessor.v2.type.NumberPrecisionFormat;
import org.apache.poi.ss.usermodel.IndexedColors;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface NumberStyle {
    boolean general() default true;
    boolean number() default false;
    boolean currency() default false;
    boolean accounting() default false;
    boolean date() default false;
    boolean time() default false;
    boolean percentage() default false;
    boolean fraction() default false;
    boolean scientific() default false;
    boolean text() default false;
    boolean custom() default false;

    NumberCategoryFormat categoryFormat() default NumberCategoryFormat.GENERAL;

    String customFormat() default "";

    DateFormatPattern dateFormat() default DateFormatPattern.SLASH_MM_DD_YYYY;

    NumberPrecisionFormat numberFormat() default NumberPrecisionFormat.TWO_DECIMAL;
}
