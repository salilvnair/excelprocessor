package com.github.salilvnair.excelprocessor.v2.test.writer.mock;

import com.github.salilvnair.excelprocessor.v2.model.CellInfo;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;
import com.github.salilvnair.excelprocessor.v2.test.sheet.multioriented.notemplate.CompanyInfoMultiOrientedRandomPositionHorizontalDynamicSheet;
import com.github.salilvnair.excelprocessor.v2.test.sheet.multioriented.notemplate.CompanyInfoMultiOrientedRandomPositionHorizontalSheet;
import com.github.salilvnair.excelprocessor.v2.test.sheet.multioriented.notemplate.UserInfoMultiOrientedRandomPositionHorizontalDynamicSheet;
import com.github.salilvnair.excelprocessor.v2.test.sheet.multioriented.notemplate.UserInfoMultiOrientedRandomPositionHorizontalSheet;

import java.util.LinkedHashMap;
import java.util.List;

public class CompanyInfoAndUserInfoMultiOrientedNoTemplateRandomPositionHorizontalSheetMock {

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

    public static List<CompanyInfoMultiOrientedRandomPositionHorizontalSheet> generateMultiOrientedCompanyInfoHorizontalSheets() {
        return CompanyInfoMultiOrientedRandomPositionHorizontalSheets(CompanyInfoMock.companyInfos().subList(0, 1));
    }

    public static List<CompanyInfoMultiOrientedRandomPositionHorizontalDynamicSheet> generateMultiOrientedCompanyInfoHorizontalDynamicSheets() {
        return companyInfoMultiOrientedDynamicHorizontalSheets(CompanyInfoMock.companyInfos().subList(0, 1));
    }

    private static List<CompanyInfoMultiOrientedRandomPositionHorizontalDynamicSheet> companyInfoMultiOrientedDynamicHorizontalSheets(List<CompanyInfoMock.CompanyInfo> companyInfos) {
        return companyInfos.stream()
                .map(CompanyInfoAndUserInfoMultiOrientedNoTemplateRandomPositionHorizontalSheetMock::generateHorizontalDynamicHeaders)
                .toList();
    }

    private static CompanyInfoMultiOrientedRandomPositionHorizontalDynamicSheet generateHorizontalDynamicHeaders(CompanyInfoMock.CompanyInfo companyInfo) {
        CompanyInfoMultiOrientedRandomPositionHorizontalDynamicSheet sheet = new CompanyInfoMultiOrientedRandomPositionHorizontalDynamicSheet();
        LinkedHashMap<String, CellInfo> dynamicHeaderKeyedCellInfoMap = new LinkedHashMap<>();
        dynamicHeaderKeyedCellInfoMap.put("Name", CellInfo.builder().header("Name").value(companyInfo.getName()).build());
        dynamicHeaderKeyedCellInfoMap.put("Company Id", CellInfo.builder().header("Company Id").value(companyInfo.getCompanyId()).build());
        dynamicHeaderKeyedCellInfoMap.put("Front Desk Phone Number", CellInfo.builder().header("Front Desk Phone Number").value(companyInfo.getFrontDeskPhoneNumber()).build());
        dynamicHeaderKeyedCellInfoMap.put("Address", CellInfo.builder().header("Address").value(companyInfo.getAddress()).build());
        sheet.setDynamicHeaderKeyedCellInfoMap(dynamicHeaderKeyedCellInfoMap);
        return sheet;
    }

    private static List<CompanyInfoMultiOrientedRandomPositionHorizontalSheet> CompanyInfoMultiOrientedRandomPositionHorizontalSheets(List<CompanyInfoMock.CompanyInfo> companyInfos) {
        return companyInfos.stream()
                .map(CompanyInfoAndUserInfoMultiOrientedNoTemplateRandomPositionHorizontalSheetMock::createCompanyInfoMultiOrientedRandomPositionHorizontalSheet)
                .toList();
    }


    private static CompanyInfoMultiOrientedRandomPositionHorizontalSheet createCompanyInfoMultiOrientedRandomPositionHorizontalSheet(CompanyInfoMock.CompanyInfo companyInfo) {
        CompanyInfoMultiOrientedRandomPositionHorizontalSheet sheet = new CompanyInfoMultiOrientedRandomPositionHorizontalSheet();
        sheet.setName(companyInfo.getName());
        sheet.setCompanyId(companyInfo.getCompanyId());
        sheet.setFrontDeskPhoneNumber(companyInfo.getFrontDeskPhoneNumber());
        sheet.setAddress(companyInfo.getAddress());
        return sheet;
    }

   public static List<UserInfoMultiOrientedRandomPositionHorizontalSheet> generateMultiOrientedUserInfoHorizontalSheets() {
       return UserInfoMultiOrientedRandomPositionHorizontalSheets(UserInfoMock.userInfos());
   }

   public static List<UserInfoMultiOrientedRandomPositionHorizontalDynamicSheet> generateMultiOrientedUserInfoHorizontalDynamicSheets() {
       return userInfoMultiOrientedDynamicHorizontalSheets(UserInfoMock.userInfos());
   }

    private static List<UserInfoMultiOrientedRandomPositionHorizontalDynamicSheet> userInfoMultiOrientedDynamicHorizontalSheets(List<UserInfoMock.UserInfo> userInfos) {
       return userInfos.stream()
               .map(CompanyInfoAndUserInfoMultiOrientedNoTemplateRandomPositionHorizontalSheetMock::generateHorizontalDynamicHeaders)
               .toList();
   }

    private static UserInfoMultiOrientedRandomPositionHorizontalDynamicSheet generateHorizontalDynamicHeaders(UserInfoMock.UserInfo userInfo) {
        UserInfoMultiOrientedRandomPositionHorizontalDynamicSheet sheet = new UserInfoMultiOrientedRandomPositionHorizontalDynamicSheet();
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

    private static List<UserInfoMultiOrientedRandomPositionHorizontalSheet> UserInfoMultiOrientedRandomPositionHorizontalSheets(List<UserInfoMock.UserInfo> userInfos) {
        return userInfos.stream()
                .map(CompanyInfoAndUserInfoMultiOrientedNoTemplateRandomPositionHorizontalSheetMock::createUserInfoMultiOrientedRandomPositionHorizontalSheet)
                .toList();
    }


    private static UserInfoMultiOrientedRandomPositionHorizontalSheet createUserInfoMultiOrientedRandomPositionHorizontalSheet(UserInfoMock.UserInfo userInfo) {
        UserInfoMultiOrientedRandomPositionHorizontalSheet sheet = new UserInfoMultiOrientedRandomPositionHorizontalSheet();
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
