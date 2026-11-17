package com.github.salilvnair.excelprocessor.v2.test.archived.sheet;

import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.sheet.DynamicHeaderSheet;
import lombok.Getter;
import lombok.Setter;

@Sheet(
        value="END OFC TRUNKING",
        userDefinedTemplate = true,
        dynamicHeaders = true,
        headerRowAt=9,
        headerColumnEndsAt = "G"
)
@Getter
@Setter
public class EndOfcSheet extends DynamicHeaderSheet {
//    @Cell("Activity Type*")
//    private String activityType;
//    @Cell("TELCO*")
//    private String telco;
//    @Cell("2-6 CODE*")
//    private String Code26;
//    @Cell("ORIGINATING OFFIC CLLI*")
//    private String originatingOfficClli;
//    @Cell("ORIGINATING TRUNK GROUP")
//    private String originatingTrunkGroup;
//    @Cell("TERMINATING TRUNK GROUP*")
//    private String terminatingTrunkGroup;
//    @Cell("DEFAULT ESN*")
//    private String defaultEsn;
}
