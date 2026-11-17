package com.github.salilvnair.excelprocessor.v2.test.writer.mock;

import com.github.salilvnair.excelprocessor.v2.model.CellInfo;
import com.github.salilvnair.excelprocessor.v2.test.sheet.vertical.withtemplate.CompanyInfoVerticalDynamicSheet;
import com.github.salilvnair.excelprocessor.v2.test.sheet.vertical.withtemplate.CompanyInfoVerticalSheet;

import java.util.LinkedHashMap;
import java.util.List;

public class CompanyInfoWithTemplateVerticalSheetMock {
    
    public static List<CompanyInfoVerticalSheet> generateVerticalSheets() {
        return companyInfoHorizontalSheets(CompanyInfoMock.companyInfos());
    }

    public static List<CompanyInfoVerticalDynamicSheet> generateVerticalDynamicSheets() {
        return companyInfoDynamicHorizontalSheets(CompanyInfoMock.companyInfos());
    }

    private static List<CompanyInfoVerticalDynamicSheet> companyInfoDynamicHorizontalSheets(List<CompanyInfoMock.CompanyInfo> companyInfos) {
       return companyInfos.stream()
               .map(CompanyInfoWithTemplateVerticalSheetMock::generateHorizontalDynamicHeaders)
               .toList();
   }

    private static CompanyInfoVerticalDynamicSheet generateHorizontalDynamicHeaders(CompanyInfoMock.CompanyInfo companyInfo) {
        CompanyInfoVerticalDynamicSheet sheet = new CompanyInfoVerticalDynamicSheet();
        LinkedHashMap<String, CellInfo> dynamicHeaderKeyedCellInfoMap = new LinkedHashMap<>();
        dynamicHeaderKeyedCellInfoMap.put("Name", CellInfo.builder().header("Name").value(companyInfo.getName()).build());
        dynamicHeaderKeyedCellInfoMap.put("Company Id", CellInfo.builder().header("Company Id").value(companyInfo.getCompanyId()).build());
        dynamicHeaderKeyedCellInfoMap.put("Front Desk Phone Number", CellInfo.builder().header("Front Desk Phone Number").value(companyInfo.getFrontDeskPhoneNumber()).build());
        dynamicHeaderKeyedCellInfoMap.put("Address", CellInfo.builder().header("Address").value(companyInfo.getAddress()).build());
        sheet.setDynamicHeaderKeyedCellInfoMap(dynamicHeaderKeyedCellInfoMap);
        return sheet;
    }

    private static List<CompanyInfoVerticalSheet> companyInfoHorizontalSheets(List<CompanyInfoMock.CompanyInfo> companyInfos) {
        return companyInfos.stream()
                .map(CompanyInfoWithTemplateVerticalSheetMock::createCompanyInfoHorizontalSheet)
                .toList();
    }


    private static CompanyInfoVerticalSheet createCompanyInfoHorizontalSheet(CompanyInfoMock.CompanyInfo companyInfo) {
        CompanyInfoVerticalSheet sheet = new CompanyInfoVerticalSheet();
        sheet.setName(companyInfo.getName());
        sheet.setCompanyId(companyInfo.getCompanyId());
        sheet.setFrontDeskPhoneNumber(companyInfo.getFrontDeskPhoneNumber());
        sheet.setAddress(companyInfo.getAddress());
        return sheet;
    }
}
