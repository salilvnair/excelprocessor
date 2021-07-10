package com.github.salilvnair.excelprocessor.v2.processor.helper;

import com.github.salilvnair.excelprocessor.util.AnnotationUtil;
import com.github.salilvnair.excelprocessor.v1.reflect.constant.ExcelValidatorConstant;
import com.github.salilvnair.excelprocessor.v2.annotation.Cell;
import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.helper.StringUtils;
import com.github.salilvnair.excelprocessor.v2.processor.constant.SheetProcessingCommonConstant;
import com.github.salilvnair.excelprocessor.v2.processor.core.ExcelSheetReader;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * @author Salil V Nair
 */
public class ExcelSheetReaderUtil {
    private ExcelSheetReaderUtil(){}
    public static String cleanHeaderString(String headerKey) {
        headerKey = headerKey.replaceAll("\\r\\n|\\r|\\n", " ");
        headerKey = headerKey.trim();
        return headerKey;
    }

    public static String processSimilarHeaderString(String headerString, Class<? extends BaseSheet> clazz, int columnIndex, int rowIndex) {
        if(clazz.isAnnotationPresent(Sheet.class)) {
            Sheet sheet = clazz.getAnnotation(Sheet.class);
            if(sheet.hasDuplicateHeaders()) {
                Set<Field> fields = AnnotationUtil.getAnnotatedFields(clazz, Cell.class);
                for(Field field:fields) {
                    Cell cell = field.getAnnotation(Cell.class);
                    if(!ExcelValidatorConstant.EMPTY_STRING.equals(cell.value())
                            && cell.value().equals(headerString)) {
                        if(sheet.isVertical()) {
                            if((cell.row()-1) == rowIndex) {
                                return headerString+ SheetProcessingCommonConstant.UNDERSCORE+cell.row();
                            }
                        }
                        else {
                            String columnName = ExcelSheetReader.toIndentName(columnIndex+1);
                            if(cell.column().equals(columnName)) {
                                return headerString+SheetProcessingCommonConstant.UNDERSCORE+cell.column();
                            }
                        }
                    }
                }
            }
            else if(sheet.dynamicHeaders()) {
                if(sheet.isVertical()) {
                    return headerString+ SheetProcessingCommonConstant.UNDERSCORE+(rowIndex+1);
                }
                else {
                    String columnName = ExcelSheetReader.toIndentName(columnIndex+1);
                    return headerString+SheetProcessingCommonConstant.UNDERSCORE+columnName;
                }
            }
        }
        return headerString;
    }

    public static String processSimilarHeaderString(String headerString, Class<? extends BaseSheet> clazz, Cell cell) {
        if(clazz.isAnnotationPresent(Sheet.class)) {
            Sheet sheet = clazz.getAnnotation(Sheet.class);
            if(sheet.hasDuplicateHeaders()) {
                if(sheet.isVertical()) {
                    if(cell.row()!=-1) {
                        return headerString+ SheetProcessingCommonConstant.UNDERSCORE+cell.row();
                    }
                }
                else {
                    if(StringUtils.isNotEmpty(cell.column())) {
                        return headerString+ SheetProcessingCommonConstant.UNDERSCORE+cell.column();
                    }
                }
            }
        }
        return headerString;
    }

    public static String cleanAndProcessSimilarHeaderString(String headerString, Class<? extends BaseSheet> clazz, int c, int r) {
        return cleanAndProcessSimilarHeaderString(headerString, clazz, c, r, null);
    }

    public static String cleanAndProcessSimilarHeaderString(String headerString, Class<? extends BaseSheet> clazz, Cell cell) {
        headerString = cleanHeaderString(headerString);
        headerString = processSimilarHeaderString(headerString, clazz, cell);
        return headerString;
    }

    public static String cleanAndProcessSimilarHeaderString(String headerString, Class<? extends BaseSheet> clazz, int c, int r, List<String> headerStringList) {
        headerString = cleanHeaderString(headerString);
        if(headerStringList!=null && !headerStringList.isEmpty() && headerStringList.contains(headerString)) {
            headerString = processSimilarHeaderString(headerString, clazz, c, r);
        }
        return headerString;
    }

    public static boolean oneOfTheIgnoreHeaders(String headerString, Class<? extends BaseSheet> clazz) {
        if (clazz.isAnnotationPresent(Sheet.class)) {
            Sheet sheet = clazz.getAnnotation(Sheet.class);
            return Arrays.asList(sheet.ignoreHeaders()).contains(headerString);
        }
        return false;
    }
}
