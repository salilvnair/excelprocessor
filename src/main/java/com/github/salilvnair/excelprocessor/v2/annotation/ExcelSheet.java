package com.github.salilvnair.excelprocessor.v2.annotation;

import com.github.salilvnair.excelprocessor.reflect.constant.ExcelSheetConstant;
import com.github.salilvnair.excelprocessor.reflect.service.AbstractCustomValidatorTask;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ExcelSheet {
	String value() default "";
	String type() default ExcelSheetConstant.EXCEL_FILE_TYPE_XLS;
	Class<? extends AbstractCustomValidatorTask> customTaskValidator() default AbstractCustomValidatorTask.class;
	boolean hasValidation() default false;
	boolean isVertical() default false;
	boolean isSingleValueVerticalSheet() default false;
	boolean ignoreUnknown() default true;
	String customTask() default "";
	String[] customTasks() default {};
	String[] ignoreHeaders() default {};
	String ignoreHeaderKey() default "";
	String headerColumnAt() default "A";
	String valueColumnAt() default "B";
	String valueColumnBeginsAt() default "B";
	String valueColumnEndsAt() default "";
	int headerRowAt() default 1;
	int valueRowAt() default 2;
	int valueRowBeginsAt() default 2;
	int valueRowEndsAt() default -1;
	String messageDelimitter() default ",";
	boolean hasDuplicateHeaders() default false;
	String userDefinedMessage() default "";
	boolean containsPicture() default false;
}
