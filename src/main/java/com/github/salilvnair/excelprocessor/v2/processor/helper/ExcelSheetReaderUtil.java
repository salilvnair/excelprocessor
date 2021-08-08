package com.github.salilvnair.excelprocessor.v2.processor.helper;

import com.github.salilvnair.excelprocessor.util.AnnotationUtil;
import com.github.salilvnair.excelprocessor.v1.reflect.constant.ExcelValidatorConstant;
import com.github.salilvnair.excelprocessor.v2.annotation.Cell;
import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.helper.StringUtils;
import com.github.salilvnair.excelprocessor.v2.processor.constant.SheetProcessingCommonConstant;
import com.github.salilvnair.excelprocessor.v2.processor.context.BaseExcelSheetContext;
import com.github.salilvnair.excelprocessor.v2.processor.context.ExcelSheetReaderContext;
import com.github.salilvnair.excelprocessor.v2.service.ExcelSheetReader;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
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

    public static String deleteJavaInValidVariables(String before) {
        String javaInValidVariables = "[^0-9a-zA-Z]";
        return before.replaceAll(javaInValidVariables, "");
    }

    public static String toCamelCase(String s) {
        if(s.length()>1){
            s = s.substring(0, 1).toUpperCase()+s.substring(1);
        }
        return s;
    }

    public static String toPascalCase(String s) {
        String [] spacedString = s.split(" ");
        String finalString = s;
        if(spacedString.length>0) {
            StringBuilder camelCaseBuilder = new StringBuilder();
            for(int i=0;i<spacedString.length;i++) {
                if(spacedString[i].length()>0){
                    if(i==0) {
                        camelCaseBuilder.append(spacedString[i].toLowerCase());
                    }
                    else {
                        camelCaseBuilder.append(spacedString[i].substring(0, 1).toUpperCase()).append(spacedString[i].substring(1).toLowerCase());
                    }
                }
            }
            finalString = camelCaseBuilder.toString();
            String hasVariable = "#";
            finalString = finalString.replace(hasVariable, "No");
            finalString = finalString.substring(0, 1).toLowerCase()+finalString.substring(1);
        }
        return finalString;
    }

    public static String processSimilarHeaderString(String headerString, Class<? extends BaseSheet> clazz, int columnIndex, int rowIndex) {
        if(clazz.isAnnotationPresent(Sheet.class)) {
            Sheet sheet = clazz.getAnnotation(Sheet.class);
            if(sheet.duplicateHeaders()) {
                Set<Field> fields = AnnotationUtil.getAnnotatedFields(clazz, Cell.class);
                for(Field field:fields) {
                    Cell cell = field.getAnnotation(Cell.class);
                    if(!ExcelValidatorConstant.EMPTY_STRING.equals(cell.value())
                            && cell.value().equals(headerString)) {
                        if(sheet.vertical()) {
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
                if(sheet.vertical()) {
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

    public static String processSimilarHeaderString(String headerString, Class<?> clazz, Cell cell) {
        if(clazz.isAnnotationPresent(Sheet.class)) {
            Sheet sheet = clazz.getAnnotation(Sheet.class);
            if(sheet.duplicateHeaders()) {
                if(sheet.vertical()) {
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

    public static String cleanAndProcessSimilarHeaderString(String headerString, Class<?> clazz, Cell cell) {
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

    public static String processSimilarHeaderString(String headerString, Class<? extends BaseSheet> clazz, int c, int r, List<String> headerStringList) {
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

    public static Workbook extractWorkbook(BaseExcelSheetContext context) {
        Workbook workbook = null;
        if(context.getWorkbook() == null) {
            try {
                workbook = generateWorkbook(context.getExcelFileInputStream(), context.getFileName());
            }
            catch (Exception ignored) {
                return null;
            }
        }
        else  {
            workbook = context.getWorkbook();
        }
        return workbook;
    }

    public static Workbook generateWorkbook(InputStream inputStream, String excelFilePath) throws Exception {
        Workbook workbook;
        if (excelFilePath.endsWith("xlsx") || excelFilePath.endsWith("xlsm")) {
            workbook = new XSSFWorkbook(inputStream);
        }
        else if (excelFilePath.endsWith("xls")) {
            workbook = new HSSFWorkbook(inputStream);
        }
        else {
            throw new IllegalArgumentException("The specified file is not Excel file");
        }
        return workbook;
    }

    public static InputStream resourceStream(String folder, String fileName) {
        ClassLoader classLoader = ExcelSheetReader.class.getClassLoader();
        return classLoader.getResourceAsStream(folder+"/"+fileName);
    }
}
