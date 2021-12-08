package com.github.salilvnair.excelprocessor.v2.annotation;


import com.github.salilvnair.excelprocessor.v2.processor.validator.type.MessageType;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(FIELD)
public @interface ConditionallyAllowedValues {
	String condition() default "";
	int[] range() default {};
	String[] value() default {};
	String dataSetKey() default "";
	boolean allowNull() default false;
	boolean allowEmpty() default false;
	boolean showValuesInMessage() default false;
	String message() default "";
	String messageId() default "";
	MessageType messageType() default MessageType.ERROR;
}
