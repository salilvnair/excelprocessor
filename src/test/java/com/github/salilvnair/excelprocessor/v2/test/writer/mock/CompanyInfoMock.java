package com.github.salilvnair.excelprocessor.v2.test.writer.mock;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

public class CompanyInfoMock {


    public static List<CompanyInfo> companyInfos() {
        return List.of(
                createCompanyInfo("TechCorp", "TC12345", "+1-800-555-0199", "123 Tech Lane, Silicon Valley, CA 94043"),
                createCompanyInfo("InnovateX", "IX67890", "+1-800-555-0123", "456 Innovation Drive, Tech City, CA 94044"),
                createCompanyInfo("FutureWorks", "FW54321", "+1-800-555-0456", "789 Future Blvd, Silicon Valley, CA 94045"),
                createCompanyInfo("GlobalTech", "GT98765", "+1-800-555-0789", "321 Global Street, Tech Hub, CA 94046"),
                createCompanyInfo("NextGen Solutions", "NG11223", "+1-800-555-0912", "654 NextGen Avenue, Innovation Park, CA 94047"),
                createCompanyInfo("SmartTech", "ST44556", "+1-800-555-0345", "987 SmartTech Road, Silicon Valley, CA 94048"),
                createCompanyInfo("Quantum Innovations", "QI77889", "+1-800-555-0678", "159 Quantum Lane, Tech Valley, CA 94049"),
                createCompanyInfo("Synergy Systems", "SS22334", "+1-800-555-0101", "753 Synergy Street, Innovation District, CA 94050"),
                createCompanyInfo("Visionary Tech", "VT55667", "+1-800-555-0202", "852 Visionary Way, Silicon Valley, CA 94051"),
                createCompanyInfo("Pioneering Solutions", "PS88990", "+1-800-555-0303", "963 Pioneering Path, Tech City, CA 94052")
        );
    }



    private static CompanyInfo createCompanyInfo(String name, String companyId, String phone, String address) {
        return new CompanyInfo(name, companyId, phone, address);
    }

    @Setter
    @Getter
    public static class CompanyInfo {
        // Getters and setters
        private String name;
        private String companyId;
        private String frontDeskPhoneNumber;
        private String address;

        public CompanyInfo(String name, String companyId, String frontDeskPhoneNumber, String address) {
            this.name = name;
            this.companyId = companyId;
            this.frontDeskPhoneNumber = frontDeskPhoneNumber;
            this.address = address;
        }

    }
}
