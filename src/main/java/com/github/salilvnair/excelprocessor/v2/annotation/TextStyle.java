package com.github.salilvnair.excelprocessor.v2.annotation;

import org.apache.poi.ss.usermodel.IndexedColors;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface TextStyle {
    IndexedColors color() default IndexedColors.AUTOMATIC;

    boolean italic() default false;

    boolean bold() default false;

    boolean strikeout() default false;

    short fontHeight() default -1;

    String fontName() default "";
}
