package com.github.salilvnair.excelprocessor.v2.generator.service;

import com.github.salilvnair.excelprocessor.util.AnnotationUtil;
import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.context.ExcelSheetContext;
import com.github.salilvnair.excelprocessor.v2.generator.sheet.DynamicClassGeneratorSheet;
import com.github.salilvnair.excelprocessor.v2.processor.factory.ExcelSheetReaderFactory;
import com.github.salilvnair.excelprocessor.v2.processor.helper.ExcelSheetReaderUtil;
import com.github.salilvnair.excelprocessor.v2.service.ExcelSheetReader;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;
import com.github.salilvnair.excelprocessor.v2.sheet.DynamicHeaderSheet;
import com.github.salilvnair.excelprocessor.v2.model.CellInfo;
import com.github.salilvnair.excelprocessor.v2.model.SheetInfo;

import java.util.*;
import java.util.stream.Collectors;


/**
 * @author Salil V Nair
 */
public class ExcelSheetClassGenerator {

    public static Map<String, String> generate(ExcelSheetContext sheetContext, List<SheetInfo> sheetInfos) throws Exception {
        Map<String, String> sheetClassStringMap = new LinkedHashMap<>();
        for (SheetInfo sheetInfo : sheetInfos) {
            String classTemplate = generate(sheetContext, sheetInfo);
            sheetClassStringMap.put(sheetInfo.name(), classTemplate);
        }
        return sheetClassStringMap;
    }

    public static String generate(ExcelSheetContext sheetContext, SheetInfo sheetInfo) throws Exception {
        ExcelSheetReader reader = ExcelSheetReaderFactory.generate(false);
        DynamicHeaderSheet dynamicHeaderSheet = new DynamicClassGeneratorSheet();
        Sheet sheet = dynamicHeaderSheet.getClass().getAnnotation(Sheet.class);
        if(sheetInfo.vertical()) {
            AnnotationUtil.changeValue(sheet, "vertical", true);
        }
        if(sheetInfo.sectional()) {
            AnnotationUtil.changeValue(sheet, "sectional", true);
        }
        AnnotationUtil.changeValue(sheet, "value", sheetInfo.name());
        AnnotationUtil.changeValue(sheet, "headerRowAt", sheetInfo.headerRowAt());
        AnnotationUtil.changeValue(sheet, "headerRowEndsAt", sheetInfo.headerRowEndsAt());
        AnnotationUtil.changeValue(sheet, "headerColumnAt", sheetInfo.headerColumnAt());
        AnnotationUtil.changeValue(sheet, "headerColumnEndsAt", sheetInfo.headerColumnEndsAt());
        AnnotationUtil.changeValue(sheet, "ignoreHeaderPatterns", sheetInfo.ignoreHeaderPatterns());
        List<? extends BaseSheet> readList = reader.read(dynamicHeaderSheet.getClass(), sheetContext);
        return classTemplate(sheetInfo, sheet.value(), sheet.vertical(), readList.get(0).sheetHeaders(), readList.get(0).cells(), sheet.headerRowAt(), sheet.headerColumnAt());
    }

    public static Set<String> findDuplicates(List<String> list) {
        Set<String> items = new HashSet<>();
        return list.stream()
                .filter(n -> !items.add(n)) // Set.add() returns false if the element was already in the set.
                .collect(Collectors.toSet());

    }


    private static String classTemplate(SheetInfo sheetInfo, String sheetName, boolean vertical, List<String> sheetHeaders, Map<String, CellInfo> headerKeyedCellInfoMap, int headerRowNumber, String headerColumn) {
        StringBuilder sb  = new StringBuilder();
        boolean containsDuplicateHeaders = containsDuplicateHeaders(headerKeyedCellInfoMap);
        Set<String> allDuplicateHeaders = findDuplicates(sheetHeaders);
        String className = ExcelSheetReaderUtil.toCamelCase(sheetName);
        className = ExcelSheetReaderUtil.deleteJavaInValidVariables(className);
        String hasDuplicateHeaderString = "";
        if(containsDuplicateHeaders) {
            hasDuplicateHeaderString=", duplicateHeaders=true";
        }
        if(vertical) {
            sb.append("@Sheet(value=\"").append(sheetName).append("\"").append(", vertical=true").append(hasDuplicateHeaderString).append(", headerRowAt=").append(headerRowNumber).append(", headerColumnAt=\"").append(headerColumn).append("\")\n");
        }
        else {
            sb.append("@Sheet(value=\"").append(sheetName).append("\"").append(hasDuplicateHeaderString).append(", headerRowAt=").append(headerRowNumber).append(", headerColumnAt=\"").append(headerColumn).append("\")\n");
        }
        String parentBaseSheet = "BaseSheet";
        sb.append("public class ").append(className).append("Sheet extends ").append(parentBaseSheet).append(" {\n");
        for(String header: headerKeyedCellInfoMap.keySet()) {
            CellInfo cellInfo = headerKeyedCellInfoMap.get(header);
            String originalHeader = cellInfo.originalHeader();
            String typeString = cellInfo.cellTypeString();
            typeString = typeString == null ? CellInfo.CELL_TYPE_STRING : sheetInfo.allCellTypeToString() ? CellInfo.CELL_TYPE_STRING : typeString;
            if(allDuplicateHeaders.contains(cellInfo.originalHeader())) {
                if(vertical) {
                    sb.append("    @Cell(value=\"").append(originalHeader).append("\", row=").append(cellInfo.rowIndex() + 1).append(")\n");
                }
                else {
                    sb.append("    @Cell(value=\"").append(originalHeader).append("\", column=\"").append(ExcelSheetReader.toIndentName(cellInfo.columnIndex() + 1)).append("\")\n");
                }
            }
            else {
                sb.append("    @Cell(\"").append(originalHeader).append("\")\n");
            }
            String field = originalHeader;
            field = ExcelSheetReaderUtil.cleanHeaderString(field);
            field = constructValidJavaVariableNameFromHeader(field);
            if(sheetInfo.useOriginalHeader()) {
                field = constructValidJavaVariableNameFromHeader(originalHeader);
            }
            sb.append("    private ").append(typeString).append(" ").append(field).append(";");
            sb.append("\n");
        }
        if(sheetInfo.skipGettersAndSetters()) {
            sb.append("}\n");
            return sb.toString();
        }
        sb.append("\n");
        sb.append("  //getters and setters");
        sb.append("\n");
        for(String header: headerKeyedCellInfoMap.keySet()) {
            CellInfo cellInfo = headerKeyedCellInfoMap.get(header);
            String originalHeader = cellInfo.originalHeader();
            String typeString = cellInfo.cellTypeString();
            typeString = typeString == null ? CellInfo.CELL_TYPE_STRING : sheetInfo.allCellTypeToString() ? CellInfo.CELL_TYPE_STRING : typeString;
            String field = originalHeader;
            field = ExcelSheetReaderUtil.cleanHeaderString(field);
            field = constructValidJavaVariableNameFromHeader(field);
            if(sheetInfo.useOriginalHeader()) {
                field = constructValidJavaVariableNameFromHeader(originalHeader);
            }
            String upperCaseFieldString = field.substring(0,1).toUpperCase()+field.substring(1);
            sb.append("    public ").append(typeString).append(" get").append(upperCaseFieldString).append("() {");
            sb.append("\n       return this.").append(field).append(";");
            sb.append("\n    }\n");
            sb.append("    public void set").append(upperCaseFieldString).append("(").append(typeString).append(" ").append(field).append(") {");
            sb.append("\n        this.").append(field).append(" = ").append(field).append(";");
            sb.append("\n    }\n");
        }
        sb.append("}\n");
        return sb.toString();
    }

    private static boolean containsDuplicateHeader(CellInfo cellInfo) {
        return !cellInfo.originalHeader().equals(cellInfo.getHeader());
    }

    private static boolean containsDuplicateHeaders(Map<String, CellInfo> headerKeyedCellInfoMap) {
        List<String> headers = headerKeyedCellInfoMap.values().stream().map(CellInfo::originalHeader).collect(Collectors.toList());
        Set<String> uniqueHeaders = new HashSet<>(headers);
        return headers.size() != uniqueHeaders.size();
    }

    private static String constructValidJavaVariableNameFromHeader(String header) {
        String pascalCase = ExcelSheetReaderUtil.toPascalCase(header);
        return ExcelSheetReaderUtil.deleteJavaInValidVariables(pascalCase);
    }


}
