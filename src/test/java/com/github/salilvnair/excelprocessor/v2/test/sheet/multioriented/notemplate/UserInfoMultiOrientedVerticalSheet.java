package com.github.salilvnair.excelprocessor.v2.test.sheet.multioriented.notemplate;

import com.github.salilvnair.excelprocessor.v2.annotation.Cell;
import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.sheet.DynamicHeaderSheet;
import lombok.Getter;
import lombok.Setter;

@Sheet(
        value = "AllHandsEvent",
        vertical = true,
        headerRowAt = 10
)
@Getter
@Setter
public class UserInfoMultiOrientedVerticalSheet extends DynamicHeaderSheet {
    @Cell(
            value = "First Name"
    )
    private String firstName;
    @Cell(
            value = "Last Name"
    )
    private String lastName;
    @Cell(
            value = "Email"
    )
    private String email;
    @Cell(
            value = "Phone Number"
    )
    private String phoneNumber;
    @Cell(
            value = "Address"
    )
    private String address;
    @Cell(
            value = "City"
    )
    private String city;
    @Cell(
            value = "State"
    )
    private String state;
    @Cell(
            value = "Country"
    )
    private String country;
    @Cell(
            value = "Zip Code"
    )
    private String zipCode;
    @Cell(
            value = "Date of Birth"
    )
    private String dateOfBirth;
}
