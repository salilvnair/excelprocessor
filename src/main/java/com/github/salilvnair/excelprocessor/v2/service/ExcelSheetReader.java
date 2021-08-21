package com.github.salilvnair.excelprocessor.v2.service;

import com.github.salilvnair.excelprocessor.v2.context.ExcelSheetContext;
import com.github.salilvnair.excelprocessor.v2.processor.validator.context.CellValidationMessage;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;
import com.github.salilvnair.excelprocessor.v2.type.ExcelInfo;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public interface ExcelSheetReader {

  default <T extends BaseSheet> ExcelInfo excelInfo(Class<T> clazz, ExcelSheetContext sheetContext)throws Exception { return null;};

  default ExcelInfo excelInfo(String[] fullyQualifiedClassNames, ExcelSheetContext sheetContext) { return null;}

  default <T extends BaseSheet> List<T> read(Class<T> clazz, ExcelSheetContext sheetContext) { return Collections.emptyList();}

  default <T extends BaseSheet> Map<String, List<? extends BaseSheet>> read(Class<T> clazz, boolean multiOriented, ExcelSheetContext sheetContext) { return Collections.emptyMap();}

  default Map<String, List<? extends BaseSheet>> read(String[] fullyQualifiedClassNames, ExcelSheetContext sheetContext) { return Collections.emptyMap();}

  default Map<String, List<? extends BaseSheet>> read(Class<? extends BaseSheet>[] classes, ExcelSheetContext sheetContext) { return Collections.emptyMap();}

  default List<CellValidationMessage> validate(List<? extends BaseSheet> sheetData, ExcelSheetContext sheetContext) { return Collections.emptyList();}

  default Map<String, List<CellValidationMessage>> validate(Map<String, List<? extends BaseSheet>> excelData, ExcelSheetContext sheetContext) { return Collections.emptyMap();}

  default void readAndValidate(Class<? extends BaseSheet> clazz, ExcelSheetContext sheetContext) {}

  default void readAndValidate(String[] fullyQualifiedClassNames, ExcelSheetContext sheetContext) {}

  default void readAndValidate(Class<? extends BaseSheet>[] classes, ExcelSheetContext sheetContext) {}

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
