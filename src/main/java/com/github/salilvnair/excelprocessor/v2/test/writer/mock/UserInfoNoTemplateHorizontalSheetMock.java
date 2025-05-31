package com.github.salilvnair.excelprocessor.v2.test.writer.mock;

import com.github.salilvnair.excelprocessor.util.DateParsingUtil;
import com.github.salilvnair.excelprocessor.v2.model.CellInfo;
import com.github.salilvnair.excelprocessor.v2.model.DataCellStyleInfo;
import com.github.salilvnair.excelprocessor.v2.model.NumberStyleInfo;
import com.github.salilvnair.excelprocessor.v2.test.sheet.horizontal.notemplate.CompanyInfoHorizontalSheet;
import com.github.salilvnair.excelprocessor.v2.test.sheet.horizontal.notemplate.UserInfoHorizontalDynamicSheet;
import com.github.salilvnair.excelprocessor.v2.test.sheet.horizontal.notemplate.UserInfoHorizontalSheet;
import com.github.salilvnair.excelprocessor.v2.type.DateFormatPattern;
import com.github.salilvnair.excelprocessor.v2.type.NumberPrecisionFormat;

import java.util.LinkedHashMap;
import java.util.List;

public class UserInfoNoTemplateHorizontalSheetMock {
    public static List<UserInfoHorizontalSheet> generateHorizontalSheets() {
        return userInfoHorizontalSheets(UserInfoMock.userInfos());
    }

    public static List<UserInfoHorizontalDynamicSheet> generateHorizontalDynamicSheets() {
        return userInfoDynamicHorizontalSheets(UserInfoMock.userInfos());
    }

    private static List<UserInfoHorizontalDynamicSheet> userInfoDynamicHorizontalSheets(List<UserInfoMock.UserInfo> userInfos) {
        return userInfos.stream()
                .map(UserInfoNoTemplateHorizontalSheetMock::generateHorizontalDynamicHeaders)
                .toList();
    }

    private static List<UserInfoHorizontalSheet> userInfoHorizontalSheets(List<UserInfoMock.UserInfo> userInfos) {
        return userInfos.stream()
                .map(UserInfoNoTemplateHorizontalSheetMock::createUserInfoHorizontalSheet)
                .toList();
    }


    private static UserInfoHorizontalSheet createUserInfoHorizontalSheet(UserInfoMock.UserInfo userInfo) {
        UserInfoHorizontalSheet sheet = new UserInfoHorizontalSheet();
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
    private static UserInfoHorizontalDynamicSheet generateHorizontalDynamicHeaders(UserInfoMock.UserInfo userInfo) {
        UserInfoHorizontalDynamicSheet sheet = new UserInfoHorizontalDynamicSheet();
        LinkedHashMap<String, CellInfo> dynamicHeaderKeyedCellInfoMap = new LinkedHashMap<>();
        dynamicHeaderKeyedCellInfoMap.put("First Name", CellInfo.builder().header("First Name").value(userInfo.getFirstName()).build());
        dynamicHeaderKeyedCellInfoMap.put("Last Name", CellInfo.builder().header("Last Name").value(userInfo.getLastName()).build());
        dynamicHeaderKeyedCellInfoMap.put("Email", CellInfo.builder().header("Email").value(userInfo.getEmail()).build());
        dynamicHeaderKeyedCellInfoMap.put("Phone Number", CellInfo.builder().header("Phone Number").value(userInfo.getPhoneNumber()).build());
        dynamicHeaderKeyedCellInfoMap.put("Address", CellInfo.builder().header("Address").value(userInfo.getAddress()).build());
        dynamicHeaderKeyedCellInfoMap.put("City", CellInfo.builder().header("City").value(userInfo.getCity()).build());
        dynamicHeaderKeyedCellInfoMap.put("State", CellInfo.builder().header("State").value(userInfo.getState()).build());
        DataCellStyleInfo zipCodeDataCellStyleInfo = DataCellStyleInfo
                                                .builder()
                                                .numberStyleInfo(
                                                        NumberStyleInfo
                                                                .builder()
                                                                .number(true)
                                                                .numberFormat(NumberPrecisionFormat.ZERO_DECIMAL)
                                                                .build()
                                                )
                                                .build();
        CellInfo zipCodeCellinfo = CellInfo
                            .builder()
                            .header("Zip Code")
                            .dataCellStyleInfo(zipCodeDataCellStyleInfo)
                            .value(Integer.parseInt(userInfo.getZipCode()))
                            .build();
        dynamicHeaderKeyedCellInfoMap.put("Zip Code", zipCodeCellinfo);
        dynamicHeaderKeyedCellInfoMap.put("Country", CellInfo.builder().header("Country").value(userInfo.getCountry()).build());
        //add mm/dd/yyyy date format to cell
        DataCellStyleInfo dataCellStyleInfo = DataCellStyleInfo
                                                .builder()
                                                .numberStyleInfo(
                                                        NumberStyleInfo
                                                                .builder()
                                                                .date(true)
                                                                .dateFormat(DateFormatPattern.SLASH_MM_DD_YYYY)
                                                                .build()
                                                )
                                                .build();
        CellInfo dateOfBirth = CellInfo
                                .builder()
                                .header("Date of Birth")
                                .dataCellStyleInfo(dataCellStyleInfo)
                                .value(DateParsingUtil.parseDate(userInfo.getDateOfBirth(), DateParsingUtil.DateFormat.DASH_YYYY_MM_DD))
                                .build();
        dynamicHeaderKeyedCellInfoMap.put("Date of Birth", dateOfBirth);
        sheet.setDynamicHeaderKeyedCellInfoMap(dynamicHeaderKeyedCellInfoMap);
        return sheet;
    }
}
