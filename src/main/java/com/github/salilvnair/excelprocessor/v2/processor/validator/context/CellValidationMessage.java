package com.github.salilvnair.excelprocessor.v2.processor.validator.context;

import com.github.salilvnair.excelprocessor.v2.processor.validator.type.ValidatorType;
import com.google.gson.Gson;

public class CellValidationMessage {
    private String header;
    private String originalHeader;
    private String mappedFieldName;
    private String message;
    private String messageId;
    private String  messageType;
    private int row;
    private String column;
    private String sheet;
    private ValidatorType validatorType;

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

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getSheet() {
        return sheet;
    }

    public void setSheet(String sheet) {
        this.sheet = sheet;
    }

    public ValidatorType getValidatorType() {
        return validatorType;
    }

    public void setValidatorType(ValidatorType validatorType) {
        this.validatorType = validatorType;
    }

    public String getOriginalHeader() {
        return originalHeader;
    }

    public void setOriginalHeader(String originalHeader) {
        this.originalHeader = originalHeader;
    }

    @Override
    public String toString() {
        return "\n"+new Gson().toJson(this);
    }
}
