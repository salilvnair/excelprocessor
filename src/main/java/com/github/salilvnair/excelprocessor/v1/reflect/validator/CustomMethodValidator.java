package com.github.salilvnair.excelprocessor.v1.reflect.validator;

import com.github.salilvnair.excelprocessor.v1.reflect.annotation.ExcelHeaderValidator;
import com.github.salilvnair.excelprocessor.v1.reflect.annotation.ExcelSheet;
import com.github.salilvnair.excelprocessor.v1.reflect.constant.ExcelHeaderConstant;
import com.github.salilvnair.excelprocessor.v1.reflect.constant.ExcelValidatorConstant;
import com.github.salilvnair.excelprocessor.v1.reflect.context.ValidatorContext;
import com.github.salilvnair.excelprocessor.util.ExcelValidatorUtil;
import com.github.salilvnair.excelprocessor.v1.reflect.service.AbstractCustomValidatorTask;

public class CustomMethodValidator extends BaseExcelValidator {

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
		if(validatorContext.isSheetCustomValidation()){
			sheetValidator = validatorContext.getExcelSheet();
			if(sheetValidator!=null) {
				methodName = validatorContext.getCustomSheetTask();
			}
			errorMessage = invokeCustomTask(validatorContext,methodName,sheetValidator,excelHeaderValidator,validatorTask);
		}
		else{
			excelHeaderValidator = validatorContext.getExcelHeaderValidator();
			methodName = excelHeaderValidator.customTask();
			if(ExcelValidatorConstant.EMPTY_STRING.equals(methodName) &&
					excelHeaderValidator.customTasks().length>0	) {
				StringBuilder errorMessageBuilder = new StringBuilder();
				for(String methodNameItr:excelHeaderValidator.customTasks()) {
					errorMessage = invokeCustomTask(validatorContext,methodNameItr,sheetValidator,excelHeaderValidator,validatorTask);
					if(errorMessage!=null) {
						errorMessageBuilder.append(errorMessage);
						errorMessageBuilder.append(",");
					}
				}
				if(!ExcelValidatorConstant.EMPTY_STRING.equals(errorMessageBuilder.toString())) {
					errorMessage = errorMessageBuilder.toString().replaceAll(",$", "");
				}
			}
			else {
				if(validatorContext.getCustomTaskMethod()!=null){
					methodName = validatorContext.getCustomTaskMethod();
				}
				errorMessage = invokeCustomTask(validatorContext,methodName,sheetValidator,excelHeaderValidator,validatorTask);
			}
		}	
		return errorMessage;
	}

	public static String invokeCustomTask(ValidatorContext validatorContext, String methodName, ExcelSheet sheetValidator, ExcelHeaderValidator excelHeaderValidator, AbstractCustomValidatorTask validatorTask) {
		String errorMessage = null;
		int rowNum = validatorContext.getRowNum();
		String columnName = validatorContext.getColumnName();
		validatorTask.setMethodName(methodName);
		ExcelValidatorUtil excelValidatorUtil = new ExcelValidatorUtil();
		Object validatedTaskResponse = excelValidatorUtil.executeTask(validatorTask, validatorContext);
		boolean hasInvalidData = false;
			
		if(validatedTaskResponse instanceof String){
			errorMessage = (String) validatedTaskResponse;
		}
		else if(validatedTaskResponse instanceof Boolean){
			hasInvalidData =  (boolean) validatedTaskResponse;
		}
		if(hasInvalidData){
			if(validatorContext.isSheetCustomValidation()){
				if(sheetValidator!=null && !ExcelValidatorConstant.EMPTY_STRING.equals(sheetValidator.userDefinedMessage())){
					errorMessage = sheetValidator.userDefinedMessage();
				}
			}
			else{
				if(!ExcelValidatorConstant.EMPTY_STRING.equals(excelHeaderValidator.userDefinedMessage())){					
					errorMessage = excelHeaderValidator.userDefinedMessage();
				}
			}			
		}
		if(errorMessage!=null){
			if(sheetValidator!=null && sheetValidator.isVertical()){
				int rowAt = getRow(validatorContext);
				if(rowAt!=-1) {
					errorMessage = errorMessage.replace(ExcelHeaderConstant.ROW_NUM_PLACEHOLDER, ""+rowAt);
				}
				errorMessage = errorMessage.replace(ExcelHeaderConstant.COLUMN_NAME_PLACEHOLDER, ""+columnName);
			}
			else{
				String columnAt = getColumn(validatorContext);
				if(!ExcelValidatorConstant.EMPTY_STRING.equals(columnAt)) {
					errorMessage = errorMessage.replace(ExcelHeaderConstant.COLUMN_NAME_PLACEHOLDER, columnAt);
				}
				errorMessage = errorMessage.replace(ExcelHeaderConstant.ROW_NUM_PLACEHOLDER, ""+rowNum);	
			}
		}
		return errorMessage;
	}

}
