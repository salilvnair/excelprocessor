package com.github.salilvnair.excelprocessor.v2.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Section {
	String value() default "";
	int headerRowBeginsAt() default -1;
	int headerRowEndsAt() default -1;
	String beginningText() default "";
	String endingText() default "";
 }
