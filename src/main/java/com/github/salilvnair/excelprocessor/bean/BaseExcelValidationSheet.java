package com.github.salilvnair.excelprocessor.bean;

import java.util.List;

public class BaseExcelValidationSheet extends BaseExcelSheet{

	private List<String> errorList;

	public List<String> getErrorList() {
		return errorList;
	}

	public void setErrorList(List<String> errorList) {
		this.errorList = errorList;
	}

	
}
