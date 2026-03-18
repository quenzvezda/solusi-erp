package com.solusi.erp.inventory.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

/**
 * Utility for generating Serial Numbers.
 * Format: SN-YYMM-XXXXX
 */
public class SerialNumberGenerator {

    private static final Random RANDOM = new Random();

    public static String generate() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMM"));
        int randomPart = RANDOM.nextInt(100000);
        return String.format("SN-%s-%05d", datePart, randomPart);
    }
}
