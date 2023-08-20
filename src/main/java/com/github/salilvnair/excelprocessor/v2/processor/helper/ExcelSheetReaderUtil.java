package com.github.salilvnair.excelprocessor.v2.processor.helper;

import com.github.salilvnair.excelprocessor.util.AnnotationUtil;
import com.github.salilvnair.excelprocessor.v2.annotation.Cell;
import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.helper.StringUtils;
import com.github.salilvnair.excelprocessor.v2.processor.constant.SheetProcessingCommonConstant;
import com.github.salilvnair.excelprocessor.v2.processor.context.BaseExcelSheetContext;
import com.github.salilvnair.excelprocessor.v2.service.ExcelSheetReader;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;
import com.github.salilvnair.excelprocessor.v2.type.ExcelFileType;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        String validJavaString = before.replaceAll(javaInValidVariables, "");
        int index = 0;
        while(index < validJavaString.length() && Character.isDigit(validJavaString.charAt(index))) {
            index ++;
        }
        validJavaString = index == 0 ? validJavaString : validJavaString.substring(index) + validJavaString.substring(0 , index);
        return validJavaString;
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
                    if(StringUtils.isNotEmpty(cell.value())
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

    public static String processSimilarHeaderString(Sheet sheet, String headerString, List<Cell> cells, int columnIndex, int rowIndex) {
        if(sheet.sectional()) {
            for(Cell cell:cells) {
                if(StringUtils.isNotEmpty(cell.value())
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
        return headerString;
    }

    public static String processSimilarHeaderString( List<String> sheetHeaders, Sheet sheet, String headerString, int columnIndex, int rowIndex) {
        if(sheet.sectional() && sheetHeaders.contains(headerString)) {
            if(sheet.vertical()) {
                int cellRow = rowIndex + 1;
                return headerString+ SheetProcessingCommonConstant.UNDERSCORE+cellRow;
            }
            else {
                String columnName = ExcelSheetReader.toIndentName(columnIndex+1);
                return headerString+SheetProcessingCommonConstant.UNDERSCORE+columnName;
            }
        }
        return headerString;
    }


    public static String processSimilarHeaderString(String headerString, Class<?> clazz, Cell cell) {
        if(clazz.isAnnotationPresent(Sheet.class)) {
            Sheet sheet = clazz.getAnnotation(Sheet.class);
            return processSimilarHeaderString(sheet, headerString, cell);
        }
        return headerString;
    }

    public static String processSimilarHeaderString(Sheet sheet, Cell cell) {
        return processSimilarHeaderString(sheet, cell.value(), cell);
    }

    public static String processSimilarHeaderString(Sheet sheet, String headerString, Cell cell) {
        if(sheet.duplicateHeaders() || sheet.sectional()) {
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
        headerString = processSimilarHeaderString(headerString, clazz, c, r);
        return headerString;
    }

    public static String processSimilarHeaderString(String headerString, Class<? extends BaseSheet> clazz, int c, int r, List<String> headerStringList) {
        headerString = processSimilarHeaderString(headerString, clazz, c, r);
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

    public static Workbook generateWorkbook(InputStream inputStream, String excelFile) throws Exception {
        Workbook workbook;
        if (excelFile.endsWith(ExcelFileType.Extension.XLSX) || excelFile.endsWith(ExcelFileType.Extension.XLSM)) {
            workbook = new XSSFWorkbook(inputStream);
        }
        else if (excelFile.endsWith(ExcelFileType.Extension.XLS)) {
            workbook = new HSSFWorkbook(inputStream);
        }
        else {
            throw new IllegalArgumentException("The specified file is not Excel file");
        }
        inputStream.close();
        return workbook;
    }

    public static Workbook generateWorkbook(InputStream inputStream, ExcelFileType fileType) throws Exception {
        Workbook workbook;
        if (ExcelFileType.XLSX.equals(fileType) || ExcelFileType.XLSM.equals(fileType)) {
            workbook = new XSSFWorkbook(inputStream);
        }
        else if (ExcelFileType.XLS.equals(fileType)) {
            workbook = new HSSFWorkbook(inputStream);
        }
        else {
            throw new IllegalArgumentException("The specified file is not Excel file");
        }
        inputStream.close();
        return workbook;
    }

    public static InputStream resourceStream(String folder, String fileName) {
        ClassLoader classLoader = ExcelSheetReader.class.getClassLoader();
        return classLoader.getResourceAsStream(folder+"/"+fileName);
    }

    public static boolean containsMultipleHyperLinks(String input) {
        return countURLsInString(input) > 1;
    }

    public static int countURLsInString(String input) {
        String urlRegex = "\\b(?:https?|ftp)://\\S+\\b";
        Pattern pattern = Pattern.compile(urlRegex);
        Matcher matcher = pattern.matcher(input);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }

    public static List<String> extractHyperlinks(String input) {
        // Regular expression to match URLs
        String urlRegex = "\\b(?:https?|ftp)://\\S+\\b";
        Pattern pattern = Pattern.compile(urlRegex);
        Matcher matcher = pattern.matcher(input);

        // Create a StringBuilder to store the extracted hyperlinks
        List<String> urls = new ArrayList<>();

        // Iterate through the matches and append them to the StringBuilder
        while (matcher.find()) {
            urls.add(matcher.group());
        }
        return urls;
    }
}
