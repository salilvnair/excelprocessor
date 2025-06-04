package com.github.salilvnair.excelprocessor.v2.test.writer.mock;

import com.github.salilvnair.excelprocessor.v2.test.sheet.horizontal.withtemplate.MultiHeaderRowIndexHorizontalSheet;
import java.util.ArrayList;
import java.util.List;

public class MultipleHeaderIndexWithTemplateHorizontalSheetMock {
   public static List<MultiHeaderRowIndexHorizontalSheet> generateHorizontalSheets() {
       return multiHeaderRowIndexHorizontalSheets();
   }


   private static List<MultiHeaderRowIndexHorizontalSheet> multiHeaderRowIndexHorizontalSheets() {
       List<MultiHeaderRowIndexHorizontalSheet> multiHeaderRowIndexHorizontalSheets = new ArrayList<>();


       MultiHeaderRowIndexHorizontalSheet multiHeaderRowIndexHorizontalSheet = new MultiHeaderRowIndexHorizontalSheet();
       multiHeaderRowIndexHorizontalSheet.setA("A1");
       multiHeaderRowIndexHorizontalSheet.setB("B1");
       multiHeaderRowIndexHorizontalSheet.setC("C1");
       multiHeaderRowIndexHorizontalSheet.setD("D1");
       multiHeaderRowIndexHorizontalSheet.setE("E1");
       multiHeaderRowIndexHorizontalSheet.setF("F1");
       multiHeaderRowIndexHorizontalSheet.setG("G1");
       multiHeaderRowIndexHorizontalSheet.setH("H1");

       multiHeaderRowIndexHorizontalSheets.add(multiHeaderRowIndexHorizontalSheet);

       multiHeaderRowIndexHorizontalSheet = new MultiHeaderRowIndexHorizontalSheet();
       multiHeaderRowIndexHorizontalSheet.setA("A2");
       multiHeaderRowIndexHorizontalSheet.setB("B2");
       multiHeaderRowIndexHorizontalSheet.setC("C2");
       multiHeaderRowIndexHorizontalSheet.setD("D2");
       multiHeaderRowIndexHorizontalSheet.setE("E2");
       multiHeaderRowIndexHorizontalSheet.setF("F2");
       multiHeaderRowIndexHorizontalSheet.setG("G2");
       multiHeaderRowIndexHorizontalSheet.setH("H2");
       multiHeaderRowIndexHorizontalSheets.add(multiHeaderRowIndexHorizontalSheet);


       return multiHeaderRowIndexHorizontalSheets;
   }

}
