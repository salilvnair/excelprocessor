package com.github.salilvnair.excelprocessor.v2.annotation;

import com.github.salilvnair.excelprocessor.v2.processor.validator.task.core.AbstractExcelTaskValidator;
import com.github.salilvnair.excelprocessor.v2.task.AbstractExcelTask;
import com.github.salilvnair.excelprocessor.v2.type.ExcelFileType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Sheet {
	String value() default "";
	String type() default ExcelFileType.Extension.XLSX;
    String excelTaskValidatorBeanName() default "";
    String excelTaskBeanName() default "";
	Class<? extends AbstractExcelTaskValidator> excelTaskValidator() default DefaultTaskValidator.class;
    Class<? extends AbstractExcelTask> excelTask() default DefaultTask.class;
	boolean vertical() default false;
	boolean userDefinedTemplate() default false;
	boolean ignoreUnknown() default true;
	String customTask() default "";
	String[] customTasks() default {};
	String[] ignoreHeaders() default {};
	String[] ignoreHeaderPatterns() default {};
	int[] ignoreRows() default {};
	String headerColumnAt() default "A";
	String headerColumnEndsAt() default "";
	String headerColumnBeginsAtText() default "";
	String headerColumnEndsAtText() default "";
	String valueColumnAt() default "";
	String valueColumnBeginsAt() default "";
	String valueColumnEndsAt() default "";
	int headerRowAt() default 1;
	int headerRowEndsAt() default -1;
	boolean mergedHeaders() default false;
	boolean sectional() default false;
	boolean ordered() default false;
	int mergedHeaderRowAt() default -1;
	String headerRowBeginsAtText() default "";
	String headerRowEndsAtText() default "";
	int valueRowAt() default -1;
	int valueRowBeginsAt() default -1;
	int valueRowEndsAt() default -1;
	String messageDelimiter() default ",";
	boolean duplicateHeaders() default false;
	boolean dynamicHeaders() default false;
	String userDefinedMessage() default "";
	final class DefaultTaskValidator extends AbstractExcelTaskValidator {};
	final class DefaultTask extends AbstractExcelTask {};
    SectionHint[] sectionHints() default {};
    boolean streamingWorkbook() default false;
    /** rows kept in memory per sheet when {@link #streamingWorkbook()} is true (SXSSF window) */
    int streamingRowWindowSize() default 100;
    boolean readValuesAsString() default false;
    boolean useDefaultNumberType() default false;
    boolean multiPositionalHeaders() default false;
    Class<? extends Number> defaultNumberType() default Long.class;

    String[] hideColumns() default {};

    /** writing: keep the header row (and the rows above it) visible while scrolling */
    boolean freezeHeader() default false;

    /** writing: add Excel filter buttons to the header row, covering the written rows */
    boolean autoFilter() default false;

    /** writing: size each written column to its content (streaming: measured over every written row) */
    boolean autoSizeColumns() default false;

    /** writing: height of the header row in points; -1 keeps Excel's default */
    float headerRowHeight() default -1;

    /**
     * writing: turn {@code @AllowedValues} (values, {@code dataSetKey} from the context's
     * validValuesDataSet, or an int {@code range}) and enum fields into Excel dropdowns or number
     * limits on the data rows, so a blank template already offers the valid choices. Dynamic headers
     * use the context's {@code headerKeyedCellValidationInfo}.
     */
    boolean allowedValuesDropdown() default false;
}
