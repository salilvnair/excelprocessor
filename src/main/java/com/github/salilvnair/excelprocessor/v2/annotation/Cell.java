package com.github.salilvnair.excelprocessor.v2.annotation;

import com.github.salilvnair.excelprocessor.v1.reflect.constant.ExcelHeaderConstant;
import com.github.salilvnair.excelprocessor.util.ExcelDateFormat;
import com.github.salilvnair.excelprocessor.v1.reflect.type.PictureAnchorType;
import com.github.salilvnair.excelprocessor.v1.reflect.type.PictureSourceType;
import com.github.salilvnair.excelprocessor.v1.reflect.type.PictureType;
import org.apache.poi.ss.usermodel.IndexedColors;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Cell {
	String value() default "";
	String column() default "";
	int row() default -1;
}
