package com.github.salilvnair.excelprocessor.v2.processor.context;

import lombok.Getter;
import lombok.Setter;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.InputStream;

@Setter
@Getter
public class BaseExcelSheetContext {
    private InputStream excelFileInputStream;
    private String fileName;
    private Workbook workbook;
}
