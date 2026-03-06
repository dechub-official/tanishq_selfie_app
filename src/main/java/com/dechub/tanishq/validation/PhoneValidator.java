package com.dechub.tanishq.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

/**
 * Validator for Indian phone numbers
 * Validates format: 10 digits starting with 6, 7, 8, or 9
 */
public class PhoneValidator implements ConstraintValidator<ValidPhone, String> {

    // Indian mobile number pattern: starts with 6-9, followed by 9 digits
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[6-9]\\d{9}$");

    @Override
    public void initialize(ValidPhone constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(String phone, ConstraintValidatorContext context) {
        // Null or empty values are handled by @NotBlank/@NotNull annotations
        if (phone == null || phone.isEmpty()) {
            return true;
        }

        // Remove any whitespace or special characters
        String cleanPhone = phone.replaceAll("[\\s\\-()]", "");

        // Validate against pattern
        return PHONE_PATTERN.matcher(cleanPhone).matches();
    }
}

