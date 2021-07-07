package com.github.salilvnair.excelprocessor.v2.processor.validator.core;

import com.github.salilvnair.excelprocessor.util.ReflectionUtil;
import com.github.salilvnair.excelprocessor.v2.annotation.ExcelHeader;
import com.github.salilvnair.excelprocessor.v2.annotation.ExcelHeaderValidator;
import com.github.salilvnair.excelprocessor.v2.annotation.ExcelSheet;
import com.github.salilvnair.excelprocessor.v2.annotation.UserDefinedMessage;
import com.github.salilvnair.excelprocessor.v2.helper.StringUtils;
import com.github.salilvnair.excelprocessor.v2.processor.core.ExcelSheetReader;
import com.github.salilvnair.excelprocessor.v2.processor.validator.context.ValidationMessage;
import com.github.salilvnair.excelprocessor.v2.processor.validator.context.ValidatorContext;
import com.github.salilvnair.excelprocessor.v2.processor.validator.type.ValidatorType;
import com.github.salilvnair.excelprocessor.v2.type.CellInfo;

import java.lang.reflect.Field;
import java.util.*;

public abstract class BaseCellValidator extends BaseExcelValidator {
    private Field field;
    public BaseCellValidator(Field field) {
        this.field = field;
    }
    protected abstract boolean violated(Object fieldValue, Object currentInstance, ValidatorContext validatorContext);
    protected abstract ValidatorType validatorType();
    protected String defaultMessage() {
        return null;
    }

    @Override
    public List<ValidationMessage> validate(Object currentInstance, ValidatorContext validatorContext) {
        List<ValidationMessage> messages = new ArrayList<>();
        Object fieldValue = ReflectionUtil.getFieldValue(currentInstance, field.getName());
        if(violated(fieldValue, currentInstance, validatorContext)) {
            messages = validationMessages(fieldValue, currentInstance, validatorContext);
        }
        return Collections.unmodifiableList(messages);
    }

    protected ValidationMessage validationMessage(Object fieldValue, Object currentInstance, ValidatorContext validatorContext) {
        ValidationMessage validationMessage = new ValidationMessage();
        String errorMessage = defaultMessage();
        ExcelHeaderValidator headerValidator = field.getAnnotation(ExcelHeaderValidator.class);
        String message = headerValidator.userDefinedMessage();
        if(!StringUtils.isEmpty(message)) {
            errorMessage = message;
        }
        validationMessage.setMessage(errorMessage);
        validationMessage.setMessageId(headerValidator.messageId());
        validationMessage.setMessageType(headerValidator.messageType().name());
        UserDefinedMessage[] userDefinedMessages = headerValidator.userDefinedMessages();
        if(userDefinedMessages.length > 0) {
            Optional<UserDefinedMessage> requiredDefinedMessage = Arrays
                    .stream(userDefinedMessages)
                    .filter(userDefinedMessage -> validatorType().equals(userDefinedMessage.validatorType()))
                    .findFirst();
            if(requiredDefinedMessage.isPresent()) {
                UserDefinedMessage userDefinedMessage = requiredDefinedMessage.get();
                errorMessage = userDefinedMessage.message();
                validationMessage.setMessage(errorMessage);
                validationMessage.setMessageId(userDefinedMessage.messageId());
                validationMessage.setMessageType(userDefinedMessage.messageType().name());
            }
        }
        validationMessage.setValidatorType(validatorType());
        return validationMessage;
    }
    protected List<ValidationMessage> validationMessages(Object fieldValue, Object currentInstance, ValidatorContext validatorContext) {
        ExcelSheet excelSheet = currentInstance.getClass().getAnnotation(ExcelSheet.class);
        List<ValidationMessage> messages = new ArrayList<>();
        ValidationMessage validationMessage = validationMessage(fieldValue, currentInstance, validatorContext);
        validationMessage.setSheet(excelSheet.value());
        addValidationMessageMetadataUsingCellInfo(validationMessage, cellInfoMap(fieldValue, currentInstance, validatorContext));
        messages.add(validationMessage);
        return messages;
    }
    protected Map<String, CellInfo> cellInfoMap(Object fieldValue, Object currentInstance, ValidatorContext validatorContext) {
        Map<Integer, Map<String, CellInfo>> rowIndexKeyedHeaderKeyCellInfoMap = validatorContext.readerContext().getRowIndexKeyedHeaderKeyCellInfoMap();
        return rowIndexKeyedHeaderKeyCellInfoMap.get(validatorContext.currentRow().getRowIndex());
    }

    protected void addValidationMessageMetadataUsingCellInfo(ValidationMessage validationMessage, Map<String, CellInfo> cellInfoMap) {
        ExcelHeader excelHeader = field.getAnnotation(ExcelHeader.class);
        String headerKey = excelHeader.value();
        CellInfo cellInfo = cellInfoMap.get(headerKey);
        validationMessage.setHeader(headerKey);
        validationMessage.setRow(cellInfo.getRowIndex() + 1);
        validationMessage.setColumn(ExcelSheetReader.toIndentName(cellInfo.getColumnIndex()+1));
        validationMessage.setMappedFieldName(field.getName());
    }
}
