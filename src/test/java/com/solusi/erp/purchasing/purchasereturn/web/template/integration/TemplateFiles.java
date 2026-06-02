package com.solusi.erp.purchasing.purchasereturn.web.template.integration;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

final class TemplateFiles {

    private TemplateFiles() {
    }

    static String read(String path) {
        try (InputStream input = TemplateFiles.class.getClassLoader().getResourceAsStream(path)) {
            if (input == null) {
                throw new IllegalStateException("Resource not found: " + path);
            }
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }
}
