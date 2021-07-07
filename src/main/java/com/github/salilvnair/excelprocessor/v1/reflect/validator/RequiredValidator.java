package com.github.salilvnair.excelprocessor.v1.reflect.validator;

import com.github.salilvnair.excelprocessor.v1.reflect.annotation.ExcelHeaderValidator;
import com.github.salilvnair.excelprocessor.v1.reflect.annotation.ExcelSheet;
import com.github.salilvnair.excelprocessor.v1.reflect.constant.ExcelHeaderConstant;
import com.github.salilvnair.excelprocessor.v1.reflect.constant.ExcelValidatorConstant;
import com.github.salilvnair.excelprocessor.v1.reflect.context.ValidatorContext;

public class RequiredValidator extends BaseExcelValidator {
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
		if(columnValue==null || ExcelValidatorConstant.EMPTY_STRING.equals(columnValue)){
			String columnAt = getColumn(validatorContext);
			errorMessage = headerKey+" at row["+rowNum+"] cannot be empty!";
			if(!ExcelValidatorConstant.EMPTY_STRING.equals(columnAt)) {
				errorMessage = headerKey+" at row["+rowNum+"],column["+columnAt+"] cannot be empty!";
			}
			if(excelSheet!=null){
				if(excelSheet.isVertical()){
					if(excelSheet.isSingleValueVerticalSheet() || excelSheet.verticallyScatteredHeaders()){
						errorMessage = headerKey+" cannot be empty!";
					}
					else{						
						int rowAt = getRow(validatorContext);
						errorMessage = headerKey+" at column["+columnName+"] cannot be empty!";
						if(rowAt!=-1) {
							errorMessage = headerKey+" at row["+rowAt+"],column["+columnName+"] cannot be empty!";
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
			else if(excelHeaderValidator.userDefinedMessages().length>0) {
				for(String userDefinedMessageItr : excelHeaderValidator.userDefinedMessages()) {
					if(userDefinedMessageItr.contains(ExcelHeaderConstant.USER_DEFINED_MESSAGE_KEY_REQUIRED)) {
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
		}
		return errorMessage;
	}

	
	
	
	
}
