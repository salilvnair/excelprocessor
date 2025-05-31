package com.github.salilvnair.excelprocessor.v2.test.writer.mock;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

public class UserInfoMock {


    public static List<UserInfo> userInfos() {
        return List.of(
                createUserInfo("John", "Doe", "johndoe@gmail.com", "1234567890", "123 Elm St", "Springfield", "IL", "USA", "62701", "1990-01-01"),
                createUserInfo("Jane", "Smith", "josmi@gmail.com", "0987654321", "456 Oak St", "Springfield", "IL", "USA", "62702", "1992-02-02"),
                createUserInfo("Alice", "Johnson", "aljohn@gmail.com", "1122334455", "789 Pine St", "Springfield", "IL", "USA", "62703", "1994-03-03"),
                createUserInfo("Bob", "Brown", "bobra@gmail.com", "5566778899", "321 Maple St", "Springfield", "IL", "USA", "62704", "1996-04-04"),
                createUserInfo("Charlie", "Davis", "charda@gmail.com", "9988776655", "654 Cedar St", "Springfield", "IL", "USA", "62705", "1998-05-05"),
                createUserInfo("Eve", "Wilson", "evewil12@gmail.com", "1231231234", "987 Birch St", "Springfield", "IL", "USA", "62706", "2000-06-06"),
                createUserInfo("Frank", "Garcia", "frankgar123@gmail.com", "4321432143", "159 Walnut St", "Springfield", "IL", "USA", "62707", "2002-07-07"),
                createUserInfo("Grace", "Martinez", "grace_mar@gmail.com", "3210321032", "753 Chestnut St", "Springfield", "IL", "USA", "62708", "2004-08-08"),
                createUserInfo("Hank", "Lopez", "hanklop@gmail.com", "6543654365", "852 Poplar St", "Springfield", "IL", "USA", "62709", "2006-09-09"),
                createUserInfo("Ivy", "Gonzalez", "ivygonz@gmail.com", "9876987698", "963 Fir St", "Springfield", "IL", "USA", "62710", "2008-10-10")
        );
    }


    private static UserInfo createUserInfo(String firstName, String lastName, String email, String phoneNumber, String address, String city, String state, String country, String zipCode, String dateOfBirth) {
        return new UserInfo(firstName, lastName, email, phoneNumber, address, city, state, country, zipCode, dateOfBirth);
    }


    @Setter
    @Getter
    public static class UserInfo {
        // Getters and setters
        private String firstName;
        private String lastName;
        private String email;
        private String phoneNumber;
        private String address;
        private String city;
        private String state;
        private String country;
        private String zipCode;
        private String dateOfBirth;

        public UserInfo(String firstName, String lastName, String email, String phoneNumber, String address, String city, String state, String country, String zipCode, String dateOfBirth) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
            this.phoneNumber = phoneNumber;
            this.address = address;
            this.city = city;
            this.state = state;
            this.country = country;
            this.zipCode = zipCode;
            this.dateOfBirth = dateOfBirth;
        }
    }
}
