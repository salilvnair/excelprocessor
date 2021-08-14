package com.github.salilvnair.excelprocessor.v2.processor.validator.provider;

import java.lang.reflect.Field;
import java.util.List;

import com.github.salilvnair.excelprocessor.util.ReflectionUtil;
import com.github.salilvnair.excelprocessor.v2.processor.validator.context.CellValidationMessage;
import com.github.salilvnair.excelprocessor.v2.processor.validator.context.CellValidatorContext;
import com.github.salilvnair.excelprocessor.v2.processor.validator.core.AbstractExcelValidator;
import com.github.salilvnair.excelprocessor.v2.processor.validator.core.BaseCellValidator;
import com.github.salilvnair.excelprocessor.v2.processor.validator.type.ValidatorType;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;
import org.apache.commons.collections.CollectionUtils;


public class UniqueValidator extends BaseCellValidator {
    private final Field field;
    public UniqueValidator(Field field) {
        super(field);
        this.field = field;
    }

    @Override
    protected boolean violated(Object fieldValue, Object currentInstance, CellValidatorContext validatorContext) {
        List<? extends BaseSheet> sheetDataList = validatorContext.getCurrentSheet();
        if(CollectionUtils.isNotEmpty(sheetDataList)) {
            return sheetDataList.stream().anyMatch(sheetData -> fieldValue.equals(ReflectionUtil.getFieldValue(sheetData, field)));
        }
        return false;
    }

    @Override
    protected ValidatorType validatorType() {
        return ValidatorType.UNIQUE;
    }

    @Override
    protected String defaultMessage(Object fieldValue, Object currentInstance, CellValidatorContext validatorContext) {
        String headerKey = headerKey(fieldValue, currentInstance, validatorContext);
        return headerKey+" should be unique!";
    }
}
