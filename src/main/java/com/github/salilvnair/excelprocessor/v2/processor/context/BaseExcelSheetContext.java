package com.github.salilvnair.excelprocessor.v2.processor.context;

import org.apache.poi.ss.usermodel.Workbook;

import java.io.InputStream;

public class BaseExcelSheetContext {
    private InputStream excelFileInputStream;
    private String fileName;
    private Workbook workbook;

    public InputStream getExcelFileInputStream() {
        return excelFileInputStream;
    }

    public void setExcelFileInputStream(InputStream excelFileInputStream) {
        this.excelFileInputStream = excelFileInputStream;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Workbook getWorkbook() {
        return workbook;
    }

    public void setWorkbook(Workbook workbook) {
        this.workbook = workbook;
    }
}
