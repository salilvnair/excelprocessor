package com.github.salilvnair.excelprocessor.v1.reflect.context;

public class ExcelValidationMessage {
    private String header;
    private String mappedFieldName;
    private String message;
    private int row;
    private String column;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getMappedFieldName() {
        return mappedFieldName;
    }

    public void setMappedFieldName(String mappedFieldName) {
        this.mappedFieldName = mappedFieldName;
    }
}
