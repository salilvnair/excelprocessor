package com.github.salilvnair.excelprocessor.v1.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.github.salilvnair.excelprocessor.v1.test.sheet.*;
import com.github.salilvnair.excelprocessor.util.StopWatch;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.util.IOUtils;
import org.json.JSONException;

import com.github.salilvnair.excelprocessor.v1.bean.BaseExcelSheet;
import com.github.salilvnair.excelprocessor.v1.builder.ExcelProcessorBuilder;

public class ExcelProcessorTestSuite {

	public static final String TEST_EXCEL_FOLDER = "excel";

	public static void main(String[] args) {
		String fileName = "ExcelProcessorTest.xls";
		
		//generateExcelSheetMappingBeanFromExcel(fileName);
		
		//validateExcel(fileName);
		
		readDataFromExcel(fileName);
		
		//writeToExcel(fileName);
		
		//writeToExcelWithMultipleOrientation(fileName);
		
		//getExcelInfo(fileName);

		//readAndWriteError(fileName);
		
	}

	private static void readAndWriteError(String fileName) {
		File excelfile = getFileFromResource(TEST_EXCEL_FOLDER,fileName);
		List<CountryStateInfoSheet> sheetData = null;
		try {
			ExcelProcessorBuilder excelProcessorBuilder = new ExcelProcessorBuilder();
			long startTime = System.nanoTime();
			excelProcessorBuilder.clear();
			sheetData =
					excelProcessorBuilder
							.setExcelfile(excelfile)
							.setExcelMappingBeanClasses(CountryStateInfoSheet.class)
							.validateInDetail()
							.fromExcel()
							.toSheetList(CountryStateInfoSheet.class);
			excelProcessorBuilder.clear();
			String currentUser = System.getProperty("user.name");
			String templateFile = "ExcelProcessorTest1.xls";
			File filetemplate = getFileFromResource(TEST_EXCEL_FOLDER,templateFile);
			excelProcessorBuilder
					.fromSheetList(sheetData)
					.setExcelTemplate(filetemplate)
					.copyHeaderStyle(true)
					.toExcel()
					.save("Test_Ouput","C:\\Users\\"+currentUser+"\\Desktop");

			long endTime = System.nanoTime();
			long duration = (endTime - startTime);
			System.out.println((duration/1000000));
			System.out.println("File saved at:"+"C:\\Users\\"+currentUser+"\\Desktop\\Test_Ouput.xls");

		}
		catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	private static void writeToExcelWithMultipleOrientation(String fileName) {
		
		try {
			ExcelProcessorBuilder excelProcessorBuilder = new ExcelProcessorBuilder();
			
			String templateFile = "OrderPreviewExportTemplate.xls";
			File filetemplate = getFileFromResource(TEST_EXCEL_FOLDER,templateFile);
			long startTime = System.nanoTime();
			
			String currentUser = System.getProperty("user.name");
			
			//random data with image test
			List<? extends BaseExcelSheet> toExcelList = new ArrayList<> ();
			List<?> multiOrientedSheetList = new ArrayList<> ();
			
			excelProcessorBuilder
			.fromSheetList(toExcelList)
			.setMultiOrientedExcelList(multiOrientedSheetList)
			.setExcelTemplate(filetemplate)
			//.copyHeaderStyle(true)
			.toExcel()
			.save("Test_Ouput","C:\\Users\\"+currentUser+"\\Desktop");
			
			long endTime = System.nanoTime();
			long duration = (endTime - startTime);
			System.out.println((duration/1000000));
			System.out.println("File saved at:"+"C:\\Users\\"+currentUser+"\\Desktop\\Test_Ouput.xls");
			
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
	}

	public static File getFileFromResource(String folder, String fileName){
		ExcelProcessorTestSuite excelProcessorTestSuite = new ExcelProcessorTestSuite();	
		ClassLoader classLoader = excelProcessorTestSuite.getClass().getClassLoader();
		File file = new File(classLoader.getResource(folder+"/"+fileName).getFile());	
		return file;
	}
	
	public static void generateExcelSheetMappingBeanFromExcel(String fileName) {

		//set this as true if the sheet has to be read vertically for example SummarySheet
		boolean isVerticalSheet = true;
		//set this to true if generated pojo has to extend BaseExcelValidationSheet
		// and metadata regarding the validation
		boolean hasHeaderValidation = true;
		//set this as true if you want to generate pojo for Hiberanate Entity 
		//which will be replica of sheet bean without excel header annotation
		boolean ignoreExcelAnnoation = false;
		
		String sheetName = "CountryStateInfo";
		
		int headerRowStartsFrom = 2;
		
		String headerColumnStartsFrom = "B";
		
		generateExcelSheetMappingBeanFromExcel(fileName,
											   sheetName,
											   headerRowStartsFrom,
											   headerColumnStartsFrom,
											   isVerticalSheet,
											   hasHeaderValidation,
											   ignoreExcelAnnoation);
	}
	
	public static void generateExcelSheetMappingBeanFromExcel(String fileName,String sheetName,int headerRow,String columnHeader,boolean isPivot,boolean hasHeaderValidation,boolean ignoreExcelAnnotation) {
		File excelfile = getFileFromResource(TEST_EXCEL_FOLDER,fileName);
		List<String> ignoreList = new ArrayList<>();

		ignoreList.add("Test Data");
		ignoreList.add("GENERAL INFORAMTION");
		ignoreList.add("ADDITIONAL INFORAMTION");
		ignoreList.add("Internet Info");
		ignoreList.add("Comments");

		try {
			ExcelProcessorBuilder excelProcessorBuilder = new ExcelProcessorBuilder();
			System.out.println(
					excelProcessorBuilder
					.setExcelfile(excelfile)
					.setSheetName(sheetName)
					.setIsVertical(isPivot)
					.setHeaderRowNumber(headerRow)
					.setHeaderColumn(columnHeader)
					.setHasValidation(hasHeaderValidation)
					.setIgnoreHeaderList(ignoreList)
					.setIgnoreExcelAnnotation(ignoreExcelAnnotation)
					.generateExcelSheetMappingBeanFromExcel());			
			
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}



	public static void validateExcel(String fileName){
		File file = getFileFromResource(TEST_EXCEL_FOLDER,fileName);
		try {
			ExcelProcessorBuilder excelProcessorBuilder = new ExcelProcessorBuilder();
			long startTime = System.nanoTime();
			Map<String, Map<String, Object>>  validationMap = excelProcessorBuilder
								.setExcelfile(file)
								.setExcelMappingBeanClasses(SchoolSheet.class,
															CollegeSheet.class,
															EmployerSheet.class)
								.validate();
			long endTime = System.nanoTime();
			long duration = (endTime - startTime);
			System.out.println((duration/1000000));
			System.out.println(validationMap);
			
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	public static void getExcelInfo(String fileName) {
		File excelfile = getFileFromResource(TEST_EXCEL_FOLDER,fileName);
		ExcelProcessorBuilder excelProcessorBuilder = new ExcelProcessorBuilder();
		try {
			HashMap<String, HashMap<String, Object>> excelInfoMap = excelProcessorBuilder
			.setExcelfile(excelfile)
			.setExcelMappingBeanClasses(SchoolSheet.class,
					CollegeSheet.class,
					EmployerSheet.class)
			.fromExcel()
			.toExcelInfo();
			
			for(String sheetName: excelInfoMap.keySet()) {
				HashMap<String, Object> sheetInfoMap = excelInfoMap.get(sheetName);
				System.out.println(sheetInfoMap);
			}
			
		} catch (InstantiationException | IllegalAccessException | NoSuchFieldException | SecurityException
				| ClassNotFoundException | InvalidFormatException | IOException | JSONException e) {
			e.printStackTrace();
		}
		
	}
	
	
	public static void readDataFromExcel(String fileName) {
		File excelfile = getFileFromResource(TEST_EXCEL_FOLDER,fileName);
		try {
			ExcelProcessorBuilder excelProcessorBuilder = new ExcelProcessorBuilder();
			excelProcessorBuilder.clear();
			StopWatch.start();

			Map<String, List<? extends BaseExcelSheet>> sheetMap = excelProcessorBuilder
					.setExcelfile(excelfile)
					.setExcelMappingBeanClasses(SchoolSheet.class)
					.validateInDetail()
					.fromExcel()
					.toSheetMap();
			excelProcessorBuilder.clear();
			System.out.println("excelprocessor v1 took " + StopWatch.elapsed(TimeUnit.MILLISECONDS) + " millisecond(s)");
			
		}
		catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	public static void writeToExcel(String fileName){
		try {
			ExcelProcessorBuilder excelProcessorBuilder = new ExcelProcessorBuilder();
			
			String templateFile = "ExcelProcessorTestTemplate.xls";
			File filetemplate = getFileFromResource(TEST_EXCEL_FOLDER,templateFile);
			long startTime = System.nanoTime();
			
			String currentUser = System.getProperty("user.name");
			List<SchoolSheet> toExcelList = new ArrayList<>();
			
			//random data with image test
			SchoolSheet schoolSheet = new SchoolSheet();
			schoolSheet.setName("Batman");
			schoolSheet.setState("Gotham");
			schoolSheet.setNoOfStudents(0);
			File batman = getFileFromResource("images", "batman.png");
			InputStream is = new FileInputStream(batman);
	        byte[] image = IOUtils.toByteArray(is);
	        schoolSheet.setImage(ArrayUtils.toObject(image));
			toExcelList.add(schoolSheet);
			schoolSheet = new SchoolSheet();
			schoolSheet.setName("Anubis");
			schoolSheet.setState("Unknown");
			schoolSheet.setNoOfStudents(0);
			File anubis = getFileFromResource("images", "anubis.png");
			InputStream is1 = new FileInputStream(anubis);
	        byte[] image1 = IOUtils.toByteArray(is1);
	        schoolSheet.setImage(ArrayUtils.toObject(image1));
			toExcelList.add(schoolSheet);
			Map<String,String> dynamicFieldHeaderMap = new LinkedHashMap<>();	
			dynamicFieldHeaderMap.put("state", "Bhai Ka State");
			dynamicFieldHeaderMap.put("name", "Bhai Ka Name");
			System.out.println(dynamicFieldHeaderMap.keySet());
			excelProcessorBuilder
			.fromSheetList(toExcelList)
			.setDynamicFieldHeaderMap(dynamicFieldHeaderMap)
			.setExcelTemplate(filetemplate)
			.copyHeaderStyle(true)
			.toExcel()
			.save("Test_Ouput","C:\\Users\\"+currentUser+"\\Desktop");
			
			long endTime = System.nanoTime();
			long duration = (endTime - startTime);
			System.out.println((duration/1000000));
			System.out.println("File saved at:"+"C:\\Users\\"+currentUser+"\\Desktop\\Test_Ouput.xls");
			
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}
}
