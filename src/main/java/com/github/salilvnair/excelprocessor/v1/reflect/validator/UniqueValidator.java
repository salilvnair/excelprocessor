package com.github.salilvnair.excelprocessor.v1.reflect.validator;

import java.util.ArrayList;
import java.util.List;

import com.github.salilvnair.excelprocessor.v1.reflect.annotation.ExcelHeaderValidator;
import com.github.salilvnair.excelprocessor.v1.reflect.annotation.ExcelSheet;
import com.github.salilvnair.excelprocessor.v1.reflect.constant.ExcelHeaderConstant;
import com.github.salilvnair.excelprocessor.v1.reflect.constant.ExcelValidatorConstant;
import com.github.salilvnair.excelprocessor.v1.reflect.context.ValidatorContext;

public class UniqueValidator extends BaseExcelValidator {
	@Override
	public String validate(ValidatorContext validatorContext) {
		String errorMessage = null;
		Object columnValue = validatorContext.getColumnValue();				
		String headerKey = validatorContext.getHeaderKey(); 
		if(isUnknownExcelHeader(validatorContext, headerKey)) {
			return errorMessage;
		}
		int rowNum = validatorContext.getRowNum();
		String columnName = validatorContext.getColumnName();
		ExcelHeaderValidator excelHeaderValidator = validatorContext.getExcelHeaderValidator();
		ExcelSheet excelSheet = validatorContext.getExcelSheet();
		boolean uniqueKeyViolated = false;
		if(validatorContext.getUniqueValueHolderList()!=null && !validatorContext.getUniqueValueHolderList().isEmpty()) {
			List<Object> uniqueValueHolderList = validatorContext.getUniqueValueHolderList();
			if(uniqueValueHolderList.contains(columnValue)) {
				uniqueKeyViolated = true;
			}
			else {
				uniqueValueHolderList.add(columnValue);
				validatorContext.setUniqueValueHolderList(uniqueValueHolderList);
			}
		}
		else {
			List<Object> uniqueValueHolderList = new ArrayList<>();
			uniqueValueHolderList.add(columnValue);
			validatorContext.setUniqueValueHolderList(uniqueValueHolderList);
		}
		if(uniqueKeyViolated) {
			errorMessage = prepareErrorString(validatorContext, headerKey, rowNum, columnName, excelHeaderValidator, excelSheet);
		}
		return errorMessage;
	}
	
	private String prepareErrorString(ValidatorContext validatorContext, String headerKey,int rowNum ,String columnName,ExcelHeaderValidator excelHeaderValidator,ExcelSheet excelSheet) {
		String columnAt = getColumn(validatorContext);
		String errorMessage = headerKey+" at row["+rowNum+"] should be unique!";
		if(!ExcelValidatorConstant.EMPTY_STRING.equals(columnAt)) {
			errorMessage = headerKey+" at row["+rowNum+"],column["+columnAt+"] should be unique!";
		}
		if(excelSheet!=null){
			if(excelSheet.isVertical()){
				if(excelSheet.isSingleValueVerticalSheet() || excelSheet.verticallyScatteredHeaders()){
					errorMessage = headerKey+" should be unique!";
				}
				else{						
					int rowAt = getRow(validatorContext);
					errorMessage = headerKey+" at column["+columnName+"] should be unique!";
					if(rowAt!=-1) {
						errorMessage = headerKey+" at row["+rowAt+"],column["+columnName+"] should be unique!";
					}
				}					
			}
		}
		String userDefinedMessage = excelHeaderValidator.userDefinedMessage();
		if(!ExcelValidatorConstant.EMPTY_STRING.equals(excelHeaderValidator.userDefinedMessage())){
			if(excelSheet!=null && excelSheet.isVertical()){
				errorMessage = userDefinedMessage.replace(ExcelHeaderConstant.COLUMN_NAME_PLACEHOLDER, ""+columnName);
			}
			else{
				errorMessage = userDefinedMessage.replace(ExcelHeaderConstant.ROW_NUM_PLACEHOLDER, ""+rowNum);	
			}				
		}
		if(excelHeaderValidator.userDefinedMessages().length>0) {
			for(String userDefinedMessageItr : excelHeaderValidator.userDefinedMessages()) {
				if(userDefinedMessageItr.contains(ExcelHeaderConstant.USER_DEFINED_MESSAGE_KEY_UNIQUE)) {
					userDefinedMessageItr = userDefinedMessageItr.split(ExcelHeaderConstant.USER_DEFINED_MESSAGE_KEY_SPLIT_HOLDER)[1];
					if(excelSheet!=null && excelSheet.isVertical()){
						errorMessage = userDefinedMessageItr.replace(ExcelHeaderConstant.COLUMN_NAME_PLACEHOLDER, ""+columnName);
					}
					else{
						errorMessage = userDefinedMessageItr.replace(ExcelHeaderConstant.ROW_NUM_PLACEHOLDER, ""+rowNum);	
					}
				}
			}
		}
		return errorMessage;
	}

}
