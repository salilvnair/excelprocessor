package com.github.salilvnair.excelprocessor.v2.annotation;


import com.github.salilvnair.excelprocessor.v2.processor.validator.type.MessageType;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(FIELD)
public @interface SectionHint {
	String beginningTextLike() default "";
	String endingTextLike() default "";
    boolean findClosestMatch() default false;
}
