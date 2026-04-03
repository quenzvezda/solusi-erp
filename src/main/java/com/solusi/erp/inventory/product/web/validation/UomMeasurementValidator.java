package com.solusi.erp.inventory.product.web.validation;

import com.solusi.erp.inventory.product.web.dto.ProductSaveRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.math.BigDecimal;

public class UomMeasurementValidator implements ConstraintValidator<ValidUomMeasurement, ProductSaveRequest> {

    @Override
    public boolean isValid(ProductSaveRequest request, ConstraintValidatorContext context) {
        if (request == null) {
            return true;
        }

        boolean isValid = true;

        // Weight validation
        if ((request.getWeightNet() != null && request.getWeightNet().compareTo(BigDecimal.ZERO) > 0) ||
                (request.getWeightGross() != null && request.getWeightGross().compareTo(BigDecimal.ZERO) > 0)) {
            if (request.getWeightUomId() == null) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("{validation.notnull}")
                        .addPropertyNode("weightUomId")
                        .addConstraintViolation();
                isValid = false;
            }
        }

        // Dimension validation
        if ((request.getLength() != null && request.getLength().compareTo(BigDecimal.ZERO) > 0) ||
                (request.getWidth() != null && request.getWidth().compareTo(BigDecimal.ZERO) > 0) ||
                (request.getHeight() != null && request.getHeight().compareTo(BigDecimal.ZERO) > 0)) {
            if (request.getDimensionUomId() == null) {
                if (isValid) {
                    context.disableDefaultConstraintViolation();
                }
                context.buildConstraintViolationWithTemplate("{validation.notnull}")
                        .addPropertyNode("dimensionUomId")
                        .addConstraintViolation();
                isValid = false;
            }
        }

        return isValid;
    }
}
