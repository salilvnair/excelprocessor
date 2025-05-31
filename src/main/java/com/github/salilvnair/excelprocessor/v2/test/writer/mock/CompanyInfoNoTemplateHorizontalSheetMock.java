package com.github.salilvnair.excelprocessor.v2.test.writer.mock;

import com.github.salilvnair.excelprocessor.v2.model.CellInfo;
import com.github.salilvnair.excelprocessor.v2.test.sheet.horizontal.notemplate.CompanyInfoHorizontalDynamicSheet;
import com.github.salilvnair.excelprocessor.v2.test.sheet.horizontal.notemplate.CompanyInfoHorizontalSheet;

import java.util.LinkedHashMap;
import java.util.List;

public class CompanyInfoNoTemplateHorizontalSheetMock {
   public static List<CompanyInfoHorizontalSheet> generateHorizontalSheets() {
       return companyInfoHorizontalSheets(CompanyInfoMock.companyInfos());
   }

   public static List<CompanyInfoHorizontalDynamicSheet> generateHorizontalDynamicSheets() {
       return companyInfoDynamicHorizontalSheets(CompanyInfoMock.companyInfos());
   }

    private static List<CompanyInfoHorizontalDynamicSheet> companyInfoDynamicHorizontalSheets(List<CompanyInfoMock.CompanyInfo> companyInfos) {
       return companyInfos.stream()
               .map(CompanyInfoNoTemplateHorizontalSheetMock::generateHorizontalDynamicHeaders)
               .toList();
   }

    private static CompanyInfoHorizontalDynamicSheet generateHorizontalDynamicHeaders(CompanyInfoMock.CompanyInfo companyInfo) {
        CompanyInfoHorizontalDynamicSheet sheet = new CompanyInfoHorizontalDynamicSheet();
        LinkedHashMap<String, CellInfo> dynamicHeaderKeyedCellInfoMap = new LinkedHashMap<>();
        dynamicHeaderKeyedCellInfoMap.put("Name", CellInfo.builder().header("Name").value(companyInfo.getName()).build());
        dynamicHeaderKeyedCellInfoMap.put("Company Id", CellInfo.builder().header("Company Id").value(companyInfo.getCompanyId()).build());
        dynamicHeaderKeyedCellInfoMap.put("Front Desk Phone Number", CellInfo.builder().header("Front Desk Phone Number").value(companyInfo.getFrontDeskPhoneNumber()).build());
        dynamicHeaderKeyedCellInfoMap.put("Address", CellInfo.builder().header("Address").value(companyInfo.getAddress()).build());
        sheet.setDynamicHeaderKeyedCellInfoMap(dynamicHeaderKeyedCellInfoMap);
        return sheet;
    }

    private static List<CompanyInfoHorizontalSheet> companyInfoHorizontalSheets(List<CompanyInfoMock.CompanyInfo> companyInfos) {
        return companyInfos.stream()
                .map(CompanyInfoNoTemplateHorizontalSheetMock::createCompanyInfoHorizontalSheet)
                .toList();
    }


    private static CompanyInfoHorizontalSheet createCompanyInfoHorizontalSheet(CompanyInfoMock.CompanyInfo companyInfo) {
        CompanyInfoHorizontalSheet sheet = new CompanyInfoHorizontalSheet();
        sheet.setName(companyInfo.getName());
        sheet.setCompanyId(companyInfo.getCompanyId());
        sheet.setFrontDeskPhoneNumber(companyInfo.getFrontDeskPhoneNumber());
        sheet.setAddress(companyInfo.getAddress());
        return sheet;
    }
}
