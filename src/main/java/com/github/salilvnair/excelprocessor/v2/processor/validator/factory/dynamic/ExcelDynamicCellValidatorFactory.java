package com.github.salilvnair.excelprocessor.v2.processor.validator.factory.dynamic;

import com.github.salilvnair.excelprocessor.v2.context.DynamicCellValidationContext;
import com.github.salilvnair.excelprocessor.v2.helper.StringUtils;
import com.github.salilvnair.excelprocessor.v2.model.CellValidationInfo;
import com.github.salilvnair.excelprocessor.v2.processor.validator.core.ExcelValidator;
import com.github.salilvnair.excelprocessor.v2.processor.validator.provider.dynamic.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Salil V Nair
 */
public class ExcelDynamicCellValidatorFactory {
    private ExcelDynamicCellValidatorFactory(){}

    public static List<ExcelValidator> generate(DynamicCellValidationContext dynamicCellValidationContext) {
        List<ExcelValidator> validators = new ArrayList<>();
        if(dynamicCellValidationContext!=null && dynamicCellValidationContext.getCellValidationInfo()!=null) {
            validators.addAll(generateCellValidators(dynamicCellValidationContext, dynamicCellValidationContext.getCellValidationInfo()));
            if(dynamicCellValidationContext.getCellValidationInfo().getAllowedValuesInfo()!=null) {
                validators.addAll(generateAllowedValueValidators(dynamicCellValidationContext, dynamicCellValidationContext.getCellValidationInfo()));
            }
        }
        return validators;
    }

    private static Collection<? extends ExcelValidator> generateAllowedValueValidators(DynamicCellValidationContext dynamicCellValidationContext, CellValidationInfo cellValidation) {
        List<ExcelValidator> validators = new ArrayList<>();
        if(cellValidation.isRequired()) {
            validators.add(new AllowedValueValidator(dynamicCellValidationContext));
        }
        return validators;
    }

    private static List<ExcelValidator> generateCellValidators(DynamicCellValidationContext dynamicCellValidationContext, CellValidationInfo cellValidation) {
        List<ExcelValidator> validators = new ArrayList<>();
        if(cellValidation.isEmail()) {
            validators.add(new EmailValidator(dynamicCellValidationContext));
        }
        if(cellValidation.isRequired()) {
            validators.add(new RequiredValidator(dynamicCellValidationContext));
        }
        else if(cellValidation.isConditional()) {
            validators.add(new ConditionalValidator(dynamicCellValidationContext));
        }
        if(cellValidation.isDate()) {
            validators.add(new DateValidator(dynamicCellValidationContext));
        }
        if(cellValidation.isUnique()) {
            validators.add(new UniqueValidator(dynamicCellValidationContext));
        }
        if(cellValidation.isAlphaNumeric()) {
            validators.add(new AlphaNumericValidator(dynamicCellValidationContext));
        }
        if(cellValidation.isNumeric()) {
            validators.add(new NumericValidator(dynamicCellValidationContext));
        }

        if(cellValidation.getLength() > -1 || cellValidation.getMinLength() > -1 || cellValidation.getMaxLength() > -1) {
            validators.add(new LengthValidator(dynamicCellValidationContext));
        }
        if(!StringUtils.isEmpty(cellValidation.getCustomTask()) || cellValidation.getCustomTasks().length > 0) {
            validators.add(new CustomMethodValidator(dynamicCellValidationContext));
        }
        if(!StringUtils.isEmpty(cellValidation.getPattern()) && (cellValidation.isMatchPattern() || cellValidation.isFindPattern())) {
            validators.add(new PatternValidator(dynamicCellValidationContext));
        }
        return validators;
    }
}
