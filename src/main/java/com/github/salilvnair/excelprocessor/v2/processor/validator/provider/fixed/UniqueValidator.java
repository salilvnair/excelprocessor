package com.github.salilvnair.excelprocessor.v2.processor.validator.provider.fixed;

import com.github.salilvnair.excelprocessor.util.ReflectionUtil;
import com.github.salilvnair.excelprocessor.v2.processor.validator.context.CellValidatorContext;
import com.github.salilvnair.excelprocessor.v2.processor.validator.core.BaseCellValidator;
import com.github.salilvnair.excelprocessor.v2.processor.validator.type.ValidatorType;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;
import org.apache.commons.collections4.CollectionUtils;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class UniqueValidator extends BaseCellValidator {
    private final Field field;
    public UniqueValidator(Field field) {
        super(field);
        this.field = field;
    }

    @Override
    protected boolean violated(Object fieldValue, Object currentInstance, CellValidatorContext validatorContext) {
        if(fieldValue == null || (fieldValue instanceof String && ((String) fieldValue).isEmpty())) {
            return false;
        }
        Map<String, Set<Object>> seen = validatorContext.uniqueValues();
        if(seen != null) {
            // streaming: the second and later occurrences are duplicates
            return !seen.computeIfAbsent(field.getDeclaringClass().getName() + "#" + field.getName(), k -> new HashSet<>()).add(fieldValue);
        }
        List<? extends BaseSheet> sheetDataList = validatorContext.getCurrentSheet();
        if(CollectionUtils.isNotEmpty(sheetDataList)) {
            // every row holding a value that another row also holds is reported (a row never duplicates itself)
            return sheetDataList.stream().anyMatch(sheetData -> sheetData != currentInstance && fieldValue.equals(ReflectionUtil.getFieldValue(sheetData, field)));
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
