package com.github.salilvnair.excelprocessor.v2.exception;

/**
 * @author Salil V Nair
 */
public class ExcelSheetWriteException extends RuntimeException {
    public ExcelSheetWriteException(String string) {
        super(string);
    }

    public ExcelSheetWriteException(Throwable e) {
        super(e);
    }
}
