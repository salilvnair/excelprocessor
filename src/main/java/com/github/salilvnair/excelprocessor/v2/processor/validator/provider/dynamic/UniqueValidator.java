package com.github.salilvnair.excelprocessor.v2.processor.validator.provider.dynamic;

import com.github.salilvnair.excelprocessor.v2.context.DynamicCellValidationContext;
import com.github.salilvnair.excelprocessor.v2.model.CellValidationInfo;
import com.github.salilvnair.excelprocessor.v2.processor.validator.context.CellValidatorContext;
import com.github.salilvnair.excelprocessor.v2.processor.validator.core.BaseDynamicCellValidator;
import com.github.salilvnair.excelprocessor.v2.processor.validator.type.ValidatorType;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;
import com.github.salilvnair.excelprocessor.v2.sheet.DynamicHeaderSheet;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;


public class UniqueValidator extends BaseDynamicCellValidator {

    public UniqueValidator(DynamicCellValidationContext dynamicCellValidationContext) {
        super(dynamicCellValidationContext);
    }

    @Override
    protected boolean violated(Object fieldValue, Object currentInstance, CellValidatorContext validatorContext) {
        CellValidationInfo cellValidation = cellValidation();
        if(allowNullOrAllowEmptyCheck(fieldValue, cellValidation)) {
            return false;
        }
        List<? extends BaseSheet> sheetDataList = validatorContext.getCurrentSheet();
        if(CollectionUtils.isNotEmpty(sheetDataList)) {
            return sheetDataList.stream().anyMatch(sheetData -> {
                DynamicHeaderSheet dynamicHeaderSheet = (DynamicHeaderSheet) sheetData;
                return fieldValue.equals(dynamicHeaderSheet.dynamicHeaderKeyedCellValueMap().get(headerKey()));
            });
        }
        return false;
    }

    @Override
    protected ValidatorType validatorType() {
        return ValidatorType.UNIQUE;
    }

    @Override
    protected String defaultMessage(Object fieldValue, Object currentInstance, CellValidatorContext validatorContext) {
        String header = header();
        return header+" should be unique!";
    }
}
