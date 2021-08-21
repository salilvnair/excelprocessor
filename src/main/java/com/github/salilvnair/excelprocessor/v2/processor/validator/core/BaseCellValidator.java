package com.github.salilvnair.excelprocessor.v2.processor.validator.core;

import com.github.salilvnair.excelprocessor.util.ReflectionUtil;
import com.github.salilvnair.excelprocessor.v2.annotation.*;
import com.github.salilvnair.excelprocessor.v2.helper.ObjectUtils;
import com.github.salilvnair.excelprocessor.v2.helper.StringUtils;
import com.github.salilvnair.excelprocessor.v2.processor.helper.ExcelSheetReaderUtil;
import com.github.salilvnair.excelprocessor.v2.service.ExcelSheetReader;
import com.github.salilvnair.excelprocessor.v2.processor.validator.context.CellValidationMessage;
import com.github.salilvnair.excelprocessor.v2.processor.validator.context.CellValidatorContext;
import com.github.salilvnair.excelprocessor.v2.processor.validator.type.ValidatorType;
import com.github.salilvnair.excelprocessor.v2.type.CellInfo;

import java.lang.reflect.Field;
import java.util.*;

public abstract class BaseCellValidator extends AbstractExcelValidator {
    private final Field field;
    public BaseCellValidator(Field field) {
        this.field = field;
    }
    protected abstract boolean violated(Object fieldValue, Object currentInstance, CellValidatorContext validatorContext);
    protected abstract ValidatorType validatorType();
    protected String defaultMessage(Object fieldValue, Object currentInstance, CellValidatorContext validatorContext) {
        return null;
    }
    protected String headerKey(Object fieldValue, Object currentInstance, CellValidatorContext validatorContext) {
        Sheet sheet = validatorContext.sheet();
        Cell cell = field.getAnnotation(Cell.class);
        return cell.value();
    }

    @Override
    public List<CellValidationMessage> validate(Object currentInstance, CellValidatorContext validatorContext) {
        List<CellValidationMessage> messages = new ArrayList<>();
        Object fieldValue = ReflectionUtil.getFieldValue(currentInstance, field.getName());
        if(violated(fieldValue, currentInstance, validatorContext)) {
            messages = validationMessages(fieldValue, currentInstance, validatorContext);
        }
        return Collections.unmodifiableList(messages);
    }

    protected CellValidationMessage validationMessage(Object fieldValue, Object currentInstance, CellValidatorContext validatorContext) {
        CellValidationMessage validationMessage = new CellValidationMessage();
        String errorMessage = defaultMessage(fieldValue, currentInstance, validatorContext);
        CellValidation cellValidation = field.getAnnotation(CellValidation.class);
        String message = cellValidation.userDefinedMessage();
        if(!StringUtils.isEmpty(message)) {
            errorMessage = message;
        }
        validationMessage.setMessage(errorMessage);
        validationMessage.setMessageId(cellValidation.messageId());
        validationMessage.setMessageType(cellValidation.messageType().name());
        UserDefinedMessage[] userDefinedMessages = cellValidation.userDefinedMessages();
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
    protected Map<String, CellInfo> cellInfoMap(Object fieldValue, Object currentInstance, CellValidatorContext validatorContext) {
        Map<Integer, Map<String, CellInfo>> rowIndexKeyedHeaderKeyCellInfoMap = validatorContext.readerContext().rowIndexKeyedHeaderKeyCellInfoMap();
        Map<Integer, Map<String, CellInfo>> colIndexKeyedHeaderKeyCellInfoMap = validatorContext.readerContext().colIndexKeyedHeaderKeyCellInfoMap();

        return validatorContext.sheet().vertical() ? colIndexKeyedHeaderKeyCellInfoMap.get(validatorContext.currentRow().getColumnIndex()) : rowIndexKeyedHeaderKeyCellInfoMap.get(validatorContext.currentRow().getRowIndex());
    }

    protected void addValidationMessageMetadataUsingCellInfo(Sheet sheet, CellValidationMessage validationMessage, Map<String, CellInfo> cellInfoMap) {
        Cell cell = field.getAnnotation(Cell.class);
        String headerKey = ExcelSheetReaderUtil.processSimilarHeaderString(sheet, cell);
        CellInfo cellInfo = cellInfoMap.get(headerKey);
        validationMessage.setHeader(cellInfo.header());
        validationMessage.setOriginalHeader(cellInfo.originalHeader());
        validationMessage.setRow(cellInfo.rowIndex() + 1);
        validationMessage.setColumn(ExcelSheetReader.toIndentName(cellInfo.columnIndex()+1));
        validationMessage.setMappedFieldName(field.getName());
    }

    protected CellValidation cellValidation() {
        return field.getAnnotation(CellValidation.class);
    }

    protected boolean allowNullOrAllowEmptyCheck(Object fieldValue, AllowedValues allowedValues) {
        if(allowedValues.allowNull() && ObjectUtils.isNull(fieldValue)) {
            return true;
        }
        else {
            return allowedValues.allowEmpty() && (!ObjectUtils.isNull(fieldValue) && ObjectUtils.isEmptyString(fieldValue));
        }
    }

    protected boolean allowNullOrAllowEmptyCheck(Object fieldValue, CellValidation cellValidation) {
        if(cellValidation.allowNull() && ObjectUtils.isNull(fieldValue)) {
            return true;
        }
        else {
            return cellValidation.allowEmpty() && (!ObjectUtils.isNull(fieldValue) && ObjectUtils.isEmptyString(fieldValue));
        }
    }
}
