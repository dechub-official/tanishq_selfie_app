package com.dechub.tanishq.util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Utility class for manual input validation
 * Used for request parameters and fields that cannot use Bean Validation
 */
public class InputValidator {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^[6-9]\\d{9}$");
    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z\\s.'-]+$");
    private static final Pattern STORE_CODE_PATTERN = Pattern.compile("^[A-Za-z0-9_-]+$");
    private static final Pattern ALPHANUMERIC_PATTERN = Pattern.compile("^[A-Za-z0-9\\s_-]+$");

    /**
     * Validate phone number
     */
    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.isEmpty()) {
            return false;
        }
        String cleanPhone = phone.replaceAll("[\\s\\-()]", "");
        return PHONE_PATTERN.matcher(cleanPhone).matches();
    }

    /**
     * Validate name (only letters, spaces, and basic punctuation)
     */
    public static boolean isValidName(String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }
        return name.length() >= 2 && name.length() <= 100 && NAME_PATTERN.matcher(name).matches();
    }

    /**
     * Validate store code
     */
    public static boolean isValidStoreCode(String storeCode) {
        if (storeCode == null || storeCode.isEmpty()) {
            return false;
        }
        return storeCode.length() <= 50 && STORE_CODE_PATTERN.matcher(storeCode).matches();
    }

    /**
     * Validate string length
     */
    public static boolean isValidLength(String value, int maxLength) {
        if (value == null) {
            return true;
        }
        return value.length() <= maxLength;
    }

    /**
     * Validate string length with min
     */
    public static boolean isValidLength(String value, int minLength, int maxLength) {
        if (value == null) {
            return true;
        }
        return value.length() >= minLength && value.length() <= maxLength;
    }

    /**
     * Validate email format
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    /**
     * Validate alphanumeric string
     */
    public static boolean isAlphanumeric(String value) {
        if (value == null || value.isEmpty()) {
            return true;
        }
        return ALPHANUMERIC_PATTERN.matcher(value).matches();
    }

    /**
     * Sanitize string input (remove potential XSS characters)
     */
    public static String sanitize(String input) {
        if (input == null) {
            return null;
        }
        return input.replaceAll("[<>\"'&]", "");
    }

    /**
     * Validate and collect errors
     */
    public static Map<String, String> validateRequest(Map<String, Object> fields) {
        Map<String, String> errors = new HashMap<>();

        for (Map.Entry<String, Object> entry : fields.entrySet()) {
            String fieldName = entry.getKey();
            Object fieldValue = entry.getValue();

            if (fieldValue == null || (fieldValue instanceof String && ((String) fieldValue).trim().isEmpty())) {
                errors.put(fieldName, fieldName + " is required");
            }
        }

        return errors;
    }
}

