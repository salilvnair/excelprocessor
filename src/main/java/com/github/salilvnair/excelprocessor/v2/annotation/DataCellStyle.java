package com.github.salilvnair.excelprocessor.v2.annotation;


import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface DataCellStyle {
    boolean conditional() default false;

    String condition() default "";

    boolean applyDefaultStyles() default false;

    String customTask() default "";

    String[] customTasks() default {};

    boolean hasForegroundColor() default false;

    boolean hasBackgroundColor() default false;

    boolean hasBorderStyle() default false;

    boolean hasBorderColor() default false;

    boolean wrapText() default false;

    IndexedColors foregroundColor() default IndexedColors.AUTOMATIC;

    IndexedColors backgroundColor() default IndexedColors.AUTOMATIC;

    FillPatternType fillPattern() default FillPatternType.SOLID_FOREGROUND;

    BorderStyle borderStyle() default BorderStyle.NONE;

    IndexedColors borderColor() default IndexedColors.AUTOMATIC;

    int columnWidthInUnits() default -1;

    boolean customTextStyle() default false;

    NumberStyle numberStyle() default @NumberStyle;

    TextStyle textStyle() default @TextStyle;

    StyleTemplateCell styleTemplateCell() default @StyleTemplateCell;

    boolean ignoreStyleTemplate() default false;
}
