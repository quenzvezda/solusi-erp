package com.solusi.erp.inventory.util;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.regex.Pattern;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SerialNumberGeneratorTest {

    @Test
    void shouldGenerateCorrectFormat() {
        String sn = SerialNumberGenerator.generate();
        
        // Format: SN-YYMM-XXXXX (e.g. SN-2603-00001)
        // Note: Year 2026 based on session context
        String regex = "^SN-\\d{4}-\\d{5}$";
        assertTrue(Pattern.matches(regex, sn), "SN should match format SN-YYMM-XXXXX");
    }
}
