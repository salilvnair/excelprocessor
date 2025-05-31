package com.github.salilvnair.excelprocessor.v2.test.writer.mock;

import com.github.salilvnair.excelprocessor.v2.model.CellInfo;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;
import com.github.salilvnair.excelprocessor.v2.test.sheet.multioriented.notemplate.CompanyInfoMultiOrientedHorizontalDynamicSheet;
import com.github.salilvnair.excelprocessor.v2.test.sheet.multioriented.notemplate.CompanyInfoMultiOrientedHorizontalSheet;
import com.github.salilvnair.excelprocessor.v2.test.sheet.multioriented.notemplate.UserInfoMultiOrientedHorizontalDynamicSheet;
import com.github.salilvnair.excelprocessor.v2.test.sheet.multioriented.notemplate.UserInfoMultiOrientedHorizontalSheet;

import java.util.LinkedHashMap;
import java.util.List;

public class CompanyInfoAndUserInfoMultiOrientedNoTemplateHorizontalSheetMock {

    public static List<List<? extends BaseSheet>> generateMultiOrientedHorizontalSheets() {
        return List.of(
                generateMultiOrientedCompanyInfoHorizontalSheets(),
                generateMultiOrientedUserInfoHorizontalSheets()
        );
    }

    public static List<List<? extends BaseSheet>> generateMultiOrientedHorizontalDynamicSheets() {
        return List.of(
                generateMultiOrientedCompanyInfoHorizontalDynamicSheets(),
                generateMultiOrientedUserInfoHorizontalDynamicSheets()
        );
    }

    public static List<CompanyInfoMultiOrientedHorizontalSheet> generateMultiOrientedCompanyInfoHorizontalSheets() {
        return companyInfoMultiOrientedHorizontalSheets(CompanyInfoMock.companyInfos().subList(0, 1));
    }

    public static List<CompanyInfoMultiOrientedHorizontalDynamicSheet> generateMultiOrientedCompanyInfoHorizontalDynamicSheets() {
        return companyInfoMultiOrientedDynamicHorizontalSheets(CompanyInfoMock.companyInfos().subList(0, 1));
    }

    private static List<CompanyInfoMultiOrientedHorizontalDynamicSheet> companyInfoMultiOrientedDynamicHorizontalSheets(List<CompanyInfoMock.CompanyInfo> companyInfos) {
        return companyInfos.stream()
                .map(CompanyInfoAndUserInfoMultiOrientedNoTemplateHorizontalSheetMock::generateHorizontalDynamicHeaders)
                .toList();
    }

    private static CompanyInfoMultiOrientedHorizontalDynamicSheet generateHorizontalDynamicHeaders(CompanyInfoMock.CompanyInfo companyInfo) {
        CompanyInfoMultiOrientedHorizontalDynamicSheet sheet = new CompanyInfoMultiOrientedHorizontalDynamicSheet();
        LinkedHashMap<String, CellInfo> dynamicHeaderKeyedCellInfoMap = new LinkedHashMap<>();
        dynamicHeaderKeyedCellInfoMap.put("Name", CellInfo.builder().header("Name").value(companyInfo.getName()).build());
        dynamicHeaderKeyedCellInfoMap.put("Company Id", CellInfo.builder().header("Company Id").value(companyInfo.getCompanyId()).build());
        dynamicHeaderKeyedCellInfoMap.put("Front Desk Phone Number", CellInfo.builder().header("Front Desk Phone Number").value(companyInfo.getFrontDeskPhoneNumber()).build());
        dynamicHeaderKeyedCellInfoMap.put("Address", CellInfo.builder().header("Address").value(companyInfo.getAddress()).build());
        sheet.setDynamicHeaderKeyedCellInfoMap(dynamicHeaderKeyedCellInfoMap);
        return sheet;
    }

    private static List<CompanyInfoMultiOrientedHorizontalSheet> companyInfoMultiOrientedHorizontalSheets(List<CompanyInfoMock.CompanyInfo> companyInfos) {
        return companyInfos.stream()
                .map(CompanyInfoAndUserInfoMultiOrientedNoTemplateHorizontalSheetMock::createCompanyInfoMultiOrientedHorizontalSheet)
                .toList();
    }


    private static CompanyInfoMultiOrientedHorizontalSheet createCompanyInfoMultiOrientedHorizontalSheet(CompanyInfoMock.CompanyInfo companyInfo) {
        CompanyInfoMultiOrientedHorizontalSheet sheet = new CompanyInfoMultiOrientedHorizontalSheet();
        sheet.setName(companyInfo.getName());
        sheet.setCompanyId(companyInfo.getCompanyId());
        sheet.setFrontDeskPhoneNumber(companyInfo.getFrontDeskPhoneNumber());
        sheet.setAddress(companyInfo.getAddress());
        return sheet;
    }

   public static List<UserInfoMultiOrientedHorizontalSheet> generateMultiOrientedUserInfoHorizontalSheets() {
       return userInfoMultiOrientedHorizontalSheets(UserInfoMock.userInfos());
   }

   public static List<UserInfoMultiOrientedHorizontalDynamicSheet> generateMultiOrientedUserInfoHorizontalDynamicSheets() {
       return userInfoMultiOrientedDynamicHorizontalSheets(UserInfoMock.userInfos());
   }

    private static List<UserInfoMultiOrientedHorizontalDynamicSheet> userInfoMultiOrientedDynamicHorizontalSheets(List<UserInfoMock.UserInfo> userInfos) {
       return userInfos.stream()
               .map(CompanyInfoAndUserInfoMultiOrientedNoTemplateHorizontalSheetMock::generateHorizontalDynamicHeaders)
               .toList();
   }

    private static UserInfoMultiOrientedHorizontalDynamicSheet generateHorizontalDynamicHeaders(UserInfoMock.UserInfo userInfo) {
        UserInfoMultiOrientedHorizontalDynamicSheet sheet = new UserInfoMultiOrientedHorizontalDynamicSheet();
        LinkedHashMap<String, CellInfo> dynamicHeaderKeyedCellInfoMap = new LinkedHashMap<>();
        dynamicHeaderKeyedCellInfoMap.put("First Name", CellInfo.builder().header("First Name").value(userInfo.getFirstName()).build());
        dynamicHeaderKeyedCellInfoMap.put("Last Name", CellInfo.builder().header("Last Name").value(userInfo.getLastName()).build());
        dynamicHeaderKeyedCellInfoMap.put("Email", CellInfo.builder().header("Email").value(userInfo.getEmail()).build());
        dynamicHeaderKeyedCellInfoMap.put("Phone Number", CellInfo.builder().header("Phone Number").value(userInfo.getPhoneNumber()).build());
        dynamicHeaderKeyedCellInfoMap.put("Address", CellInfo.builder().header("Address").value(userInfo.getAddress()).build());
        dynamicHeaderKeyedCellInfoMap.put("City", CellInfo.builder().header("City").value(userInfo.getCity()).build());
        dynamicHeaderKeyedCellInfoMap.put("State", CellInfo.builder().header("State").value(userInfo.getState()).build());
        dynamicHeaderKeyedCellInfoMap.put("Zip Code", CellInfo.builder().header("Zip Code").value(userInfo.getZipCode()).build());
        dynamicHeaderKeyedCellInfoMap.put("Country", CellInfo.builder().header("Country").value(userInfo.getCountry()).build());
        dynamicHeaderKeyedCellInfoMap.put("Date of Birth", CellInfo.builder().header("Date of Birth").value(userInfo.getDateOfBirth()).build());
        sheet.setDynamicHeaderKeyedCellInfoMap(dynamicHeaderKeyedCellInfoMap);
        return sheet;
    }

    private static List<UserInfoMultiOrientedHorizontalSheet> userInfoMultiOrientedHorizontalSheets(List<UserInfoMock.UserInfo> userInfos) {
        return userInfos.stream()
                .map(CompanyInfoAndUserInfoMultiOrientedNoTemplateHorizontalSheetMock::createUserInfoMultiOrientedHorizontalSheet)
                .toList();
    }


    private static UserInfoMultiOrientedHorizontalSheet createUserInfoMultiOrientedHorizontalSheet(UserInfoMock.UserInfo userInfo) {
        UserInfoMultiOrientedHorizontalSheet sheet = new UserInfoMultiOrientedHorizontalSheet();
        sheet.setFirstName(userInfo.getFirstName());
        sheet.setLastName(userInfo.getLastName());
        sheet.setEmail(userInfo.getEmail());
        sheet.setPhoneNumber(userInfo.getPhoneNumber());
        sheet.setAddress(userInfo.getAddress());
        sheet.setCity(userInfo.getCity());
        sheet.setState(userInfo.getState());
        sheet.setZipCode(userInfo.getZipCode());
        sheet.setCountry(userInfo.getCountry());
        sheet.setDateOfBirth(userInfo.getDateOfBirth());
        return sheet;
    }
}
