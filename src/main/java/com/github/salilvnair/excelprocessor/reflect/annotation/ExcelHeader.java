package com.github.salilvnair.excelprocessor.reflect.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.github.salilvnair.excelprocessor.reflect.constant.ExcelHeaderConstant;
import com.github.salilvnair.excelprocessor.reflect.helper.ExcelDateFormat;
import com.github.salilvnair.excelprocessor.reflect.type.PictureAnchorType;
import com.github.salilvnair.excelprocessor.reflect.type.PictureSourceType;
import com.github.salilvnair.excelprocessor.reflect.type.PictureType;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ExcelHeader {
	String value() default "";
	String column() default "";
	int row() default -1;
	String numberFormat() default ExcelHeaderConstant.COLUMN_VALUE_NUMERIC_LONG;
	String toExcelDateFormat() default ExcelDateFormat.DASH_MM_DD_YYYY;
	String[] fromExcelDateFormats() default {ExcelDateFormat.DASH_MM_DD_YYYY,
									ExcelDateFormat.DASH_DD_MM_YYYY,
									ExcelDateFormat.SLASH_DD_MM_YYYY,
									ExcelDateFormat.SLASH_MM_DD_YYYY};
	boolean picture() default false;
	PictureType pictureType() default PictureType.JPEG;
	PictureSourceType pictureSource() default PictureSourceType.FILE_PATH; 
	PictureAnchorType pictureAnchorType() default PictureAnchorType.DONT_MOVE_AND_RESIZE;
	double pictureResizeScale() default 1.0;
}
