package com.github.salilvnair.excelprocessor.v2.annotation;

import com.github.salilvnair.excelprocessor.v1.reflect.constant.ExcelSheetConstant;
import com.github.salilvnair.excelprocessor.v2.processor.validator.task.core.AbstractExcelTaskValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Sheet {
	String value() default "";
	String type() default ExcelSheetConstant.EXCEL_FILE_TYPE_XLS;
	Class<? extends AbstractExcelTaskValidator> excelTaskValidator() default DefaultTaskValidator.class;
	boolean hasValidation() default false;
	boolean isVertical() default false;
	boolean ignoreUnknown() default true;
	String customTask() default "";
	String[] customTasks() default {};
	String[] ignoreHeaders() default {};
	int[] ignoreRows() default {};
	String headerColumnAt() default "A";
	String headerColumnBeginsAtText() default "";
	String headerColumnEndsAtText() default "";
	String valueColumnAt() default "";
	String valueColumnBeginsAt() default "";
	String valueColumnEndsAt() default "";
	int headerRowAt() default 1;
	String headerRowBeginsAtText() default "";
	String headerRowEndsAtText() default "";
	int valueRowAt() default -1;
	int valueRowBeginsAt() default -1;
	int valueRowEndsAt() default -1;
	String messageDelimiter() default ",";
	boolean hasDuplicateHeaders() default false;
	boolean dynamicHeaders() default false;
	String userDefinedMessage() default "";
	final static class DefaultTaskValidator extends AbstractExcelTaskValidator {}
}
