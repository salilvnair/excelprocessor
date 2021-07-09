package com.github.salilvnair.excelprocessor.v2.processor.core;

import com.github.salilvnair.excelprocessor.v2.processor.context.ExcelSheetContext;
import com.github.salilvnair.excelprocessor.v2.processor.context.ExcelSheetReaderContext;
import com.github.salilvnair.excelprocessor.v2.processor.validator.context.CellValidationMessage;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;
import com.github.salilvnair.excelprocessor.v2.type.ExcelInfo;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public interface ExcelSheetReader {

  default <T extends BaseSheet> ExcelInfo excelInfo(Class<T> clazz, ExcelSheetContext sheetContext)throws Exception { return null;};

  default ExcelInfo excelInfo(String[] fullyQualifiedClassNames, ExcelSheetContext sheetContext) throws Exception { return null;}

  default <T extends BaseSheet> List<T> read(Class<T> clazz, ExcelSheetContext sheetContext) throws Exception { return Collections.emptyList();}

  default <T extends BaseSheet> Map<String, List<? extends BaseSheet>> read(Class<T> clazz, boolean multiOriented, ExcelSheetContext sheetContext) throws Exception { return Collections.emptyMap();}

  default Map<String, List<? extends BaseSheet>> read(String[] fullyQualifiedClassNames, ExcelSheetContext sheetContext) throws Exception { return Collections.emptyMap();}

  default Map<String, List<? extends BaseSheet>> read(Class<? extends BaseSheet>[] classes, ExcelSheetContext sheetContext) throws Exception { return Collections.emptyMap();}

  default List<CellValidationMessage> validate(List<? extends BaseSheet> sheetData, ExcelSheetContext sheetContext) throws Exception { return Collections.emptyList();}

  default Map<String, List<CellValidationMessage>> validate(Map<String, List<? extends BaseSheet>> excelData, ExcelSheetContext sheetContext) throws Exception { return Collections.emptyMap();}

  default void readAndValidate(Class<? extends BaseSheet> clazz, ExcelSheetContext sheetContext) throws Exception {}

  default void readAndValidate(String[] fullyQualifiedClassNames, ExcelSheetContext sheetContext) throws Exception {}

  default void readAndValidate(Class<? extends BaseSheet>[] classes, ExcelSheetContext sheetContext) throws Exception {}


  static Workbook extractWorkbook(ExcelSheetReaderContext context) {
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

  static Workbook generateWorkbook(InputStream inputStream, String excelFilePath) throws Exception {
    Workbook workbook;
    if (excelFilePath.endsWith("xlsx")) {
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

  static InputStream resourceStream(String folder, String fileName) {
    ClassLoader classLoader = ExcelSheetReader.class.getClassLoader();
    return classLoader.getResourceAsStream(folder+"/"+fileName);
  }
  static  int toIndentNumber(String name) {
    int number = 0;
    for (int i = 0; i < name.length(); i++) {
      number = number * 26 + (name.charAt(i) - ('A' - 1));
    }
    return number;
  }

  static String toIndentName(int number) {
    StringBuilder sb = new StringBuilder();
    while (number-- > 0) {
      sb.append((char)('A' + (number % 26)));
      number /= 26;
    }
    return sb.reverse().toString();
  }
}
