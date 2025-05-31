package com.github.salilvnair.excelprocessor.v2.test.archived.sheet;

import com.github.salilvnair.excelprocessor.v2.annotation.Sheet;
import com.github.salilvnair.excelprocessor.v2.sheet.DynamicHeaderSheet;
import lombok.Getter;
import lombok.Setter;

@Sheet(
        value="END OFC TRUNKING",
        vertical=true,
        userDefinedTemplate = true,
        dynamicHeaders = true,
        headerRowAt=3,
        headerRowEndsAt = 6,
        valueColumnEndsAt = "B"
)
@Getter
@Setter
public class PsapInfoSheet extends DynamicHeaderSheet {
//    @Cell("PSAP/HOST NAME* (Exact Name)")
//    private String psaphostNameexactName;
//    @Cell("PSAP RDN*")
//    private String psapRdn;
//    @Cell("PSAP TGN-(5E) or PSAP CLLI-DMS*")
//    private String psapTgn5eOrPsapCllidms;
//    @Cell("E911 Tandem CLLI*")
//    private String e911TandemClli;
}