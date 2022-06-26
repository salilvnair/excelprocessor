package com.github.salilvnair.excelprocessor.v2.exception;

/**
 * @author Salil V Nair
 */
public class ExcelSheetWriterException extends RuntimeException {
    public ExcelSheetWriterException(String string) {
        super(string);
    }

    public ExcelSheetWriterException(Throwable e) {
        super(e);
    }
}
