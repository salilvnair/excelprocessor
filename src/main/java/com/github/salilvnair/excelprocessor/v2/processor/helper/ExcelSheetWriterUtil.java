package com.github.salilvnair.excelprocessor.v2.processor.helper;

import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.processor.context.BaseExcelSheetContext;
import com.github.salilvnair.excelprocessor.v2.type.ExcelFileType;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * @author Salil V Nair
 */
public class ExcelSheetWriterUtil {
    private ExcelSheetWriterUtil(){}
    private static final Object staticFileCreatorObjectLock = new Object();

    public static Workbook generateWorkbook(Sheet sheet) {
        if(ExcelFileType.Extension.XLSX.equals(sheet.type())) {
            return new XSSFWorkbook();
        }
        return new HSSFWorkbook();
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

    public static void write(Workbook workbook, String fileName, String filePath) throws Exception{
        File destinationFile = null;
        synchronized (staticFileCreatorObjectLock) {
            destinationFile = new File(new File(filePath), fileName);
        }
        FileOutputStream fout = new FileOutputStream(destinationFile);
        workbook.write(fout);
        fout.close();
    }

    public static void write(Workbook workbook, File file) throws Exception{
        File destinationFile = null;
        synchronized (staticFileCreatorObjectLock) {
            destinationFile = file;
        }
        FileOutputStream fout=new FileOutputStream(destinationFile);
        workbook.write(fout);
        fout.close();
    }
}
