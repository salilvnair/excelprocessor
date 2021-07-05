package com.github.salilvnair.excelprocessor.reflect.validator;

import java.util.Date;

import com.github.salilvnair.excelprocessor.reflect.annotation.ExcelHeader;
import com.github.salilvnair.excelprocessor.reflect.annotation.ExcelHeaderValidator;
import com.github.salilvnair.excelprocessor.reflect.annotation.ExcelSheet;
import com.github.salilvnair.excelprocessor.reflect.constant.ExcelHeaderConstant;
import com.github.salilvnair.excelprocessor.reflect.constant.ExcelValidatorConstant;
import com.github.salilvnair.excelprocessor.reflect.context.ValidatorContext;
import com.github.salilvnair.excelprocessor.reflect.helper.DateParsingUtil;

public class DateValidator extends BaseExcelValidator {

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
		ExcelHeader excelHeader = validatorContext.getExcelHeader();
		ExcelSheet excelSheet = validatorContext.getExcelSheet();
		boolean isFieldTypeDate = false;
		if(!ExcelValidatorConstant.EMPTY_STRING.equals(excelHeaderValidator.date())
			||!ExcelValidatorConstant.EMPTY_STRING.equals(excelHeaderValidator.minDate())
			||!ExcelValidatorConstant.EMPTY_STRING.equals(excelHeaderValidator.maxDate())
				) {
			isFieldTypeDate = true;
		}
		boolean invalidDate = false;
		if(isFieldTypeDate) {
			if((excelHeaderValidator.nonNull() && columnValue==null)  || ((excelHeaderValidator.nonEmpty()||excelHeaderValidator.nonNull()) && ExcelValidatorConstant.EMPTY_STRING.equals(columnValue))){
				invalidDate = true;
			}
			else if(columnValue instanceof Date) {
				Date dateColumnValue = (Date) columnValue;
				if(!ExcelValidatorConstant.EMPTY_STRING.equals(excelHeaderValidator.date())) {
					invalidDate = !DateValidator.isValidSameDate(dateColumnValue, excelHeaderValidator.date(), excelHeader.fromExcelDateFormats());
				}
				if(!ExcelValidatorConstant.EMPTY_STRING.equals(excelHeaderValidator.minDate())) {
					invalidDate = !DateValidator.isValidMinDate(dateColumnValue, excelHeaderValidator.date(), excelHeader.fromExcelDateFormats());
				}
				if(!ExcelValidatorConstant.EMPTY_STRING.equals(excelHeaderValidator.maxDate())) {
					invalidDate = !DateValidator.isValidMaxDate(dateColumnValue, excelHeaderValidator.date(), excelHeader.fromExcelDateFormats());
				}
			}	
			errorMessage = prepareErrorString(columnValue,
					validatorContext,
					headerKey,
					rowNum,
					columnName,
					invalidDate,
					excelHeaderValidator,
					excelSheet);
		}
		return errorMessage;
	}
	public static boolean isValidMaxDate(Date inputDate, String maxDateString, String[] dateFormats) {
		Date maxDate = DateParsingUtil.parseDate(maxDateString, dateFormats);
		if(DateParsingUtil.compareDate(maxDate, inputDate)==0 || DateParsingUtil.compareDate(maxDate, inputDate)==1) {
			return true;
		}
		return false;
	}
	public static boolean isValidMinDate(Date inputDate, String minDateString, String[] dateFormats) {
		Date maxDate = DateParsingUtil.parseDate(minDateString, dateFormats);
		if(DateParsingUtil.compareDate(maxDate, inputDate)==0 || DateParsingUtil.compareDate(maxDate, inputDate)==-1) {
			return true;
		}
		return false;
	}
	public static boolean isValidSameDate(Date inputDate, String dateString, String[] dateFormats) {
		Date maxDate = DateParsingUtil.parseDate(dateString, dateFormats);
		if(DateParsingUtil.compareDate(maxDate, inputDate)==0) {
			return true;
		}
		return false;
	}
	private String prepareErrorString(Object columnValue,
										ValidatorContext validatorContext,
										String headerKey,
										int rowNum ,
										String columnName,
										boolean invalidDate,
										ExcelHeaderValidator excelHeaderValidator,
										ExcelSheet excelSheet) {
		String errorMessage = null;
		if(invalidDate) {
			String columnAt = getColumn(validatorContext);
			errorMessage = headerKey+" at row["+rowNum+"] date value doesn't meet the validations.";
			if(!ExcelValidatorConstant.EMPTY_STRING.equals(columnAt)) {
				errorMessage = headerKey+"'s length at row["+rowNum+"],column["+columnAt+"] date value doesn't meet the validations.";
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
