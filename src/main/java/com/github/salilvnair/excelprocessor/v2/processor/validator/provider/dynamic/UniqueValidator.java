package com.github.salilvnair.excelprocessor.v2.processor.validator.provider.dynamic;

import com.github.salilvnair.excelprocessor.v2.context.DynamicCellValidationContext;
import com.github.salilvnair.excelprocessor.v2.model.CellValidationInfo;
import com.github.salilvnair.excelprocessor.v2.processor.validator.context.CellValidatorContext;
import com.github.salilvnair.excelprocessor.v2.processor.validator.core.BaseDynamicCellValidator;
import com.github.salilvnair.excelprocessor.v2.processor.validator.type.ValidatorType;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;
import com.github.salilvnair.excelprocessor.v2.sheet.DynamicHeaderSheet;
import org.apache.commons.collections4.CollectionUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


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
        if(fieldValue == null) {
            return false;
        }
        Map<String, Set<Object>> seen = validatorContext.uniqueValues();
        if(seen != null) {
            // streaming: the second and later occurrences are duplicates
            return !seen.computeIfAbsent("dynamic#" + headerKey(), k -> new HashSet<>()).add(fieldValue);
        }
        List<? extends BaseSheet> sheetDataList = validatorContext.getCurrentSheet();
        if(CollectionUtils.isNotEmpty(sheetDataList)) {
            // a row never duplicates itself
            return sheetDataList.stream().anyMatch(sheetData -> {
                if(sheetData == currentInstance) {
                    return false;
                }
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
