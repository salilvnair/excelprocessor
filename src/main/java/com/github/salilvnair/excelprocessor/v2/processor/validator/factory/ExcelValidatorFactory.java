package com.github.salilvnair.excelprocessor.v2.processor.validator.factory;

import com.github.salilvnair.excelprocessor.v2.context.DynamicCellValidationContext;
import com.github.salilvnair.excelprocessor.v2.processor.validator.core.ExcelSheetValidatorType;
import com.github.salilvnair.excelprocessor.v2.processor.validator.core.ExcelValidator;
import com.github.salilvnair.excelprocessor.v2.processor.validator.factory.common.ExcelCommonValidatorFactory;
import com.github.salilvnair.excelprocessor.v2.processor.validator.factory.dynamic.ExcelDynamicCellValidatorFactory;
import com.github.salilvnair.excelprocessor.v2.processor.validator.factory.fixed.ExcelCellValidatorFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Salil V Nair
 */
public class ExcelValidatorFactory {
    private ExcelValidatorFactory(){}

    public static List<ExcelValidator> generate(Object classInstance, ExcelSheetValidatorType excelSheetValidatorType) {
        List<ExcelValidator> validators = new ArrayList<>();
        if(ExcelSheetValidatorType.ROW.equals(excelSheetValidatorType)) {
            validators = ExcelCommonValidatorFactory.generateRowValidators(classInstance);
        }
        else if(ExcelSheetValidatorType.SHEET.equals(excelSheetValidatorType)) {
            validators = ExcelCommonValidatorFactory.generateSheetValidators(classInstance);
        }
        else if(ExcelSheetValidatorType.CELL.equals(excelSheetValidatorType)) {
            validators = ExcelCellValidatorFactory.generate((Field) classInstance);
        }
        else if(ExcelSheetValidatorType.DYNAMIC_CELL.equals(excelSheetValidatorType)) {
            validators = ExcelDynamicCellValidatorFactory.generate((DynamicCellValidationContext) classInstance);
        }
        else if(ExcelSheetValidatorType.SECTION.equals(excelSheetValidatorType)) {
            validators = ExcelCommonValidatorFactory.generateSectionValidators((Field) classInstance);
        }
        return validators;
    }
}
