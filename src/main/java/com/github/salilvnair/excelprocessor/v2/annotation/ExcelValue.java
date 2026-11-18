package com.github.salilvnair.excelprocessor.v2.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Text that represents an enum constant in a sheet. Without it a cell matches an enum constant by
 * name, ignoring case and treating spaces and hyphens as underscores.
 *
 * <pre>{@code
 * enum Status {
 *     @ExcelValue("In progress") IN_PROGRESS,
 *     @ExcelValue({"Done", "Closed"}) DONE
 * }
 * }</pre>
 *
 * Writers write the first label instead of the constant name.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ExcelValue {
    String[] value();
}
