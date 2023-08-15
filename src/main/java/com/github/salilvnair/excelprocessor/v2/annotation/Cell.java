package com.github.salilvnair.excelprocessor.v2.annotation;

import com.github.salilvnair.excelprocessor.util.DateParsingUtil;
import com.github.salilvnair.excelprocessor.v2.type.PictureAnchorType;
import com.github.salilvnair.excelprocessor.v2.type.PictureSourceType;
import com.github.salilvnair.excelprocessor.v2.type.PictureType;
import org.apache.poi.ss.usermodel.IndexedColors;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Cell {
    DateParsingUtil.DateFormat dateFormat() default DateParsingUtil.DateFormat.SLASH_MM_DD_YYYY;

    DateParsingUtil.DateTimeFormat dateTimeFormat() default DateParsingUtil.DateTimeFormat.SLASH_MM_DD_YYYY_HH_MM;

    boolean dateString() default false;

    boolean dateTimeString() default false;

	String value() default "";

	String column() default "";

    String[] metaData() default {};

	int row() default -1;

    boolean picture() default false;

    boolean multiPicture() default false;

    int pictureMarginInPixels() default 0;

    int pictureHeightInPixels() default 100;

    int pictureWidthInPixels() default 100;

    PictureType pictureType() default PictureType.JPEG;

    PictureSourceType pictureSource() default PictureSourceType.FILE_PATH;

    PictureAnchorType pictureAnchorType() default PictureAnchorType.MOVE_AND_RESIZE;

    double pictureResizeScale() default 1.0;

    boolean hyperLink() default false;
    String hyperLinkText() default "";

    boolean multiHyperLink() default false;
}
