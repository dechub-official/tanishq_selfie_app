package com.dechub.tanishq.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

/**
 * Validator for store codes
 * Validates format: alphanumeric with hyphens, reasonable length
 */
public class StoreCodeValidator implements ConstraintValidator<ValidStoreCode, String> {

    // Store code pattern: alphanumeric, hyphens, underscores
    private static final Pattern STORE_CODE_PATTERN = Pattern.compile("^[A-Za-z0-9_-]+$");
    private static final int MAX_LENGTH = 50;

    @Override
    public void initialize(ValidStoreCode constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(String storeCode, ConstraintValidatorContext context) {
        // Null or empty values are handled by @NotBlank/@NotNull annotations
        if (storeCode == null || storeCode.isEmpty()) {
            return true;
        }

        // Check length
        if (storeCode.length() > MAX_LENGTH) {
            return false;
        }

        // Validate against pattern
        return STORE_CODE_PATTERN.matcher(storeCode).matches();
    }
}

