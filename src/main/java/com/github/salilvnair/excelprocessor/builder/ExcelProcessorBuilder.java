package com.github.salilvnair.excelprocessor.builder;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.json.JSONException;

import com.github.salilvnair.excelprocessor.bean.BaseExcelSheet;
import com.github.salilvnair.excelprocessor.helper.ExcelProcessorUtil;
import com.github.salilvnair.excelprocessor.reflect.annotation.ExcelHeader;
import com.github.salilvnair.excelprocessor.reflect.annotation.ExcelSheet;
import com.github.salilvnair.excelprocessor.reflect.constant.ExcelValidatorConstant;
import com.github.salilvnair.excelprocessor.reflect.context.ExcelValidatorContext;
import com.github.salilvnair.excelprocessor.reflect.helper.AnnotationUtil;
/**
 * <b>ExcelProcessorBuilder</b> generates <b>MS-Excel(.xls,.xlsx)</b> 
 * file from <b> Bean(POJO)</b> values using JSON AND Apache-POI APIs and vice-versa
 * using the <b>ExcelProcessorUtil</b>
 * @author <b>Name:</b> Salil V Nair 
 */
public class ExcelProcessorBuilder {	
	private String firstRowValue="";
	private String lastRowValue="";
	private Map<String,String> customHeader;
	private double rowHeight=0;
	private short formatDateIndex=0;
	private int headerRowNumber=0;
	private String headerColumn="A";
	private boolean hasStyleTemplate;
	private int copyStyleFromRow = -1;
	private boolean hasExcelTemplate;
	private boolean ignoreFormatting;
	private boolean wrapTexting;
	private File excelTemplate;
	private final ExcelProcessorUtil excelProcessorUtil = new ExcelProcessorUtil();
	private List<? extends BaseExcelSheet> toExcelList;
	private LinkedHashMap<String,List<? extends BaseExcelSheet>> toExcelListOrderedMap;
	private String excelFileType;
	private String sheetName;
	private Workbook existingWorkBook;
	private boolean hasCustomHeader;
	private boolean pivotEnabled;
	private boolean hasInfoRowFirst;
	private boolean hasInfoRowLast;
	private File excelfile;
	private Class<? extends BaseExcelSheet> fromExcelHeaderBeanClass;
	private Map<String,Class<? extends BaseExcelSheet>> fromExcelBeanMap;
	private Class<? extends BaseExcelSheet>[] fromExcelHeaderBeanClasses;
	private boolean hasMultiReaderSheet;
	private Map<String, Object[]> fromExcelSheetReaderMetaDataMap;	
	private boolean hasInfoRowDetail;
	private boolean hasPivotDetail;
	private boolean isSingleValueVerticalSheet;
	private boolean verticallyScatteredHeaders;
	private List<String> ignoreHeaderList;
	private boolean autoResizeColoumn;
	private ExcelValidatorContext excelValidatorContext;
	private String singleSheetName;
	private boolean hasValidation = false;
	private boolean ignoreExcelAnnotation = false;
	public static final String EXCEL_FILE_TYPE_XLS ="xls";
	public static final String EXCEL_FILE_TYPE_XLSX ="xlsx";	
	private boolean enableHBMGenerator = false;
	private boolean enableTableGenerator = false;
	private final String BASE_EXCEL_SHEET_FULLY_QUALIFIED_NAME="org.bitbucket.kyrosprogrammer.bean.BaseExcelSheet";
	private List<String> toExcelOrderedFieldNameList;
	private Map<String,List<String>> toExcelOrderedFieldNameMap;
	private Set<String> dynamicFields;
	private Map<String,Set<String>> dynamicHeaderMap;
	private boolean copyHeaderStyle = false;
	private Map<String, String> dynamicFieldHeaderMap;
	private boolean hasMultiOrientationInSheet;
	private List<? extends Object> multiOrientedExcelList;
	
	public ExcelProcessorBuilder copyHeaderStyle(boolean copyHeaderStyle) {
		this.copyHeaderStyle = copyHeaderStyle;
		return this;
	}
	
	public ExcelProcessorBuilder setMultiOrientedExcelList(List<? extends Object> multiOrientedExcelList) {
		this.multiOrientedExcelList = multiOrientedExcelList;
		this.hasMultiOrientationInSheet = true;
		return this;
	}
		
	public ExcelProcessorBuilder setEnableHBMGenerator(boolean enableHBMGenerator) {
		this.enableHBMGenerator = enableHBMGenerator;
		return this;
	}
	
	public ExcelProcessorBuilder copyRowStyleFromExistingTemplate(int rowNumber) {
		this.copyStyleFromRow = rowNumber-1;
		this.hasStyleTemplate = true;
		return this;
	}

	public ExcelProcessorBuilder validateInDetail() {
		if(this.excelValidatorContext==null) {
			this.excelValidatorContext = new ExcelValidatorContext();
		}
		this.excelValidatorContext.setValidateInDetail(true);
		return this;
	}
	
	public ExcelProcessorBuilder setToExcelOrderedFieldNameListMap(Map<String,List<String>> toExcelOrderedFieldNameMap) {
		this.toExcelOrderedFieldNameMap = toExcelOrderedFieldNameMap;
		return this;
	}
	
	public ExcelProcessorBuilder setToExcelOrderedFieldNameList(List<String> toExcelOrderedFieldNameList) {
		this.toExcelOrderedFieldNameList = toExcelOrderedFieldNameList;
		return this;
	}
	
	public ExcelProcessorBuilder setDynamicFieldMap(Map<String,Set<String>> dynamicHeaderMap) {
		this.dynamicHeaderMap = dynamicHeaderMap;
		return this;
	}
		
	public ExcelProcessorBuilder setDynamicFieldHeaderMap(Map<String,String> dynamicFieldHeaderMap) {
		this.dynamicFieldHeaderMap = dynamicFieldHeaderMap;
		if(dynamicFieldHeaderMap!=null && !dynamicFieldHeaderMap.isEmpty()) {
			this.dynamicFields = dynamicFieldHeaderMap.keySet();
			if(dynamicFieldHeaderMap instanceof LinkedHashMap) {
				List<String> orderedFieldNameList = new ArrayList<String>();
				orderedFieldNameList.addAll(dynamicFieldHeaderMap.keySet());
				this.toExcelOrderedFieldNameList = orderedFieldNameList;
			}
		}
		return this;
	}

	public ExcelProcessorBuilder setEnableTableGenerator(boolean enableTableGenerator) {
		this.enableTableGenerator = enableTableGenerator;
		return this;
	}
	
	public ExcelProcessorBuilder setExcelValidatorContext(ExcelValidatorContext excelValidatorContext) {
		this.excelValidatorContext = excelValidatorContext;
		return this;
	}
	
	public ExcelProcessorBuilder setHasValidation(boolean hasValidation) {
		this.hasValidation = hasValidation;
		return this;
	}
	
	public ExcelProcessorBuilder setIgnoreExcelAnnotation(boolean ignoreExcelAnnotation) {
		this.ignoreExcelAnnotation = ignoreExcelAnnotation;
		return this;
	}
	
	public ExcelProcessorBuilder setAutoResizeColoumn(boolean autoResizeColoumn) {
		this.autoResizeColoumn = autoResizeColoumn;
		return this;
	}
	
	public ExcelProcessorBuilder setFirstRowValue(String firstRowValue) {
		this.firstRowValue = firstRowValue;
		return this;
	}
	
	
	public ExcelProcessorBuilder setLastRowValue(String lastRowValue) {
		this.lastRowValue = lastRowValue;
		return this;
	}

	public ExcelProcessorBuilder setCustomHeader(Map<String,String> customHeader,boolean flip) {
		this.hasCustomHeader = true;
		if(flip){
			customHeader=this.excelProcessorUtil.flipMap(customHeader);
		}
		this.customHeader = customHeader;
		return this;
	}
	
	
	public ExcelProcessorBuilder setRowHeight(double rowHeight) {
		this.rowHeight = rowHeight;
		return this;
	}
	
	public ExcelProcessorBuilder setFormatDateIndex(short formatDateIndex) {
		this.formatDateIndex = formatDateIndex;
		return this;
	}
	
	public ExcelProcessorBuilder setHeaderRowNumber(int headerRowNumber) {
		this.headerRowNumber = headerRowNumber;
		return this;
	}
	
	public ExcelProcessorBuilder setHeaderColumn(String headerColumn) {
		this.headerColumn = headerColumn;
		return this;
	}
	
	public ExcelProcessorBuilder setHasStyleTemplate(boolean hasStyleTemplate) {
		this.hasStyleTemplate = hasStyleTemplate;
		return this;
	}
		
	public ExcelProcessorBuilder setIgnoreFormatting(boolean ignoreFormatting) {
		this.ignoreFormatting = ignoreFormatting;
		return this;
	}
	
	public ExcelProcessorBuilder setWrapTexting(boolean wrapTexting) {
		this.wrapTexting = wrapTexting;
		return this;
	}
	
	public ExcelProcessorBuilder setExcelTemplate(File excelTemplate) {
		this.hasExcelTemplate = true;
		this.excelTemplate = excelTemplate;
		return this;
	}
	
	public ExcelProcessorBuilder fromSheetList(List<? extends BaseExcelSheet> toExcelList) {
		this.toExcelList = toExcelList;
		return this;
	}
	
	public ExcelProcessorBuilder fromSheetMap(LinkedHashMap<String,List<? extends BaseExcelSheet>> toExcelListOrderedMap) {
		this.toExcelListOrderedMap = toExcelListOrderedMap;
		return this;
	}
	
	public ExcelProcessorBuilder setExcelFileType(String excelFileType) {
		this.excelFileType = excelFileType;
		return this;
	}
	
	public ExcelProcessorBuilder setSheetName(String sheetName) {
		this.sheetName = sheetName;
		return this;
	}
	
	public ExcelProcessorBuilder setExistingWorkBook(Workbook existingWorkBook) {
		this.existingWorkBook = existingWorkBook;
		return this;
	}

	public ExcelProcessorBuilder setIsVertical(boolean pivotEnabled) {
		this.pivotEnabled = pivotEnabled;
		hasPivotDetail = pivotEnabled;
		return this;
	}
		
	public ExcelProcessorBuilder setHasInfoRowFirst(boolean hasInfoRowFirst) {
		this.hasInfoRowFirst = hasInfoRowFirst;
		this.hasInfoRowDetail = true;
		return this;
	}
	
	public ExcelProcessorBuilder setHasInfoRowLast(boolean hasInfoRowLast) {
		this.hasInfoRowLast = hasInfoRowLast;
		this.hasInfoRowDetail = true;
		return this;
	}
	
	public ExcelProcessorBuilder setExcelfile(File excelfile) {
		this.excelfile = excelfile;
		return this;
	}
	
	public ExcelProcessorBuilder setFromExcelHeaderBean(Class<? extends BaseExcelSheet> fromExcelHeaderBeanClass) {
		this.fromExcelHeaderBeanClass = fromExcelHeaderBeanClass;
		return this;
	}
	
	public ExcelProcessorBuilder setFromExcelBeanMap(Map<String,Class<? extends BaseExcelSheet>> fromExcelBeanMap) {
		this.fromExcelBeanMap = fromExcelBeanMap;
		return this;
	}
	
	public ExcelProcessorBuilder setExcelMappingBeanClass(Class<? extends BaseExcelSheet> fromExcelHeaderBeanClass) {
		this.fromExcelHeaderBeanClass = fromExcelHeaderBeanClass;
		return this;
	}
	
	@SuppressWarnings("unchecked")
	public ExcelProcessorBuilder setExcelMappingBeanClass(String baseExcelSheetClassName) throws ClassNotFoundException {
		this.fromExcelHeaderBeanClass = (Class<? extends BaseExcelSheet>) Class.forName(baseExcelSheetClassName);
		return this;
	}
	
	@SuppressWarnings("unchecked")
	public ExcelProcessorBuilder setExcelMappingBeanClasses(Class<?>... fromExcelHeaderBeanClasses) {
		this.fromExcelHeaderBeanClasses =  (Class<? extends BaseExcelSheet>[]) fromExcelHeaderBeanClasses;
		return this;
	}
	
	@SuppressWarnings("unchecked")
	public ExcelProcessorBuilder setExcelMappingBeanClasses(String[] baseExcelSheetClassNames) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		int arraySize = baseExcelSheetClassNames.length;
		Class<BaseExcelSheet> newBaseSheet = (Class<BaseExcelSheet>) Class.forName(BASE_EXCEL_SHEET_FULLY_QUALIFIED_NAME);
		Class<? extends BaseExcelSheet>[] finalBaseExcelSheetClass = (Class<? extends BaseExcelSheet>[]) Array.newInstance(newBaseSheet.getClass(),arraySize);
		int classSheetCounter = 0;
		for(String className:baseExcelSheetClassNames) {
			Class<? extends BaseExcelSheet> baseExcelSheetClass =  (Class<? extends BaseExcelSheet>) Class.forName(className);
			finalBaseExcelSheetClass[classSheetCounter] = baseExcelSheetClass;
			classSheetCounter++;
		}
		this.fromExcelHeaderBeanClasses = finalBaseExcelSheetClass;
		return this;
	}
	
	public ExcelProcessorBuilder setDatasetKeyValueMap(Map<String,List<String>> datasetKeyValueMap) {
		if(this.excelValidatorContext==null) {
			this.excelValidatorContext = new ExcelValidatorContext();
		}
		excelValidatorContext.setPredefinedDatasetMap(datasetKeyValueMap);
		return this;
	}
	
	public ExcelProcessorBuilder setUserValidatorMap(Map<String,Object> userValidatorMap) {
		if(this.excelValidatorContext==null) {
			this.excelValidatorContext = new ExcelValidatorContext();
		}
		excelValidatorContext.setUserValidatorMap(userValidatorMap);
		return this;
	}
		
	public ExcelProcessorBuilder setHasMultiReaderSheet(boolean hasMultiReaderSheet) {
		this.hasMultiReaderSheet = hasMultiReaderSheet;
		return this;
	}
	
	public ExcelProcessorBuilder setFromExcelSheetReaderMetaDataMap(Map<String, Object[]> fromExcelSheetReaderMetaDataMap) {
		this.fromExcelSheetReaderMetaDataMap = fromExcelSheetReaderMetaDataMap;
		return this;
	}
	
	public ExcelProcessorBuilder setIgnoreHeaderList(List<String> ignoreHeaderList) {
		this.ignoreHeaderList = ignoreHeaderList;
		return this;
	}
	
	
	private Workbook toWorkBook() throws InstantiationException, IllegalAccessException, ClassNotFoundException, JSONException, IOException, ParseException {
		return invokeToExcel();
	}
	
	public ExcelProcessorBuilder toExcel() throws InstantiationException, IllegalAccessException, ClassNotFoundException, JSONException, IOException, ParseException {
		preProcessExcelProcessorUtil(true);
		initExcelProcessorUtil();
		return this;
	}
	
	public Workbook generateWorkbook() throws Exception{
		return toWorkBook();
	}
	
	public void save(String fileName,String filePath) throws Exception{
		Workbook workbook = toWorkBook();
		if(workbook!=null){
			this.excelProcessorUtil.uploadExcelOnGivenPath(workbook, fileName+"."+excelFileType, filePath);
		}
	}
	
	public List<? extends Object> fromExcelSheet() throws NoSuchFieldException, SecurityException, ClassNotFoundException, InstantiationException, IllegalAccessException, IOException, JSONException, InvalidFormatException{
		preProcessExcelProcessorUtil(false);
		initExcelProcessorUtil();
		return invokeFromExcelSheet();
	}
	
	public ExcelProcessorBuilder fromExcel() throws NoSuchFieldException, SecurityException, ClassNotFoundException, InstantiationException, IllegalAccessException, IOException, JSONException, InvalidFormatException {
		preProcessExcelProcessorUtil(false);
		initExcelProcessorUtil();
		return this;
	}
	
	@SuppressWarnings("unchecked")
	public List<Map<String,List<String>>> toErrorMapList() throws NoSuchFieldException, SecurityException, ClassNotFoundException, InstantiationException, IllegalAccessException, IOException, JSONException, InvalidFormatException {
		Map<String, List<? extends Object>> extractedMap = invokeFromExcelMap();
		if(extractedMap.containsKey(ExcelProcessorUtil.EXCEL_ERROR_LIST)) {
			return (List<Map<String, List<String>>>) extractedMap.get(ExcelProcessorUtil.EXCEL_ERROR_LIST);
		}
		return null;
	}
	
	public Map<String, List<? extends Object>> toMap() throws NoSuchFieldException, SecurityException, ClassNotFoundException, InstantiationException, IllegalAccessException, IOException, JSONException, InvalidFormatException {
		return invokeFromExcelMap();
	}
	
	@SuppressWarnings("unchecked")
	public <T> List<T> toSheetList(Class<T> sheetClazz) throws NoSuchFieldException, SecurityException, ClassNotFoundException, InstantiationException, IllegalAccessException, IOException, JSONException, InvalidFormatException {
		Map<String, List<? extends Object>> extractedMap = invokeFromExcelMap();
		BaseExcelSheet baseExcelSheet = (BaseExcelSheet) sheetClazz.newInstance();
		if(baseExcelSheet.getClass().isAnnotationPresent(ExcelSheet.class)) {
			ExcelSheet excelSheet = baseExcelSheet.getClass().getAnnotation(ExcelSheet.class);
			String sheetName = excelSheet.value();
			return (List<T>) extractedMap.get(sheetName);
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public  <T> Map<String, List<? extends BaseExcelSheet>> toSheetMap() throws NoSuchFieldException, SecurityException, ClassNotFoundException, InstantiationException, IllegalAccessException, IOException, JSONException, InvalidFormatException {
		Map<String, List<? extends Object>> extractedMap = invokeFromExcelMap();
		Map<String, List<? extends BaseExcelSheet>> sheetMap = new HashMap<>();
		for(Class<? extends BaseExcelSheet> beanClass:fromExcelHeaderBeanClasses) {
			BaseExcelSheet baseExcelSheet = beanClass.newInstance();
			if(baseExcelSheet.getClass().isAnnotationPresent(ExcelSheet.class)) {
				ExcelSheet excelSheet = baseExcelSheet.getClass().getAnnotation(ExcelSheet.class);
				String sheetName = excelSheet.value();
				sheetMap.put(sheetName, (List<? extends BaseExcelSheet>) extractedMap.get(sheetName));
			}
		}
		if(!sheetMap.isEmpty()) {
			return sheetMap;
		}
		return null;
	}
	
	public Map<String, Map<String, List<? extends Object>>> toSheetMap(int fromRow, int toRow) throws NoSuchFieldException, SecurityException, ClassNotFoundException, InstantiationException, IllegalAccessException, IOException, JSONException {
		return invokeFromExcelMap(fromRow,toRow);
	}
	
	public Map<String, Map<String, Object>> validate() throws NoSuchFieldException, SecurityException, ClassNotFoundException, InstantiationException, IllegalAccessException, IOException, JSONException{
		prePopulateHeaderBeanClasses();
		preProcessExcelProcessorUtil(false);
		initExcelProcessorUtil();
		return validateExcelBeanMap();
	}
	
	public HashMap<String, HashMap<String, Object>> toExcelInfo() throws InstantiationException, IllegalAccessException {
		return invokeExcelInfo();
	}
	
	public HashMap<String, Object> toSheetInfo() throws InstantiationException, IllegalAccessException {
		return invokeExcelSheetInfo();
	}
	
	@SuppressWarnings("unchecked")
	private void prePopulateHeaderBeanClasses() {
		if(fromExcelHeaderBeanClass!=null){
			List<Class<? extends BaseExcelSheet> > fromExcelHeaderBeanClassList = new ArrayList<>();			
			fromExcelHeaderBeanClassList.add(fromExcelHeaderBeanClass);
			fromExcelHeaderBeanClasses = (Class<? extends BaseExcelSheet>[]) Array.newInstance(fromExcelHeaderBeanClass.getClass(),1);
			fromExcelHeaderBeanClasses[0] = fromExcelHeaderBeanClass;
		}
	}
	
	public String generateExcelSheetMappingBeanFromExcel() throws ClassNotFoundException, IOException {
		initExcelProcessorUtil();		
		return this.excelProcessorUtil.generateExcelSheetMappingBeanFromExcel(excelfile, sheetName, pivotEnabled, ignoreHeaderList,hasValidation);
	}
	
	private void preProcessExcelProcessorUtil(boolean isToExcel){
		try {
			processSheetAnnotation();
			processHeaderAnnotation(isToExcel);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void processSheetAnnotation() throws InstantiationException, IllegalAccessException {
		prepareSheetMetaDataMap();
	}
	
	private void prepareSheetMetaDataMap() throws InstantiationException, IllegalAccessException{
		if(fromExcelHeaderBeanClasses!=null){
			Map<String,Class<? extends BaseExcelSheet>> fromExcelBeanMap = new HashMap<>();
			Map<String, Object[]> fromExcelSheetReaderMetaDataMap = new HashMap<>();
			List<Boolean> multiSheetAlignList = new ArrayList<>();
			for(Class<? extends BaseExcelSheet> excelHeaderBeanClass:fromExcelHeaderBeanClasses){
				BaseExcelSheet baseExcelSheet = excelHeaderBeanClass.newInstance();
				if(baseExcelSheet.getClass().isAnnotationPresent(ExcelSheet.class)){										
					ExcelSheet excelSheet = baseExcelSheet.getClass().getAnnotation(ExcelSheet.class);					
					multiSheetAlignList.add(excelSheet.isVertical());					
					String sheetName = excelSheet.value();
					if(!"".equals(sheetName)){
						String[] ignoreHeaders = excelSheet.ignoreHeaders();
						List<String> ignoreHeaderList = null;
						if(ignoreHeaders.length>0) {
							ignoreHeaderList = Arrays.asList(ignoreHeaders);
						}
						else {
							String ignoreHeaderKey = excelSheet.ignoreHeaderKey();
							if(excelValidatorContext!=null){
								if(excelValidatorContext.getPredefinedDatasetMap()!=null){
									if(excelValidatorContext.getPredefinedDatasetMap().containsKey(ignoreHeaderKey)){
										ignoreHeaderList = excelValidatorContext.getPredefinedDatasetMap().get(ignoreHeaderKey);
									}
								}
							}
						}
						
						setIgnoreHeaderList(ignoreHeaderList);
						setHeaderRowNumber(excelSheet.headerRowAt());
						setHeaderColumn(excelSheet.headerColumnAt());
						if(sheetName==null){
							setSheetName(excelSheet.value());
						}
						if(!hasPivotDetail){
							setIsVertical(excelSheet.isVertical());
						}
						isSingleValueVerticalSheet = excelSheet.isSingleValueVerticalSheet();
						verticallyScatteredHeaders = excelSheet.verticallyScatteredHeaders();
						prepareMultiSheetReaderMetaData(excelSheet,excelHeaderBeanClass,fromExcelSheetReaderMetaDataMap);
						fromExcelBeanMap.put(sheetName, excelHeaderBeanClass);
					}
				}
			}
			if(multiSheetAlignList.contains(true) && multiSheetAlignList.contains(false)){
				setFromExcelSheetReaderMetaDataMap(fromExcelSheetReaderMetaDataMap);
				setFromExcelBeanMap(fromExcelBeanMap);
			}
			else{
				fromExcelSheetReaderMetaDataMap = null;
				setFromExcelBeanMap(fromExcelBeanMap);
			}
		}
		else if(toExcelList!=null && !toExcelList.isEmpty()){
			prepareToExcelMetaData(toExcelList);
		}
	}
	
	private void prepareToExcelMetaData(List<? extends BaseExcelSheet> toExcelList) {
		if(toExcelList.get(0).getClass().isAnnotationPresent(ExcelSheet.class)){										
			ExcelSheet excelSheet = toExcelList.get(0).getClass().getAnnotation(ExcelSheet.class);	
			if(!"".equals(excelSheet.value())){
				isSingleValueVerticalSheet = excelSheet.isSingleValueVerticalSheet();
				verticallyScatteredHeaders = excelSheet.verticallyScatteredHeaders();
				setHeaderRowNumber(excelSheet.headerRowAt());
				setHeaderColumn(excelSheet.headerColumnAt());
				setSheetName(excelSheet.value());
				String[] ignoreHeaders = excelSheet.ignoreHeaders();
				List<String> ignoreHeaderList = null;
				if(ignoreHeaders.length>0) {
					ignoreHeaderList = Arrays.asList(ignoreHeaders);
				}
				else {
					String ignoreHeaderKey = excelSheet.ignoreHeaderKey();
					if(excelValidatorContext!=null){
						if(excelValidatorContext.getPredefinedDatasetMap()!=null){
							if(excelValidatorContext.getPredefinedDatasetMap().containsKey(ignoreHeaderKey)){
								ignoreHeaderList = excelValidatorContext.getPredefinedDatasetMap().get(ignoreHeaderKey);
							}
						}
					}
				}
				setIgnoreHeaderList(ignoreHeaderList);
				if(!hasPivotDetail){
					setIsVertical(excelSheet.isVertical());
				}
				if(excelFileType==null) {
					setExcelFileType(excelSheet.type());
				}
			}
		}
	}

	private void prepareMultiSheetReaderMetaData(ExcelSheet excelSheet,Class<? extends BaseExcelSheet> excelHeaderBeanClass, Map<String, Object[]> fromExcelSheetReaderMetaDataMap) throws InstantiationException, IllegalAccessException {
		Object[] sheetReaderMetaData = new Object[4];//row//piv//cust//bean
		BaseExcelSheet baseExcelSheet = excelHeaderBeanClass.newInstance();
		if(excelSheet.isVertical()){
			sheetReaderMetaData[0] = excelSheet.headerColumnAt();
		}
		else{
			sheetReaderMetaData[0] = excelSheet.headerRowAt();
		}
		sheetReaderMetaData[1] = excelSheet.isVertical();
		
		Map<String,String> customHeaderMap = new HashMap<>();
		prepareHeaderMapFromHeaderBean(baseExcelSheet, customHeaderMap);
		sheetReaderMetaData[2] = customHeaderMap;
		sheetReaderMetaData[3] = baseExcelSheet;
		fromExcelSheetReaderMetaDataMap.put(excelSheet.value(), sheetReaderMetaData);
	}

	private void processFromExcelBean(Map<String,String> customHeaderMap) throws InstantiationException, IllegalAccessException{
		if(fromExcelHeaderBeanClass!=null){
			BaseExcelSheet fromExcelHeaderBean =  fromExcelHeaderBeanClass.newInstance();
			if(fromExcelHeaderBean.getClass().isAnnotationPresent(ExcelSheet.class)){
				ExcelSheet excelSheet = fromExcelHeaderBean.getClass().getAnnotation(ExcelSheet.class);
				setHeaderRowNumber(excelSheet.headerRowAt());
				setHeaderColumn(excelSheet.headerColumnAt());
				isSingleValueVerticalSheet = excelSheet.isSingleValueVerticalSheet();
				verticallyScatteredHeaders = excelSheet.verticallyScatteredHeaders();
				singleSheetName = "".equals(excelSheet.value())? null:excelSheet.value();
			}
		}
	}
		
	private void prepareHeaderMapFromHeaderBean(BaseExcelSheet baseExcelSheet,Map<String,String> customHeaderMap){
		Set<Field> fields = AnnotationUtil.getAnnotatedFields(baseExcelSheet.getClass(), ExcelHeader.class);
		for(Field field:fields){
			ExcelHeader excelHeader = field.getAnnotation(ExcelHeader.class);
			String headerValue = excelHeader.value();
			ExcelSheet excelSheet = getExcelSheetFromBaseExcelSheet(baseExcelSheet);
			String appendOnDuplicate = null;
			if(!excelSheet.isVertical()) {
				String column = excelHeader.column();
				if(!ExcelValidatorConstant.EMPTY_STRING.equals(column)) {
					appendOnDuplicate = column;
				}
			}
			else {
				int row = excelHeader.row();
				if(row!=-1) {
					appendOnDuplicate = row+"";
				}
			}
			if(appendOnDuplicate!=null) {
				headerValue = headerValue+ExcelProcessorUtil.APPEND_UNDERSCORE+appendOnDuplicate;
			}

			String beanValue = field.getName();
			
			if(headerValue!=null && !"".equals(headerValue)){
				customHeaderMap.put(headerValue, beanValue);
			}
		}
	}
		
	public static ExcelSheet getExcelSheetFromBaseExcelSheet(BaseExcelSheet baseExcelSheet) {
		ExcelSheet excelSheet = null;
		if(baseExcelSheet.getClass().isAnnotationPresent(ExcelSheet.class)){
			excelSheet = baseExcelSheet.getClass().getAnnotation(ExcelSheet.class);
		}
		return excelSheet;
	}
	
	public static ExcelSheet getExcelSheetFromBaseExcelSheetList(List<? extends BaseExcelSheet> baseExcelSheetList) {
		ExcelSheet excelSheet = null;
		if(baseExcelSheetList!=null && !baseExcelSheetList.isEmpty()
				&& baseExcelSheetList.get(0).getClass().isAnnotationPresent(ExcelSheet.class)){
			excelSheet = baseExcelSheetList.get(0).getClass().getAnnotation(ExcelSheet.class);
		}
		return excelSheet;
	}
	
	public static String getExcelSheetNameFromBaseExcelSheetList(List<? extends BaseExcelSheet> baseExcelSheetList) {
		ExcelSheet excelSheet = getExcelSheetFromBaseExcelSheetList(baseExcelSheetList);
		if(excelSheet!=null) {
			return excelSheet.value();
		}
		return null;
	}
	
	private Map<String, Map<String, List<? extends Object>>> invokeFromExcelMap(int fromRow,int toRow) throws NoSuchFieldException, SecurityException, ClassNotFoundException, InstantiationException, IllegalAccessException, IOException, JSONException {
		if(fromExcelBeanMap!=null) {
			return this.excelProcessorUtil.fromExcelBeanMap(excelfile, existingWorkBook, fromRow, toRow, fromExcelBeanMap, hasCustomHeader);
		}
		else {
			return null;
		}
	}

	private void initExcelProcessorUtil() {
		this.excelProcessorUtil.setFirstRowValue(firstRowValue);
		this.excelProcessorUtil.setLastRowValue(lastRowValue);
		this.excelProcessorUtil.setCustomHeader(customHeader);
		this.excelProcessorUtil.setRowHeight(rowHeight);
		this.excelProcessorUtil.setFormatDateIndex(formatDateIndex);
		this.excelProcessorUtil.setHeaderRowNumber(headerRowNumber);
		this.excelProcessorUtil.setHeaderColumn(headerColumn);
		this.excelProcessorUtil.setHasStyleTemplate(hasStyleTemplate);
		this.excelProcessorUtil.setCopyStyleFromRow(copyStyleFromRow);
		this.excelProcessorUtil.setHasExcelTemplate(hasExcelTemplate);
		this.excelProcessorUtil.setIgnoreFormatting(ignoreFormatting);
		this.excelProcessorUtil.setWrapTexting(wrapTexting);
		this.excelProcessorUtil.setExcelTemplate(excelTemplate);
		this.excelProcessorUtil.setForceAutoSizing(autoResizeColoumn);
		this.excelProcessorUtil.setExcelValidatorContext(excelValidatorContext);
		this.excelProcessorUtil.setIgnoreHeaderList(ignoreHeaderList);
		this.excelProcessorUtil.setIgnoreExcelAnnotation(ignoreExcelAnnotation);
		this.excelProcessorUtil.setEnableHBMGenerator(enableHBMGenerator);
		this.excelProcessorUtil.setEnableTableGenerator(enableTableGenerator);
		this.excelProcessorUtil.setToExcelOrderedFieldNameList(toExcelOrderedFieldNameList);
		this.excelProcessorUtil.setToExcelOrderedFieldNameMap(toExcelOrderedFieldNameMap);
		this.excelProcessorUtil.setDynamicFields(dynamicFields);
		this.excelProcessorUtil.setDynamicFieldMap(dynamicHeaderMap);
		this.excelProcessorUtil.setCopyHeaderStyle(copyHeaderStyle);
		this.excelProcessorUtil.setDynamicFieldHeaderMap(dynamicFieldHeaderMap);
		this.excelProcessorUtil.setMultiOrientedExcelList(multiOrientedExcelList);
	}

	private Map<String, Map<String,Object>> validateExcelBeanMap() throws NoSuchFieldException, SecurityException, ClassNotFoundException, InstantiationException, IllegalAccessException, IOException, JSONException{
		return this.excelProcessorUtil.validateExcelBeanMap(excelfile, existingWorkBook, fromExcelBeanMap, hasCustomHeader, pivotEnabled);
	}
	
	private Map<String, List<? extends Object>> invokeFromExcelMap() throws NoSuchFieldException, SecurityException, ClassNotFoundException, InstantiationException, IllegalAccessException, IOException, JSONException, InvalidFormatException {
		if(fromExcelBeanMap!=null && fromExcelSheetReaderMetaDataMap==null) {
			if(hasPivotDetail) {
				if(verticallyScatteredHeaders) {
					return this.excelProcessorUtil.fromExcelVerticalScatteredSheetBeanMap(excelfile, existingWorkBook, fromExcelBeanMap, hasCustomHeader, pivotEnabled);
				}
				else if(isSingleValueVerticalSheet){
					return this.excelProcessorUtil.fromSingleValueVerticalSheetBeanMap(excelfile, existingWorkBook, fromExcelBeanMap, hasCustomHeader, pivotEnabled);
				}
				else{
					return this.excelProcessorUtil.fromExcelVerticalSheetBeanMap(excelfile, existingWorkBook, fromExcelBeanMap, hasCustomHeader, pivotEnabled);	
				}
			}
			else if(hasInfoRowDetail) {
				return this.excelProcessorUtil.fromExcelBeanMap(excelfile, fromExcelBeanMap, hasCustomHeader, hasInfoRowFirst, hasInfoRowLast);
			}
			else {
				return this.excelProcessorUtil.fromExcelBeanMap(excelfile, existingWorkBook, fromExcelBeanMap, hasCustomHeader);
			}		
		}
		else if(fromExcelSheetReaderMetaDataMap!=null) {
			return this.excelProcessorUtil.fromExcelBeanMap(excelfile, hasMultiReaderSheet, fromExcelSheetReaderMetaDataMap, hasCustomHeader);
		}
		else if(singleSheetName!=null){
			Map<String, List<? extends Object>> singleSheetMap = new HashMap<>();
			List<? extends Object> excelSheetDataList = invokeFromExcelSheet();
			singleSheetMap.put(singleSheetName, excelSheetDataList);
			return singleSheetMap;
		}
		else {
			return null;
		}		
	}
	
	private List<? extends Object> invokeFromExcelSheet() throws NoSuchFieldException, SecurityException, ClassNotFoundException, InstantiationException, IllegalAccessException, IOException, JSONException, InvalidFormatException {
		return excelProcessorUtil.fromExcelBeanList(excelfile, fromExcelHeaderBeanClass, hasCustomHeader);
	}
	
	private HashMap<String,Object> invokeExcelSheetInfo() throws InstantiationException, IllegalAccessException{
		return excelProcessorUtil.excelSheetInfo(excelfile, fromExcelHeaderBeanClass);
	}
	
	private HashMap<String, HashMap<String, Object>> invokeExcelInfo() throws InstantiationException, IllegalAccessException{
		return excelProcessorUtil.excelInfo(excelfile, fromExcelBeanMap);
	}
	
	private Workbook invokeToExcel() throws InstantiationException, IllegalAccessException, ClassNotFoundException, JSONException, IOException, ParseException {
		if(this.toExcelList!=null) {
			if(this.existingWorkBook==null && !hasCustomHeader) {
				return excelProcessorUtil.toExcel(toExcelList, excelFileType, sheetName);
			}
			else {
				 if(hasInfoRowDetail) {
					 return excelProcessorUtil.toExcel(toExcelList, excelFileType, sheetName, existingWorkBook, hasCustomHeader, hasInfoRowFirst, hasInfoRowLast);
				 }
				 else {
					 if(hasMultiOrientationInSheet) {
						 return excelProcessorUtil.toExcel(hasMultiOrientationInSheet,toExcelList, excelFileType, sheetName, existingWorkBook, hasCustomHeader, pivotEnabled);
					 }
					 return excelProcessorUtil.toExcel(toExcelList, excelFileType, sheetName, existingWorkBook, hasCustomHeader, pivotEnabled);
				 }
			}		
		}
		else if(this.toExcelListOrderedMap!=null) {
			Workbook existingWorkBook = null;
			for(String sheet:toExcelListOrderedMap.keySet()) {
				List<? extends BaseExcelSheet> toExcelList = toExcelListOrderedMap.get(sheet);
				prepareToExcelMetaData(toExcelList);
				Map<String,String> customHeaderMap = new HashMap<>();
				processToExcelCustomHeaderMap(customHeaderMap,toExcelList);
				setCustomHeader(customHeaderMap, true);
				initExcelProcessorUtil();
				if(hasInfoRowDetail) {
				 existingWorkBook = excelProcessorUtil.toExcel(toExcelList, excelFileType, sheetName, existingWorkBook, hasCustomHeader, hasInfoRowFirst, hasInfoRowLast);
				}
				else {
				 existingWorkBook = excelProcessorUtil.toExcel(toExcelList, excelFileType, sheetName, existingWorkBook, hasCustomHeader, pivotEnabled);
				}				
			}
			return existingWorkBook;
		}
		else {
			return null;
		}
	}
	
	private void processHeaderAnnotation(boolean isToExcel) throws InstantiationException, IllegalAccessException{
		Map<String,String> customHeaderMap = new HashMap<>();
		processFromExcelBean(customHeaderMap);
		processToExcelCustomHeaderMap(customHeaderMap,toExcelList);
		setCustomHeader(customHeaderMap, isToExcel);
	}
	
	private void processToExcelCustomHeaderMap(Map<String, String> customHeaderMap, List<? extends BaseExcelSheet> toExcelList) {
		if(toExcelList!=null && !toExcelList.isEmpty()){
			BaseExcelSheet baseExcelSheet = toExcelList.get(0);
			prepareHeaderMapFromHeaderBean(baseExcelSheet, customHeaderMap);
		}
	}

	public ExcelProcessorBuilder clear() {
		firstRowValue = "";
		lastRowValue = "";
		customHeader = null;
		rowHeight=0;
		formatDateIndex=0;
		headerRowNumber=0;
		headerColumn="A";
		excelTemplate = null;	
		toExcelList = null;
		excelFileType = null;
		sheetName = null;
		existingWorkBook = null;
		excelfile = null;
		fromExcelHeaderBeanClass = null;
		fromExcelBeanMap = null;
		fromExcelSheetReaderMetaDataMap = null;	
		ignoreHeaderList = null;
		fromExcelHeaderBeanClasses = null;
		excelValidatorContext = null;
		ignoreHeaderList = null;
		singleSheetName = null;
		wrapTexting = false;
		hasValidation = false;
		pivotEnabled = false;
		hasStyleTemplate = false;
		copyStyleFromRow = -1;
		hasExcelTemplate = false;
		hasCustomHeader = false;
		hasInfoRowFirst = false;
		hasInfoRowLast = false;
		hasMultiReaderSheet = false;
		hasInfoRowDetail = false;
		hasPivotDetail = false;
		ignoreFormatting = false;
		autoResizeColoumn = false;
		isSingleValueVerticalSheet=false;
		verticallyScatteredHeaders = false;
		ignoreExcelAnnotation=false;
		enableHBMGenerator = false;
		enableTableGenerator = false;
		toExcelOrderedFieldNameList = null;
		toExcelOrderedFieldNameMap = null;
		dynamicFields = null;
		dynamicHeaderMap = null;
		copyHeaderStyle = false;
		dynamicFieldHeaderMap = null;
		multiOrientedExcelList = null;
		hasMultiOrientationInSheet = false;
		initExcelProcessorUtil();
		return this;
	}	
}
