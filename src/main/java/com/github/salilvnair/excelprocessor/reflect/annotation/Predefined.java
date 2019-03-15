package com.github.salilvnair.excelprocessor.reflect.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Predefined {
	boolean allowNull() default false;
	boolean allowEmpty() default false;
	String[] predefinedValues() default {};
	String predefinedDatasetKey() default "";
	String messageDescription() default "";
}
