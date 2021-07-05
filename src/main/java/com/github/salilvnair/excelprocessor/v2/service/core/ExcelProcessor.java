package com.github.salilvnair.excelprocessor.v2.service.core;

import com.github.salilvnair.excelprocessor.v2.sheet.BaseExcelSheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public interface ExcelProcessor {
  default <T extends BaseExcelSheet> List<T> read(Class<T> clazz) throws Exception { return Collections.emptyList();}
  default Map<String, List<? extends BaseExcelSheet>> read(String[] fullyQualifiedClassNames) throws Exception { return Collections.emptyMap();}
  default Map<String, List<? extends BaseExcelSheet>> read(Class<? extends BaseExcelSheet>[] classes) throws Exception { return Collections.emptyMap();}
  default <T extends BaseExcelSheet> void write(List<T> sheet, String filePath) {}
  default void write(Map<String, List<? extends BaseExcelSheet>> sheet, String filePath) {}
  default <T extends BaseExcelSheet> Workbook workbook(List<T> sheet) { return null;}
  default Workbook workbook(Map<String, List<? extends BaseExcelSheet>> sheet) {return null;}
}
