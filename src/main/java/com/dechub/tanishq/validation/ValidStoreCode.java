package com.dechub.tanishq.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * Custom annotation for store code validation
 * Validates that store code:
 * - Is not empty
 * - Contains only alphanumeric characters and hyphens
 * - Does not exceed max length
 */
@Documented
@Constraint(validatedBy = StoreCodeValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidStoreCode {
    String message() default "Invalid store code format";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

