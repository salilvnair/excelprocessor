package com.github.salilvnair.excelprocessor.v1.reflect.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.github.salilvnair.excelprocessor.v1.reflect.constant.ExcelSheetConstant;
import com.github.salilvnair.excelprocessor.v1.reflect.service.AbstractCustomValidatorTask;
import org.apache.poi.ss.usermodel.IndexedColors;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ExcelSheet {
	String value() default "";
	String type() default ExcelSheetConstant.EXCEL_FILE_TYPE_XLS;
	Class<? extends AbstractCustomValidatorTask> customTaskValidator() default AbstractCustomValidatorTask.class;
	boolean hasValidation() default false;
	boolean isVertical() default false;
	boolean isSingleValueVerticalSheet() default false;
	boolean verticallyScatteredHeaders() default false;
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
	boolean commentCellWithError() default false;
	boolean highlightCellWithError() default false;
	IndexedColors highlightedErrorCellColor() default IndexedColors.AUTOMATIC;
}
