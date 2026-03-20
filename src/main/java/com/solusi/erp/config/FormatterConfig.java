package com.solusi.erp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.format.Formatter;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.Locale;

/**
 * Standardized Formatting Configuration for Numeric types.
 * Handles parsing of numbers with thousand separators from UI to Java,
 * and consistent formatting from Java to UI.
 */
@Configuration
public class FormatterConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addFormatter(new StandardBigDecimalFormatter());
        registry.addFormatter(new StandardIntegerFormatter());
    }

    /**
     * Formatter for BigDecimal (Decimals).
     * Format: 1,250,000.00
     */
    public static class StandardBigDecimalFormatter implements Formatter<BigDecimal> {
        @Override
        public BigDecimal parse(String text, Locale locale) throws ParseException {
            if (text == null || text.trim().isEmpty()) return null;
            // Remove thousand separators before parsing
            String cleanText = text.replaceAll(",", "");
            return new BigDecimal(cleanText);
        }

        @Override
        public String print(BigDecimal object, Locale locale) {
            if (object == null) return "";
            DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
            symbols.setGroupingSeparator(',');
            symbols.setDecimalSeparator('.');
            DecimalFormat df = new DecimalFormat("#,##0.00", symbols);
            return df.format(object);
        }
    }

    /**
     * Formatter for Integer (Whole Numbers).
     * Format: 1,250,000
     */
    public static class StandardIntegerFormatter implements Formatter<Integer> {
        @Override
        public Integer parse(String text, Locale locale) throws ParseException {
            if (text == null || text.trim().isEmpty()) return null;
            // Remove thousand separators before parsing
            String cleanText = text.replaceAll(",", "");
            return Integer.parseInt(cleanText);
        }

        @Override
        public String print(Integer object, Locale locale) {
            if (object == null) return "";
            DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
            symbols.setGroupingSeparator(',');
            DecimalFormat df = new DecimalFormat("#,##0", symbols);
            return df.format(object);
        }
    }
}
