package com.github.salilvnair.excelprocessor.v2.test.writer.mock;

import com.github.salilvnair.excelprocessor.v2.model.CellInfo;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;
import com.github.salilvnair.excelprocessor.v2.test.sheet.multioriented.notemplate.CompanyInfoMultiOrientedRandomPositionVerticalDynamicSheet;
import com.github.salilvnair.excelprocessor.v2.test.sheet.multioriented.notemplate.CompanyInfoMultiOrientedRandomPositionVerticalSheet;
import com.github.salilvnair.excelprocessor.v2.test.sheet.multioriented.notemplate.UserInfoMultiOrientedRandomPositionVerticalDynamicSheet;
import com.github.salilvnair.excelprocessor.v2.test.sheet.multioriented.notemplate.UserInfoMultiOrientedRandomPositionVerticalSheet;

import java.util.LinkedHashMap;
import java.util.List;

public class CompanyInfoAndUserInfoMultiOrientedNoTemplateRandomPositionVerticalSheetMock {

    public static List<List<? extends BaseSheet>> generateMultiOrientedVerticalSheets() {
        return List.of(
                generateMultiOrientedCompanyInfoVerticalSheets(),
                generateMultiOrientedUserInfoVerticalSheets()
        );
    }

    public static List<List<? extends BaseSheet>> generateMultiOrientedVerticalDynamicSheets() {
        return List.of(
                generateMultiOrientedCompanyInfoVerticalDynamicSheets(),
                generateMultiOrientedUserInfoVerticalDynamicSheets()
        );
    }

    public static List<CompanyInfoMultiOrientedRandomPositionVerticalSheet> generateMultiOrientedCompanyInfoVerticalSheets() {
        return CompanyInfoMultiOrientedRandomPositionVerticalSheets(CompanyInfoMock.companyInfos().subList(0,1));
    }

    public static List<CompanyInfoMultiOrientedRandomPositionVerticalDynamicSheet> generateMultiOrientedCompanyInfoVerticalDynamicSheets() {
        return companyInfoMultiOrientedDynamicVerticalSheets(CompanyInfoMock.companyInfos().subList(0,1));
    }

    private static List<CompanyInfoMultiOrientedRandomPositionVerticalDynamicSheet> companyInfoMultiOrientedDynamicVerticalSheets(List<CompanyInfoMock.CompanyInfo> companyInfos) {
        return companyInfos.stream()
                .map(CompanyInfoAndUserInfoMultiOrientedNoTemplateRandomPositionVerticalSheetMock::generateVerticalDynamicHeaders)
                .toList();
    }

    private static CompanyInfoMultiOrientedRandomPositionVerticalDynamicSheet generateVerticalDynamicHeaders(CompanyInfoMock.CompanyInfo companyInfo) {
        CompanyInfoMultiOrientedRandomPositionVerticalDynamicSheet sheet = new CompanyInfoMultiOrientedRandomPositionVerticalDynamicSheet();
        LinkedHashMap<String, CellInfo> dynamicHeaderKeyedCellInfoMap = new LinkedHashMap<>();
        dynamicHeaderKeyedCellInfoMap.put("Name", CellInfo.builder().header("Name").value(companyInfo.getName()).build());
        dynamicHeaderKeyedCellInfoMap.put("Company Id", CellInfo.builder().header("Company Id").value(companyInfo.getCompanyId()).build());
        dynamicHeaderKeyedCellInfoMap.put("Front Desk Phone Number", CellInfo.builder().header("Front Desk Phone Number").value(companyInfo.getFrontDeskPhoneNumber()).build());
        dynamicHeaderKeyedCellInfoMap.put("Address", CellInfo.builder().header("Address").value(companyInfo.getAddress()).build());
        sheet.setDynamicHeaderKeyedCellInfoMap(dynamicHeaderKeyedCellInfoMap);
        return sheet;
    }

    private static List<CompanyInfoMultiOrientedRandomPositionVerticalSheet> CompanyInfoMultiOrientedRandomPositionVerticalSheets(List<CompanyInfoMock.CompanyInfo> companyInfos) {
        return companyInfos.stream()
                .map(CompanyInfoAndUserInfoMultiOrientedNoTemplateRandomPositionVerticalSheetMock::createCompanyInfoMultiOrientedRandomPositionVerticalSheet)
                .toList();
    }


    private static CompanyInfoMultiOrientedRandomPositionVerticalSheet createCompanyInfoMultiOrientedRandomPositionVerticalSheet(CompanyInfoMock.CompanyInfo companyInfo) {
        CompanyInfoMultiOrientedRandomPositionVerticalSheet sheet = new CompanyInfoMultiOrientedRandomPositionVerticalSheet();
        sheet.setName(companyInfo.getName());
        sheet.setCompanyId(companyInfo.getCompanyId());
        sheet.setFrontDeskPhoneNumber(companyInfo.getFrontDeskPhoneNumber());
        sheet.setAddress(companyInfo.getAddress());
        return sheet;
    }

   public static List<UserInfoMultiOrientedRandomPositionVerticalSheet> generateMultiOrientedUserInfoVerticalSheets() {
       return UserInfoMultiOrientedRandomPositionVerticalSheets(UserInfoMock.userInfos());
   }

   public static List<UserInfoMultiOrientedRandomPositionVerticalDynamicSheet> generateMultiOrientedUserInfoVerticalDynamicSheets() {
       return userInfoMultiOrientedDynamicVerticalSheets(UserInfoMock.userInfos());
   }

    private static List<UserInfoMultiOrientedRandomPositionVerticalDynamicSheet> userInfoMultiOrientedDynamicVerticalSheets(List<UserInfoMock.UserInfo> userInfos) {
       return userInfos.stream()
               .map(CompanyInfoAndUserInfoMultiOrientedNoTemplateRandomPositionVerticalSheetMock::generateVerticalDynamicHeaders)
               .toList();
   }

    private static UserInfoMultiOrientedRandomPositionVerticalDynamicSheet generateVerticalDynamicHeaders(UserInfoMock.UserInfo userInfo) {
        UserInfoMultiOrientedRandomPositionVerticalDynamicSheet sheet = new UserInfoMultiOrientedRandomPositionVerticalDynamicSheet();
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

    private static List<UserInfoMultiOrientedRandomPositionVerticalSheet> UserInfoMultiOrientedRandomPositionVerticalSheets(List<UserInfoMock.UserInfo> userInfos) {
        return userInfos.stream()
                .map(CompanyInfoAndUserInfoMultiOrientedNoTemplateRandomPositionVerticalSheetMock::createUserInfoMultiOrientedRandomPositionVerticalSheet)
                .toList();
    }


    private static UserInfoMultiOrientedRandomPositionVerticalSheet createUserInfoMultiOrientedRandomPositionVerticalSheet(UserInfoMock.UserInfo userInfo) {
        UserInfoMultiOrientedRandomPositionVerticalSheet sheet = new UserInfoMultiOrientedRandomPositionVerticalSheet();
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
