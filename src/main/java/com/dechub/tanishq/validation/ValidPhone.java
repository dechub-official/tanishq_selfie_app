package com.dechub.tanishq.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * Custom annotation for Indian phone number validation
 * Validates that phone number:
 * - Contains only digits
 * - Has 10 digits
 * - Starts with 6, 7, 8, or 9
 */
@Documented
@Constraint(validatedBy = PhoneValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPhone {
    String message() default "Invalid phone number. Must be 10 digits starting with 6, 7, 8, or 9";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

