package com.github.salilvnair.excelprocessor.v1.reflect.validator;

import com.github.salilvnair.excelprocessor.v1.reflect.context.ExcelValidationMessage;
import com.github.salilvnair.excelprocessor.v1.reflect.context.ValidatorContext;

public interface IExcelValidator {
	
	String validate(ValidatorContext validatorContext);

	ExcelValidationMessage validateInDetail(ValidatorContext validatorContext);
	
}
