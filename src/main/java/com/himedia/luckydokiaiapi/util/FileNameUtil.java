package com.himedia.luckydokiaiapi.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class FileNameUtil {
    private static final String MONTHLY_REPORT_FORMAT = "monthly-ai-report-%s.pdf";

    public static String generateMonthlyReportFileName(LocalDate date) {
        return String.format(MONTHLY_REPORT_FORMAT,
                date.format(DateTimeFormatter.ofPattern("yyyy-MM")));
    }
}
