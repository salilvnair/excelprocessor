package com.github.salilvnair.excelprocessor.v2.exception;

/**
 * @author Salil V Nair
 */
public class ExcelSheetReaderException extends RuntimeException {
    public ExcelSheetReaderException(String string) {
        super(string);
    }

    public ExcelSheetReaderException(Throwable e) {
        super(e);
    }
}
