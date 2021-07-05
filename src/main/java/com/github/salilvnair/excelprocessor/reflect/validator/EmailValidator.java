package com.github.salilvnair.excelprocessor.reflect.validator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.salilvnair.excelprocessor.reflect.annotation.ExcelHeaderValidator;
import com.github.salilvnair.excelprocessor.reflect.annotation.ExcelSheet;
import com.github.salilvnair.excelprocessor.reflect.constant.ExcelHeaderConstant;
import com.github.salilvnair.excelprocessor.reflect.constant.ExcelValidatorConstant;
import com.github.salilvnair.excelprocessor.reflect.context.ValidatorContext;

public class EmailValidator extends BaseExcelValidator {

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
		boolean isFieldTypeEmail = excelHeaderValidator.email();
		boolean invalidEmail = false;
		if(isFieldTypeEmail) {
			if((excelHeaderValidator.nonNull() && columnValue==null)  || ((excelHeaderValidator.nonEmpty()||excelHeaderValidator.nonNull()) && ExcelValidatorConstant.EMPTY_STRING.equals(columnValue))){
				invalidEmail = true;
			}
			else if(columnValue instanceof String) {
				String columnStringValue = (String) columnValue;
				invalidEmail = !EmailValidator.isValid(columnStringValue);
			}	
			errorMessage = prepareErrorString(columnValue,
					validatorContext,
					headerKey,
					rowNum,
					columnName,
					invalidEmail,
					excelHeaderValidator,
					excelSheet);
		}
		return errorMessage;
	}
	public static boolean isValid(String emailString) {
		String ATOM = "[a-z0-9!#$%&'*+/=?^_`{|}~-]";
		String DOMAIN = "(" + ATOM + "+(\\." + ATOM + "+)*";
		String IP_DOMAIN = "\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\]"; 
		Pattern pattern = Pattern.compile("^" + ATOM + "+(\\." + ATOM + "+)*@" + DOMAIN + "|" + IP_DOMAIN + ")$", 2);
		if ((emailString == null) || (emailString.length() == 0)) {
			return false;
		}
		Matcher m = pattern.matcher(emailString);
		return m.matches();
	}
	private String prepareErrorString(Object columnValue,
										ValidatorContext validatorContext,
										String headerKey,
										int rowNum ,
										String columnName,
										boolean invalidEmail,
										ExcelHeaderValidator excelHeaderValidator,
										ExcelSheet excelSheet) {
		String errorMessage = null;
		if(invalidEmail) {
			String columnAt = getColumn(validatorContext);
			if(excelSheet!=null && (excelSheet.isSingleValueVerticalSheet() || excelSheet.verticallyScatteredHeaders())){
				errorMessage = headerKey+" is not a valid email format.";
			}
			else {
				errorMessage = headerKey+" at row["+rowNum+"] is not a valid email format.";
				if(!ExcelValidatorConstant.EMPTY_STRING.equals(columnAt)) {
					errorMessage = headerKey+"'s length at row["+rowNum+"],column["+columnAt+"] is not a valid email format.";
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
					if(userDefinedMessageItr.contains(ExcelHeaderConstant.USER_DEFINED_MESSAGE_KEY_EMAIL)) {
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
