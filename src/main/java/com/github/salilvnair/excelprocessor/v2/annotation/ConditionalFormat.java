package com.github.salilvnair.excelprocessor.v2.annotation;

import org.apache.poi.ss.usermodel.IndexedColors;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A native Excel conditional-formatting rule for a column, written once for all data rows. Excel
 * evaluates it, so it costs nothing per row, works with streaming writes and stays correct when
 * users edit values. Repeat the annotation for several rules; the first matching rule wins.
 *
 * <pre>{@code
 * @ConditionalFormat(operator = Operator.LESS_THAN, value = "0", fontColor = IndexedColors.RED)
 * @ConditionalFormat(operator = Operator.BETWEEN, value = "0", value2 = "100", backgroundColor = IndexedColors.LIGHT_GREEN)
 * @Cell("Balance")
 * private Double balance;
 *
 * @ConditionalFormat(formula = "{cell}<TODAY()", backgroundColor = IndexedColors.ROSE)  // whole formula
 * @Cell("Due date")
 * private Date dueDate;
 * }</pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Repeatable(ConditionalFormats.class)
public @interface ConditionalFormat {

    enum Operator { EQUAL, NOT_EQUAL, GREATER_THAN, GREATER_OR_EQUAL, LESS_THAN, LESS_OR_EQUAL, BETWEEN, NOT_BETWEEN }

    /** comparison with the cell value; ignored when {@link #formula()} is set */
    Operator operator() default Operator.EQUAL;

    /** first operand: a number, a quoted text such as {@code "\"Open\""}, or a formula */
    String value() default "";

    /** second operand for BETWEEN and NOT_BETWEEN */
    String value2() default "";

    /**
     * Formula rule that formats the cell when it is TRUE. {@code {cell}} stands for this column's
     * cell in the first data row (relative reference, so it moves with each row), e.g.
     * {@code "AND({cell}<>\"\",{cell}<TODAY())"}.
     */
    String formula() default "";

    IndexedColors backgroundColor() default IndexedColors.AUTOMATIC;

    IndexedColors fontColor() default IndexedColors.AUTOMATIC;

    boolean bold() default false;

    boolean italic() default false;
}
