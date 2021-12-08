package com.github.salilvnair.excelprocessor.v2.annotation;

import com.github.salilvnair.excelprocessor.util.DateParsingUtil;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Cell {
    DateParsingUtil.DateFormat dateFormat() default DateParsingUtil.DateFormat.SLASH_MM_DD_YYYY;
    DateParsingUtil.DateTimeFormat dateTimeFormat() default DateParsingUtil.DateTimeFormat.SLASH_MM_DD_YYYY_HH_MM;
    boolean dateString() default false;
    boolean dateTimeString() default false;
	String value() default "";
	String column() default "";
	int row() default -1;
}
