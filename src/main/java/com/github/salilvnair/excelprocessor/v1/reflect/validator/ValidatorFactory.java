package com.github.salilvnair.excelprocessor.v1.reflect.validator;

import java.util.ArrayList;
import java.util.List;

import com.github.salilvnair.excelprocessor.v1.reflect.annotation.ExcelHeaderValidator;
import com.github.salilvnair.excelprocessor.v1.reflect.constant.ExcelValidatorConstant;

public class ValidatorFactory {

	
	public static List<IExcelValidator> getValidator(ExcelHeaderValidator excelHeaderValidator){
		List<IExcelValidator> excelValidatorList = new ArrayList<>();
		if(excelHeaderValidator.required()){
			excelValidatorList.add(new RequiredValidator());
		}
		else if(excelHeaderValidator.conditional()) {
			excelValidatorList.add(new ConditionalValidator());
		}
		if(excelHeaderValidator.minLength()!=-1 || excelHeaderValidator.maxLength()!=-1 || excelHeaderValidator.length()!=-1 ) {
			excelValidatorList.add(new LengthValidator());
		}
		if(!ExcelValidatorConstant.EMPTY_STRING.equals(excelHeaderValidator.pattern())){
			excelValidatorList.add(new PatternValidator());
		}
		if(excelHeaderValidator.email()){
			excelValidatorList.add(new EmailValidator());
		}
		if(excelHeaderValidator.numeric()){
			excelValidatorList.add(new NumericValidator());
		}
		else if(excelHeaderValidator.alphaNumeric()){
			excelValidatorList.add(new AlphaNumericValidator());
		}
		if(excelHeaderValidator.unique()){
			excelValidatorList.add(new UniqueValidator());
		}
		if(!ExcelValidatorConstant.EMPTY_STRING.equals(excelHeaderValidator.customTask()) || excelHeaderValidator.customTasks().length>0){
			excelValidatorList.add(new CustomMethodValidator());
		}
		if(excelHeaderValidator.dependentHeaders().length>0){
			excelValidatorList.add(new DependencyValidator());
		}
		return excelValidatorList;
	}
	
	public static IExcelValidator getValidator(String validatorType){
		if(ExcelValidatorConstant.EXCEL_CUSTOM_METHOD_VALIDATOR.equals(validatorType)){
			return new CustomMethodValidator();
		}
		
		else if(ExcelValidatorConstant.EXCEL_UNIQUE_VALIDATOR.equals(validatorType)){
			return new UniqueValidator();
		}
		
		else if(ExcelValidatorConstant.EXCEL_DEPENDENCY_VALIDATOR.equals(validatorType)){
			return new DependencyValidator();
		}
		
		else if(ExcelValidatorConstant.EXCEL_CONDITIONAL_VALIDATOR.equals(validatorType)){
			return new ConditionalValidator();
		}
		
		else if(ExcelValidatorConstant.EXCEL_REQUIRED_VALIDATOR.equals(validatorType)){
			return new RequiredValidator();
		}
		
		else if(ExcelValidatorConstant.EXCEL_LENGTH_VALIDATOR.equals(validatorType)){
			return new LengthValidator();
		}
		
		else if(ExcelValidatorConstant.EXCEL_EMAIL_VALIDATOR.equals(validatorType)){
			return new EmailValidator();
		}
		
		else if(ExcelValidatorConstant.EXCEL_PATTERN_VALIDATOR.equals(validatorType)){
			return new PatternValidator();
		}
		return null;
	}
	
	
}
