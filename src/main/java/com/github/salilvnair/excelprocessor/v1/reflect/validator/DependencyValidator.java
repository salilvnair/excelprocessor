package com.github.salilvnair.excelprocessor.v1.reflect.validator;

import java.util.List;
import java.util.Map;

import com.github.salilvnair.excelprocessor.v1.bean.BaseExcelValidationSheet;
import com.github.salilvnair.excelprocessor.v1.reflect.annotation.ExcelHeaderValidator;
import com.github.salilvnair.excelprocessor.v1.reflect.annotation.ExcelSheet;
import com.github.salilvnair.excelprocessor.v1.reflect.constant.ExcelHeaderConstant;
import com.github.salilvnair.excelprocessor.v1.reflect.constant.ExcelValidatorConstant;
import com.github.salilvnair.excelprocessor.v1.reflect.context.ExcelValidatorContext;
import com.github.salilvnair.excelprocessor.v1.reflect.context.ValidatorContext;
import com.github.salilvnair.excelprocessor.util.ReflectionUtil;

public class DependencyValidator extends BaseExcelValidator {


	@Override
	public String validate(ValidatorContext validatorContext) {
		String errorMessage = null;
		Object columnValue = validatorContext.getColumnValue();
		String jsonKey = validatorContext.getJsonKey();		
		String headerKey = validatorContext.getHeaderKey(); 
		if(isUnknownExcelHeader(validatorContext, headerKey)) {
			return errorMessage;
		}
		BaseExcelValidationSheet jsonExcelBean = validatorContext.getBaseExcelValidationSheet();
		ExcelHeaderValidator excelHeaderValidator = validatorContext.getExcelHeaderValidator();
		Map<String,String> customheaderMap = null;
		if(validatorContext.getFieldNameHeaderNameMap()!=null){
			customheaderMap = validatorContext.getFieldNameHeaderNameMap();
 		}
		String[] dependencyHeaders = null;
		if(excelHeaderValidator!=null) {
			dependencyHeaders = excelHeaderValidator.dependentHeaders();
		}
		if(dependencyHeaders==null || dependencyHeaders.length==0){
			ExcelValidatorContext excelValidatorContext = validatorContext.getExcelValidatorContext();
			if(excelValidatorContext!=null){
				if(excelValidatorContext.getPredefinedDatasetMap()!=null){
					if(excelHeaderValidator!=null && excelValidatorContext.getPredefinedDatasetMap().containsKey(excelHeaderValidator.dependentHeaderKey())){
						List<String> dependentHeaderList = excelValidatorContext.getPredefinedDatasetMap().get(excelHeaderValidator.dependentHeaderKey());
						dependencyHeaders = (String[]) dependentHeaderList.toArray();
					}
				}
			}
		}
		if(dependencyHeaders!=null) {
			String[] dependencyHeadersWithCustomValidation = new String[dependencyHeaders.length];
			StringBuilder dependencyErrorBuilder = new StringBuilder();
			errorMessage = processDependencyHeadersWithoutCustomValidation(excelHeaderValidator, dependencyHeaders, dependencyHeadersWithCustomValidation,validatorContext, customheaderMap, jsonExcelBean, columnValue, dependencyErrorBuilder, jsonKey, errorMessage);
			errorMessage = processDependencyHeadersWithCustomValidation(excelHeaderValidator, dependencyHeaders, dependencyHeadersWithCustomValidation, validatorContext, customheaderMap, jsonExcelBean, columnValue, dependencyErrorBuilder, jsonKey, errorMessage);
			
		}
		return errorMessage;
	}
	
	
	public String processDependencyHeadersWithoutCustomValidation(
			ExcelHeaderValidator excelHeaderValidator,
			String[] dependencyHeaders,
			String[] dependencyHeadersWithCustomValidation,
			ValidatorContext validatorContext,
			Map<String,String> customheaderMap,
			BaseExcelValidationSheet jsonExcelBean,
			Object columnValue,
			StringBuilder dependencyErrorBuilder,
			String jsonKey,
			String errorMessage
			)
	{
		if(dependencyHeaders.length>0){
			ExcelSheet excelSheet = validatorContext.getExcelSheet();
			int counter = 0;
			for(String dependentHeaderItr:dependencyHeaders){
				if(dependentHeaderItr.contains(ExcelValidatorConstant.EXCEL_CUSTOM_METHOD_VALIDATOR_PLACEHOLDER)){
					dependencyHeadersWithCustomValidation[counter]= dependentHeaderItr;
					counter++;
					continue;
				}

				Object dependentHeaderValue = ReflectionUtil.getFieldValue(jsonExcelBean, dependentHeaderItr);
				String dependentHeader = dependentHeaderItr;
				if(customheaderMap!=null && customheaderMap.containsKey(dependentHeaderItr)){
					dependentHeader = customheaderMap.get(dependentHeaderItr);
				}
				if(columnValue!=null && dependentHeaderValue==null){
					dependencyErrorBuilder.append(dependentHeader);
					dependencyErrorBuilder.append(excelSheet.messageDelimitter());
				}
			}
			if(customheaderMap!=null && customheaderMap.containsKey(jsonKey)){
				jsonKey = customheaderMap.get(jsonKey);
			}
			if(!ExcelValidatorConstant.EMPTY_STRING.equals(dependencyErrorBuilder.toString())){
				String dependentHeaders=dependencyErrorBuilder.toString().replaceAll(excelSheet.messageDelimitter()+"$", "");
				if(!ExcelValidatorConstant.EMPTY_STRING.equals(excelHeaderValidator.userDefinedMessage())) {
					String userDefinedMessage = excelHeaderValidator.userDefinedMessage();
					errorMessage = userDefinedMessage.replace(ExcelHeaderConstant.TARGET_HEADER_PLACEHOLDER, jsonKey);
					errorMessage = userDefinedMessage.replace(ExcelHeaderConstant.TARGET_HEADER_VALUE_PLACEHOLDER, columnValue+"");
					errorMessage = userDefinedMessage.replace(ExcelHeaderConstant.DEPENDENT_HEADERS_PLACEHOLDER, dependentHeaders);
				}
				else {					
					errorMessage = dependentHeaders+" cannot be empty when "+jsonKey+" is "+columnValue;
				}
			}
		}
		return errorMessage;
	}
	
	public String processDependencyHeadersWithCustomValidation(
			ExcelHeaderValidator excelHeaderValidator,
			String[] dependencyHeaders,
			String[] dependencyHeadersWithCustomValidation,
			ValidatorContext validatorContext,
			Map<String,String> customheaderMap,
			BaseExcelValidationSheet jsonExcelBean,
			Object columnValue,
			StringBuilder dependencyErrorBuilder,
			String jsonKey,
			String errorMessage
			)
	{
		if(dependencyHeadersWithCustomValidation.length>0 && dependencyHeadersWithCustomValidation[0]!=null){
			ExcelSheet excelSheet = validatorContext.getExcelSheet();
			IExcelValidator customValidator = ValidatorFactory.getValidator(ExcelValidatorConstant.EXCEL_CUSTOM_METHOD_VALIDATOR);
			for(String dependentHeaderWithCustomValidator:dependencyHeadersWithCustomValidation){
				if(dependentHeaderWithCustomValidator==null){
					continue;
				}			
				String customMethod = dependentHeaderWithCustomValidator.split(ExcelValidatorConstant.EXCEL_CUSTOM_METHOD_VALIDATOR_PLACEHOLDER)[1];				
				validatorContext.setCustomTaskMethod(customMethod);
				dependencyErrorBuilder = new StringBuilder();
				if(errorMessage!=null){
					dependencyErrorBuilder.append(excelSheet.messageDelimitter());
				}
				dependencyErrorBuilder.append(customValidator.validate(validatorContext));
				dependencyErrorBuilder.append(excelSheet.messageDelimitter());
			}
			if(validatorContext.getCustomTaskMethod()!=null){
				validatorContext.setCustomTaskMethod(null);
			}
			if(errorMessage!=null){
				errorMessage=errorMessage+dependencyErrorBuilder.toString().replaceAll(excelSheet.messageDelimitter()+"$", "");
			}
			else{
				errorMessage=dependencyErrorBuilder.toString().replaceAll(excelSheet.messageDelimitter()+"$", "");
			}			
		}
		return errorMessage;
	}
}
