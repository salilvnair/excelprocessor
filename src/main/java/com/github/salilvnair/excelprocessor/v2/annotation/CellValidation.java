package com.github.salilvnair.excelprocessor.v2.annotation;


import com.github.salilvnair.excelprocessor.v2.processor.validator.type.MessageType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface CellValidation {
	boolean required() default false;
	boolean unique() default false;
	boolean conditional() default false;
	String condition() default "";
	int minLength() default -1;
	int maxLength() default -1;
	int length() default -1;
	String date() default "";
	String minDate() default "";
	String maxDate() default "";
	boolean email() default false;
	boolean nonNull() default true;
	boolean nonEmpty() default true;
	String pattern() default "";
	boolean matchPattern() default false;
	boolean findPattern() default true;
	boolean alphaNumeric() default false;
	boolean numeric() default false;
	boolean currency() default false;
	String customTask() default "";
	String[] customTasks() default {};
	String[] dependentHeaders() default {};
	String dependentHeaderKey() default "";	
	String userDefinedMessage() default "";
	String messageId()  default "";
	MessageType messageType() default MessageType.ERROR;
	UserDefinedMessage[] userDefinedMessages() default {};
}
