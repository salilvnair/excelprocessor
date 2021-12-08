package com.github.salilvnair.excelprocessor.v1.bean;

import com.github.salilvnair.excelprocessor.v1.reflect.context.ExcelValidationMessage;

import java.util.ArrayList;
import java.util.List;

public class BaseExcelValidationSheet extends BaseExcelSheet{

	private List<String> errorList;

	private List<ExcelValidationMessage> errorMessages;

	public List<String> getErrorList() {
		return errorList;
	}

	public void setErrorList(List<String> errorList) {
		this.errorList = errorList;
	}


	public List<ExcelValidationMessage> getErrorMessages() {
		if(errorMessages == null) {
			errorMessages = new ArrayList<>();
		}
		return errorMessages;
	}

	public void setErrorMessages(List<ExcelValidationMessage> errorMessages) {
		this.errorMessages = errorMessages;
	}
}
