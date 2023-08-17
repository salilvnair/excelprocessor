package com.github.salilvnair.excelprocessor.v2.annotation;

import com.github.salilvnair.excelprocessor.util.DateParsingUtil;
import com.github.salilvnair.excelprocessor.v2.type.PictureAnchorType;
import com.github.salilvnair.excelprocessor.v2.type.PictureSourceType;
import com.github.salilvnair.excelprocessor.v2.type.PictureType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface StyleTemplateCell {
    int row() default 1;
    String column() default "A";
}
