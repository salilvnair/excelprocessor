package com.github.salilvnair.excelprocessor.v1.reflect.validator;

import com.github.salilvnair.excelprocessor.v1.reflect.annotation.ExcelHeaderValidator;
import com.github.salilvnair.excelprocessor.v1.reflect.annotation.ExcelSheet;
import com.github.salilvnair.excelprocessor.v1.reflect.constant.ExcelValidatorConstant;
import com.github.salilvnair.excelprocessor.v1.reflect.context.ValidatorContext;
import com.github.salilvnair.excelprocessor.v1.reflect.service.AbstractCustomValidatorTask;

public class ConditionalValidator extends BaseExcelValidator {
	@Override
	public String validate(ValidatorContext validatorContext) {
		String errorMessage = null;			
		String headerKey = validatorContext.getHeaderKey(); 
		if(isUnknownExcelHeader(validatorContext, headerKey)) {
			return errorMessage;
		}
		AbstractCustomValidatorTask validatorTask = (AbstractCustomValidatorTask) validatorContext.getValidatorTask();
		if(validatorTask==null) {
			return null;
		}
		String methodName = null;
		ExcelHeaderValidator excelHeaderValidator = null;
		ExcelSheet sheetValidator = null;
		excelHeaderValidator = validatorContext.getExcelHeaderValidator();
		methodName = excelHeaderValidator.condition();
		if(!ExcelValidatorConstant.EMPTY_STRING.equals(methodName)) {
			errorMessage = CustomMethodValidator.invokeCustomTask(validatorContext,methodName,sheetValidator,excelHeaderValidator,validatorTask);
		}
		return errorMessage;
	}

	
	
	
	
}
