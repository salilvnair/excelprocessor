package com.github.salilvnair.excelprocessor.v2.test.writer.mock;

import com.github.salilvnair.excelprocessor.v2.model.CellInfo;
import com.github.salilvnair.excelprocessor.v2.sheet.BaseSheet;
import com.github.salilvnair.excelprocessor.v2.test.sheet.multioriented.notemplate.CompanyInfoMultiOrientedVerticalDynamicSheet;
import com.github.salilvnair.excelprocessor.v2.test.sheet.multioriented.notemplate.CompanyInfoMultiOrientedVerticalSheet;
import com.github.salilvnair.excelprocessor.v2.test.sheet.multioriented.notemplate.UserInfoMultiOrientedVerticalDynamicSheet;
import com.github.salilvnair.excelprocessor.v2.test.sheet.multioriented.notemplate.UserInfoMultiOrientedVerticalSheet;

import java.util.LinkedHashMap;
import java.util.List;

public class CompanyInfoAndUserInfoMultiOrientedNoTemplateVerticalSheetMock {

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

    public static List<CompanyInfoMultiOrientedVerticalSheet> generateMultiOrientedCompanyInfoVerticalSheets() {
        return companyInfoMultiOrientedVerticalSheets(CompanyInfoMock.companyInfos().subList(0,1));
    }

    public static List<CompanyInfoMultiOrientedVerticalDynamicSheet> generateMultiOrientedCompanyInfoVerticalDynamicSheets() {
        return companyInfoMultiOrientedDynamicVerticalSheets(CompanyInfoMock.companyInfos().subList(0,1));
    }

    private static List<CompanyInfoMultiOrientedVerticalDynamicSheet> companyInfoMultiOrientedDynamicVerticalSheets(List<CompanyInfoMock.CompanyInfo> companyInfos) {
        return companyInfos.stream()
                .map(CompanyInfoAndUserInfoMultiOrientedNoTemplateVerticalSheetMock::generateVerticalDynamicHeaders)
                .toList();
    }

    private static CompanyInfoMultiOrientedVerticalDynamicSheet generateVerticalDynamicHeaders(CompanyInfoMock.CompanyInfo companyInfo) {
        CompanyInfoMultiOrientedVerticalDynamicSheet sheet = new CompanyInfoMultiOrientedVerticalDynamicSheet();
        LinkedHashMap<String, CellInfo> dynamicHeaderKeyedCellInfoMap = new LinkedHashMap<>();
        dynamicHeaderKeyedCellInfoMap.put("Name", CellInfo.builder().header("Name").value(companyInfo.getName()).build());
        dynamicHeaderKeyedCellInfoMap.put("Company Id", CellInfo.builder().header("Company Id").value(companyInfo.getCompanyId()).build());
        dynamicHeaderKeyedCellInfoMap.put("Front Desk Phone Number", CellInfo.builder().header("Front Desk Phone Number").value(companyInfo.getFrontDeskPhoneNumber()).build());
        dynamicHeaderKeyedCellInfoMap.put("Address", CellInfo.builder().header("Address").value(companyInfo.getAddress()).build());
        sheet.setDynamicHeaderKeyedCellInfoMap(dynamicHeaderKeyedCellInfoMap);
        return sheet;
    }

    private static List<CompanyInfoMultiOrientedVerticalSheet> companyInfoMultiOrientedVerticalSheets(List<CompanyInfoMock.CompanyInfo> companyInfos) {
        return companyInfos.stream()
                .map(CompanyInfoAndUserInfoMultiOrientedNoTemplateVerticalSheetMock::createCompanyInfoMultiOrientedVerticalSheet)
                .toList();
    }


    private static CompanyInfoMultiOrientedVerticalSheet createCompanyInfoMultiOrientedVerticalSheet(CompanyInfoMock.CompanyInfo companyInfo) {
        CompanyInfoMultiOrientedVerticalSheet sheet = new CompanyInfoMultiOrientedVerticalSheet();
        sheet.setName(companyInfo.getName());
        sheet.setCompanyId(companyInfo.getCompanyId());
        sheet.setFrontDeskPhoneNumber(companyInfo.getFrontDeskPhoneNumber());
        sheet.setAddress(companyInfo.getAddress());
        return sheet;
    }

   public static List<UserInfoMultiOrientedVerticalSheet> generateMultiOrientedUserInfoVerticalSheets() {
       return userInfoMultiOrientedVerticalSheets(UserInfoMock.userInfos());
   }

   public static List<UserInfoMultiOrientedVerticalDynamicSheet> generateMultiOrientedUserInfoVerticalDynamicSheets() {
       return userInfoMultiOrientedDynamicVerticalSheets(UserInfoMock.userInfos());
   }

    private static List<UserInfoMultiOrientedVerticalDynamicSheet> userInfoMultiOrientedDynamicVerticalSheets(List<UserInfoMock.UserInfo> userInfos) {
       return userInfos.stream()
               .map(CompanyInfoAndUserInfoMultiOrientedNoTemplateVerticalSheetMock::generateVerticalDynamicHeaders)
               .toList();
   }

    private static UserInfoMultiOrientedVerticalDynamicSheet generateVerticalDynamicHeaders(UserInfoMock.UserInfo userInfo) {
        UserInfoMultiOrientedVerticalDynamicSheet sheet = new UserInfoMultiOrientedVerticalDynamicSheet();
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

    private static List<UserInfoMultiOrientedVerticalSheet> userInfoMultiOrientedVerticalSheets(List<UserInfoMock.UserInfo> userInfos) {
        return userInfos.stream()
                .map(CompanyInfoAndUserInfoMultiOrientedNoTemplateVerticalSheetMock::createUserInfoMultiOrientedVerticalSheet)
                .toList();
    }


    private static UserInfoMultiOrientedVerticalSheet createUserInfoMultiOrientedVerticalSheet(UserInfoMock.UserInfo userInfo) {
        UserInfoMultiOrientedVerticalSheet sheet = new UserInfoMultiOrientedVerticalSheet();
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
