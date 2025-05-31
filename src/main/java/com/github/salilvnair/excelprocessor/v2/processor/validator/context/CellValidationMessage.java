package com.github.salilvnair.excelprocessor.v2.processor.validator.context;

import com.github.salilvnair.excelprocessor.v2.processor.validator.type.ValidatorType;
import com.google.gson.Gson;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
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

    @Override
    public String toString() {
        return "\n"+new Gson().toJson(this);
    }
}
