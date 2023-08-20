package com.github.salilvnair.excelprocessor.v2.processor.validator.core;

import com.github.salilvnair.excelprocessor.util.ObjectUtil;
import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.context.DynamicCellValidationContext;
import com.github.salilvnair.excelprocessor.v2.helper.StringUtils;
import com.github.salilvnair.excelprocessor.v2.model.*;
import com.github.salilvnair.excelprocessor.v2.processor.validator.context.CellValidationMessage;
import com.github.salilvnair.excelprocessor.v2.processor.validator.context.CellValidatorContext;
import com.github.salilvnair.excelprocessor.v2.processor.validator.type.ValidatorType;
import com.github.salilvnair.excelprocessor.v2.sheet.DynamicHeaderSheet;

import java.util.*;

public abstract class BaseDynamicCellValidator extends AbstractExcelValidator {
    private final String header;
    private String headerKey;
    private final DynamicCellValidationContext dynamicCellValidationContext;
    private final List<String> headerKeys = new ArrayList<>();;
    private final CellValidationInfo cellValidationInfo;
    public BaseDynamicCellValidator(DynamicCellValidationContext dynamicCellValidationContext) {
        this.dynamicCellValidationContext = dynamicCellValidationContext;
        this.header = dynamicCellValidationContext.getHeader();
        this.headerKey = dynamicCellValidationContext.getHeader();
        this.cellValidationInfo = dynamicCellValidationContext.getCellValidationInfo();
    }
    protected abstract boolean violated(Object fieldValue, Object currentInstance, CellValidatorContext validatorContext);
    protected abstract ValidatorType validatorType();
    protected String defaultMessage(Object fieldValue, Object currentInstance, CellValidatorContext validatorContext) {
        return null;
    }
    protected String headerKey() {
        return headerKey;
    }
    protected String header() {
        return header;
    }

    @Override
    public List<CellValidationMessage> validate(Object currentInstance, CellValidatorContext validatorContext) {
        DynamicHeaderSheet sheetObj = (DynamicHeaderSheet) currentInstance;
        List<CellValidationMessage> messages = new ArrayList<>();
        sheetObj.cells().forEach((k,v) -> {
            if(v.originalHeader().equals(header)) {
                headerKeys.add(k);
            }
        });
        for (String key : headerKeys) {
            headerKey = key;
            Object fieldValue = sheetObj.dynamicHeaderKeyedCellValueMap().get(headerKey);
            if(violated(fieldValue, currentInstance, validatorContext)) {
                List<CellValidationMessage> validationMessages = validationMessages(fieldValue, currentInstance, validatorContext);
                messages.addAll(validationMessages);
            }
        }
        return Collections.unmodifiableList(messages);
    }


    protected CellValidationMessage validationMessage(Object fieldValue, Object currentInstance, CellValidatorContext validatorContext) {
        CellValidationMessage validationMessage = new CellValidationMessage();
        String errorMessage = defaultMessage(fieldValue, currentInstance, validatorContext);
        String message = cellValidationInfo.getUserDefinedMessage();
        if(!StringUtils.isEmpty(message)) {
            errorMessage = message;
        }
        validationMessage.setMessage(errorMessage);
        validationMessage.setMessageId(cellValidationInfo.getMessageId());
        validationMessage.setMessageType(cellValidationInfo.getMessageType());
        UserDefinedMessageInfo[] userDefinedMessages = cellValidationInfo.getUserDefinedMessages();
        if(userDefinedMessages.length > 0) {
            Optional<UserDefinedMessageInfo> requiredDefinedMessage = Arrays
                                                                    .stream(userDefinedMessages)
                                                                    .filter(userDefinedMessage -> validatorType().equals(ValidatorType.type(userDefinedMessage.getValidatorType())))
                                                                    .findFirst();
            if(requiredDefinedMessage.isPresent()) {
                UserDefinedMessageInfo userDefinedMessage = requiredDefinedMessage.get();
                errorMessage = userDefinedMessage.getMessage();
                validationMessage.setMessage(errorMessage);
                validationMessage.setMessageId(userDefinedMessage.getMessageId());
                validationMessage.setMessageType(userDefinedMessage.getMessageType());
            }
        }
        validationMessage.setValidatorType(validatorType());
        return validationMessage;
    }
    protected List<CellValidationMessage> validationMessages(Object fieldValue, Object currentInstance, CellValidatorContext validatorContext) {
        Sheet sheet = currentInstance.getClass().getAnnotation(Sheet.class);
        List<CellValidationMessage> messages = new ArrayList<>();
        CellValidationMessage validationMessage = validationMessage(fieldValue, currentInstance, validatorContext);
        String sheetName = validatorContext.sheetName()!=null ? validatorContext.sheetName() : sheet.value();
        validationMessage.setSheet(sheetName);
        addValidationMessageMetadataUsingCellInfo(sheet, validationMessage, cellInfoMap(fieldValue, currentInstance, validatorContext));
        messages.add(validationMessage);
        return messages;
    }

    protected CellValidationInfo cellValidation() {
        return cellValidationInfo;
    }

    protected Map<String, CellInfo> cellInfoMap(Object fieldValue, Object currentInstance, CellValidatorContext validatorContext) {
        Map<Integer, Map<String, CellInfo>> rowIndexKeyedHeaderKeyCellInfoMap = validatorContext.readerContext().rowIndexKeyedHeaderKeyCellInfoMap();
        Map<Integer, Map<String, CellInfo>> colIndexKeyedHeaderKeyCellInfoMap = validatorContext.readerContext().colIndexKeyedHeaderKeyCellInfoMap();

        return validatorContext.sheet().vertical() ? colIndexKeyedHeaderKeyCellInfoMap.get(validatorContext.currentRow().getColumnIndex()) : rowIndexKeyedHeaderKeyCellInfoMap.get(validatorContext.currentRow().getRowIndex());
    }

    protected void addValidationMessageMetadataUsingCellInfo(Sheet sheet, CellValidationMessage validationMessage, Map<String, CellInfo> cellInfoMap) {
        CellInfo cellInfo = cellInfoMap.get(headerKey);
        validationMessage.setHeader(cellInfo.header());
        validationMessage.setOriginalHeader(cellInfo.originalHeader());
        validationMessage.setRow(cellInfo.row());
        validationMessage.setColumn(cellInfo.getColumn());
    }


    protected boolean allowNullOrAllowEmptyCheck(Object fieldValue, AllowedValuesInfo allowedValues) {
        if(allowedValues.isAllowNull() && ObjectUtil.isNull(fieldValue)) {
            return true;
        }
        else {
            return allowedValues.isAllowEmpty() && (!ObjectUtil.isNull(fieldValue) && ObjectUtil.isEmptyString(fieldValue));
        }
    }

    protected boolean allowNullOrAllowEmptyCheck(Object fieldValue, ConditionallyAllowedValuesInfo conditionallyAllowedValues) {
        if(conditionallyAllowedValues.isAllowNull() && ObjectUtil.isNull(fieldValue)) {
            return true;
        }
        else {
            return conditionallyAllowedValues.isAllowEmpty() && (!ObjectUtil.isNull(fieldValue) && ObjectUtil.isEmptyString(fieldValue));
        }
    }

    protected boolean allowNullOrAllowEmptyCheck(Object fieldValue, CellValidationInfo cellValidation) {
        if(cellValidation.isAllowNull() && ObjectUtil.isNull(fieldValue)) {
            return true;
        }
        else {
            return cellValidation.isAllowEmpty() && (!ObjectUtil.isNull(fieldValue) && ObjectUtil.isEmptyString(fieldValue));
        }
    }

    protected AllowedValuesInfo allowedValuesInfo() {
        return dynamicCellValidationContext.getCellValidationInfo().getAllowedValuesInfo();
    }
}
