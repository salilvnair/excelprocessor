package com.github.salilvnair.excelprocessor.v2.exception;

/**
 * @author Salil V Nair
 */
public class ExcelSheetReadException extends RuntimeException {
    public ExcelSheetReadException(String string) {
        super(string);
    }

    public ExcelSheetReadException(Throwable e) {
        super(e);
    }
}
