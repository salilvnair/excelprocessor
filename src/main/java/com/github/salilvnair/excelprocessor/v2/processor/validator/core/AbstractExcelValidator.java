package com.github.salilvnair.excelprocessor.v2.processor.validator.core;

import com.github.salilvnair.excelprocessor.util.ReflectionUtil;
import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.context.DynamicCellValidationContext;
import com.github.salilvnair.excelprocessor.v2.model.CellValidationInfo;
import com.github.salilvnair.excelprocessor.v2.processor.validator.context.CellValidationMessage;
import com.github.salilvnair.excelprocessor.v2.processor.validator.context.CellValidatorContext;
import com.github.salilvnair.excelprocessor.v2.processor.validator.helper.ExcelSheetValidatorUtil;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class AbstractExcelValidator implements ExcelValidator {

    protected List<CellValidationMessage> validate(Object currentInstance, CellValidatorContext validatorContext, Set<Field> cells, Set<Field> sections) {
        List<CellValidationMessage> errors = new ArrayList<>();
        Sheet sheet = currentInstance.getClass().getAnnotation(Sheet.class);
        validatorContext.setSheet(sheet);
        for (Field cell: cells) {
            ExcelSheetValidatorUtil validatorUtil = new ExcelSheetValidatorUtil(cell, ExcelSheetValidatorType.CELL);
            validatorContext.setField(cell);
            validatorUtil.setValidatorContext(validatorContext);
            errors.addAll(validatorUtil.validate(currentInstance, validatorContext));
        }
        for (Field section: sections) {
            ExcelSheetValidatorUtil validatorUtil = new ExcelSheetValidatorUtil(section, ExcelSheetValidatorType.SECTION);
            validatorUtil.setValidatorContext(validatorContext);
            Object sectionInstance = ReflectionUtil.getFieldValue(currentInstance, section);
            errors.addAll(validatorUtil.validate(sectionInstance, validatorContext));
        }
        return errors;
    }

    protected List<CellValidationMessage> validate(Object currentInstance, CellValidatorContext validatorContext, Map<String, CellValidationInfo> headerKeyedCellValidationInfo) {
        List<CellValidationMessage> errors = new ArrayList<>();
        if(headerKeyedCellValidationInfo == null || headerKeyedCellValidationInfo.isEmpty()) {
            return errors;
        }
        Sheet sheet = currentInstance.getClass().getAnnotation(Sheet.class);
        validatorContext.setSheet(sheet);
        for (String header : headerKeyedCellValidationInfo.keySet()) {
            DynamicCellValidationContext dynamicCellValidationContext = DynamicCellValidationContext.builder().header(header).cellValidationInfo(headerKeyedCellValidationInfo.get(header)).build();
            ExcelSheetValidatorUtil validatorUtil = new ExcelSheetValidatorUtil(dynamicCellValidationContext, ExcelSheetValidatorType.DYNAMIC_CELL);
            validatorUtil.setValidatorContext(validatorContext);
            errors.addAll(validatorUtil.validate(currentInstance, validatorContext));
        }
        return errors;
    }

}
