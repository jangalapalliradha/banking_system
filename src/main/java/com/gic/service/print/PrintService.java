package com.gic.service.print;

import java.time.LocalDate;

public interface PrintService {

     void printFormat(String account, LocalDate fromDate, LocalDate toDate, String yearMonth, Boolean isFromPrintFunction) ;
}
