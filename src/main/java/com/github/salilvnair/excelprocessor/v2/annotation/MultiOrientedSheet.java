package com.github.salilvnair.excelprocessor.v2.annotation;

import com.github.salilvnair.excelprocessor.v2.sheet.BaseExcelSheet;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Salil V Nair
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface MultiOrientedSheet {
    String name();
    Class<? extends BaseExcelSheet>[] value();
}
