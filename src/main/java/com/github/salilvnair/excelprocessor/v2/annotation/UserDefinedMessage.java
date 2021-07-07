package com.github.salilvnair.excelprocessor.v2.annotation;


import com.github.salilvnair.excelprocessor.v2.processor.validator.type.MessageType;
import com.github.salilvnair.excelprocessor.v2.processor.validator.type.ValidatorType;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(METHOD)
public @interface UserDefinedMessage {
	ValidatorType validatorType();
	String message() default "";
	String messageId()  default "";
	MessageType messageType() default MessageType.ERROR;
}