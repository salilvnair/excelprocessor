package com.github.salilvnair.excelprocessor.v2.generator.service;

import java.util.*;
import java.util.stream.Collectors;
import com.github.salilvnair.excelprocessor.util.AnnotationUtil;
import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.context.ExcelSheetContext;
import com.github.salilvnair.excelprocessor.v2.generator.sheet.DynamicClassGeneratorSheet;
import com.github.salilvnair.excelprocessor.v2.processor.factory.ExcelSheetReaderFactory;
import com.github.salilvnair.excelprocessor.v2.processor.helper.ExcelSheetReaderUtil;
import com.github.salilvnair.excelprocessor.v2.service.ExcelSheetReader;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;
import com.github.salilvnair.excelprocessor.v2.sheet.DynamicHeaderSheet;
import com.github.salilvnair.excelprocessor.v2.type.CellInfo;
import com.github.salilvnair.excelprocessor.v2.type.SheetInfo;


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
        AnnotationUtil.changeValue(sheet, "value", sheetInfo.name());
        AnnotationUtil.changeValue(sheet, "headerRowAt", sheetInfo.headerRowAt());
        AnnotationUtil.changeValue(sheet, "headerColumnAt", sheetInfo.headerColumnAt());
        AnnotationUtil.changeValue(sheet, "ignoreHeaderPatterns", sheetInfo.ignoreHeaderPatterns());
        List<? extends BaseSheet> readList = reader.read(dynamicHeaderSheet.getClass(), sheetContext);
        return classTemplate(sheet.value(), sheet.vertical(), readList.get(0).sheetHeaders(), readList.get(0).cells(), sheet.headerRowAt(), sheet.headerColumnAt());
    }

    public static Set<String> findDuplicates(List<String> list) {
        Set<String> items = new HashSet<>();
        return list.stream()
                .filter(n -> !items.add(n)) // Set.add() returns false if the element was already in the set.
                .collect(Collectors.toSet());

    }


    private static String classTemplate(String sheetName, boolean isPivotEnabled, List<String> sheetHeaders, Map<String, CellInfo> headerKeyCellInfoMap, int headerRowNumber, String headerColumn) {
        StringBuilder sb  = new StringBuilder("");
        List<String> nonEmptyHeaders = new ArrayList<>(headerKeyCellInfoMap.keySet());
        Set<String> duplicateHeaders = findDuplicates(sheetHeaders);
        Map<String, String> javaFieldNameKeyedSheetHeaderMap = new HashMap<>();
        List<String> javaFieldNames = new ArrayList<>();
        nonEmptyHeaders.forEach(header -> {
            header = ExcelSheetReaderUtil.cleanHeaderString(header);
            String pascalCase = ExcelSheetReaderUtil.toPascalCase(header);
            String javaVar = ExcelSheetReaderUtil.deleteJavaInValidVariables(pascalCase);
            javaFieldNames.add(javaVar);
            javaFieldNameKeyedSheetHeaderMap.put(javaVar, header);
        });
        String className = ExcelSheetReaderUtil.toCamelCase(sheetName);
        className = ExcelSheetReaderUtil.deleteJavaInValidVariables(className);
        String hasDuplicateHeaderString = "";
        if(!duplicateHeaders.isEmpty()) {
            hasDuplicateHeaderString=", duplicateHeaders=true";
        }
        if(isPivotEnabled) {
            sb.append("@Sheet(value=\"").append(sheetName).append("\"").append(", vertical=true").append(hasDuplicateHeaderString).append(", headerRowAt=").append(headerRowNumber).append(", headerColumnAt=\"").append(headerColumn).append("\")\n");
        }
        else {
            sb.append("@Sheet(value=\"").append(sheetName).append("\"").append(hasDuplicateHeaderString).append(", headerRowAt=").append(headerRowNumber).append(", headerColumnAt=\"").append(headerColumn).append("\")\n");
        }
        String parentBaseSheet = "BaseSheet";
        sb.append("public class ").append(className).append("Sheet extends ").append(parentBaseSheet).append("{\n");
        for(String field:javaFieldNames) {
            String sheetHeaderKey = javaFieldNameKeyedSheetHeaderMap.get(field);
            CellInfo cellInfo = headerKeyCellInfoMap.get(sheetHeaderKey);
            String typeString = cellInfo.cellTypeString();
            if(duplicateHeaderContainsHeaderKey(duplicateHeaders, sheetHeaderKey)) {
                String headerKey = duplicateHeaderKey(duplicateHeaders, sheetHeaderKey);
                if(isPivotEnabled) {
                    sb.append("    @Cell(value=\"").append(headerKey).append("\", row=").append(cellInfo.rowIndex() + 1).append(")\n");
                }
                else {
                    sb.append("    @Cell(value=\"").append(typeString).append("\", column=\"").append(ExcelSheetReader.toIndentName(cellInfo.columnIndex() + 1)).append("\")\n");
                }
            }
            else {
                sb.append("    @Cell(\"").append(sheetHeaderKey).append("\")\n");
            }
            sb.append("    private ").append(typeString).append(" ").append(field).append(";");
            sb.append("\n");
        }
        sb.append("\n");
        sb.append("  //getters and setters");
        sb.append("\n");
        for(String field:javaFieldNames) {
            String upperCaseFieldString = field.substring(0,1).toUpperCase()+field.substring(1);
            String sheetHeaderKey = javaFieldNameKeyedSheetHeaderMap.get(field);
            CellInfo cellInfo = headerKeyCellInfoMap.get(sheetHeaderKey);
            String typeString = cellInfo.cellTypeString();
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

    private static boolean duplicateHeaderContainsHeaderKey(Set<String> duplicateHeaders, String sheetHeaderKey) {
        return duplicateHeaders.stream().anyMatch(sheetHeaderKey::contains);
    }

    private static String duplicateHeaderKey(Set<String> duplicateHeaders, String sheetHeaderKey) {
        return duplicateHeaders.stream().filter(sheetHeaderKey::contains).findAny().orElse(null);
    }

}
