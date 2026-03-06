package com.dechub.tanishq.exception;

import com.dechub.tanishq.util.ResponseDataDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Global Exception Handler for all application exceptions
 * 
 * SECURITY FIX - OWASP A05: Security Misconfiguration
 * - Prevents stack traces from being exposed to clients
 * - Returns generic error messages to users
 * - Logs detailed errors server-side only
 * - Handles database errors without exposing SQL details
 * 
 * @author VAPT Security Fix Team
 * @since March 4, 2026
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String GENERIC_ERROR_MESSAGE = "An unexpected error occurred. Please try again later.";
    private static final String GENERIC_DATABASE_ERROR = "A database error occurred. Please contact support if the issue persists.";
    
    /**
     * Generate a unique error reference ID for tracking
     */
    private String generateErrorReference() {
        return "ERR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * Get timestamp for error logging
     */
    private String getTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    // ========== VALIDATION EXCEPTIONS ==========

    /**
     * Handle validation errors for @Valid on request body
     * Status: 400 Bad Request
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseDataDTO> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        log.warn("Validation failed for request to {}: {}", request.getRequestURI(), errors);

        ResponseDataDTO response = new ResponseDataDTO();
        response.setStatus(false);
        response.setMessage("Validation failed");
        response.setResult(errors);

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle validation errors for @Validated on method parameters
     * Status: 400 Bad Request
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ResponseDataDTO> handleConstraintViolationException(ConstraintViolationException ex, HttpServletRequest request) {
        Map<String, String> errors = ex.getConstraintViolations()
            .stream()
            .collect(Collectors.toMap(
                violation -> violation.getPropertyPath().toString(),
                ConstraintViolation::getMessage,
                (existing, replacement) -> existing + "; " + replacement
            ));

        log.warn("Constraint violation at {}: {}", request.getRequestURI(), errors);

        ResponseDataDTO response = new ResponseDataDTO();
        response.setStatus(false);
        response.setMessage("Validation failed");
        response.setResult(errors);

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle illegal argument exceptions (for custom validations)
     * Status: 400 Bad Request
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ResponseDataDTO> handleIllegalArgumentException(IllegalArgumentException ex, HttpServletRequest request) {
        log.warn("Illegal argument at {}: {}", request.getRequestURI(), ex.getMessage());

        ResponseDataDTO response = new ResponseDataDTO();
        response.setStatus(false);
        response.setMessage(ex.getMessage());

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle missing required request parameters
     * Status: 400 Bad Request
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ResponseDataDTO> handleMissingParams(MissingServletRequestParameterException ex, HttpServletRequest request) {
        log.warn("Missing parameter '{}' at {}", ex.getParameterName(), request.getRequestURI());

        ResponseDataDTO response = new ResponseDataDTO();
        response.setStatus(false);
        response.setMessage(String.format("Required parameter '%s' is missing", ex.getParameterName()));

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle type mismatch errors (e.g., passing string where integer is expected)
     * Status: 400 Bad Request
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ResponseDataDTO> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        String errorRef = generateErrorReference();
        log.warn("[{}] Type mismatch at {}: Parameter '{}' - Expected type: {}, Provided value: '{}'", 
                errorRef, request.getRequestURI(), ex.getName(), 
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown", 
                ex.getValue());

        ResponseDataDTO response = new ResponseDataDTO();
        response.setStatus(false);
        response.setMessage(String.format("Invalid value for parameter '%s'", ex.getName()));

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle malformed JSON request body
     * Status: 400 Bad Request
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ResponseDataDTO> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        String errorRef = generateErrorReference();
        log.warn("[{}] Malformed request body at {}: {}", errorRef, request.getRequestURI(), ex.getMessage());

        ResponseDataDTO response = new ResponseDataDTO();
        response.setStatus(false);
        response.setMessage("Invalid request format. Please check your request body.");

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // ========== DATABASE EXCEPTIONS ==========

    /**
     * Handle data integrity violations (unique constraints, foreign key violations, etc.)
     * Status: 409 Conflict
     * SECURITY: Hides SQL error details from client
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ResponseDataDTO> handleDataIntegrityViolation(DataIntegrityViolationException ex, HttpServletRequest request) {
        String errorRef = generateErrorReference();
        
        // Log detailed error server-side (with stack trace)
        log.error("[{}] Data integrity violation at {} - Time: {}", errorRef, request.getRequestURI(), getTimestamp(), ex);

        // Return generic message to client (NO SQL details)
        ResponseDataDTO response = new ResponseDataDTO();
        response.setStatus(false);
        response.setMessage("A database constraint was violated. Please check your input data.");
        
        // Add error reference for support tracking
        Map<String, String> errorDetails = new HashMap<>();
        errorDetails.put("errorReference", errorRef);
        errorDetails.put("timestamp", getTimestamp());
        response.setResult(errorDetails);

        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    /**
     * Handle generic database access exceptions
     * Status: 500 Internal Server Error
     * SECURITY: Hides SQL error details from client
     */
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ResponseDataDTO> handleDataAccessException(DataAccessException ex, HttpServletRequest request) {
        String errorRef = generateErrorReference();
        
        // Log detailed error server-side
        log.error("[{}] Database access error at {} - Time: {}", errorRef, request.getRequestURI(), getTimestamp(), ex);

        // Return generic message to client
        ResponseDataDTO response = new ResponseDataDTO();
        response.setStatus(false);
        response.setMessage(GENERIC_DATABASE_ERROR);
        
        Map<String, String> errorDetails = new HashMap<>();
        errorDetails.put("errorReference", errorRef);
        errorDetails.put("timestamp", getTimestamp());
        response.setResult(errorDetails);

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handle SQL exceptions
     * Status: 500 Internal Server Error
     * SECURITY: Hides SQL error details from client
     */
    @ExceptionHandler(SQLException.class)
    public ResponseEntity<ResponseDataDTO> handleSQLException(SQLException ex, HttpServletRequest request) {
        String errorRef = generateErrorReference();
        
        // Log detailed SQL error server-side
        log.error("[{}] SQL error at {} - Time: {} - SQL State: {}, Error Code: {}", 
                errorRef, request.getRequestURI(), getTimestamp(), ex.getSQLState(), ex.getErrorCode(), ex);

        // Return generic message to client (NO SQL details)
        ResponseDataDTO response = new ResponseDataDTO();
        response.setStatus(false);
        response.setMessage(GENERIC_DATABASE_ERROR);
        
        Map<String, String> errorDetails = new HashMap<>();
        errorDetails.put("errorReference", errorRef);
        errorDetails.put("timestamp", getTimestamp());
        response.setResult(errorDetails);

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // ========== HTTP AND FILE EXCEPTIONS ==========

    /**
     * Handle unsupported HTTP methods
     * Status: 405 Method Not Allowed
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ResponseDataDTO> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        log.warn("Method {} not supported for {}", ex.getMethod(), request.getRequestURI());

        ResponseDataDTO response = new ResponseDataDTO();
        response.setStatus(false);
        response.setMessage(String.format("HTTP method '%s' is not supported for this endpoint", ex.getMethod()));

        return new ResponseEntity<>(response, HttpStatus.METHOD_NOT_ALLOWED);
    }

    /**
     * Handle unsupported media types
     * Status: 415 Unsupported Media Type
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ResponseDataDTO> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex, HttpServletRequest request) {
        log.warn("Media type not supported at {}: {}", request.getRequestURI(), ex.getContentType());

        ResponseDataDTO response = new ResponseDataDTO();
        response.setStatus(false);
        response.setMessage("Unsupported media type. Please check Content-Type header.");

        return new ResponseEntity<>(response, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    /**
     * Handle file size exceeded errors
     * Status: 413 Payload Too Large
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ResponseDataDTO> handleMaxSizeException(MaxUploadSizeExceededException ex, HttpServletRequest request) {
        log.warn("File upload size exceeded at {}: {}", request.getRequestURI(), ex.getMessage());

        ResponseDataDTO response = new ResponseDataDTO();
        response.setStatus(false);
        response.setMessage("File size exceeds maximum allowed limit (100MB)");

        return new ResponseEntity<>(response, HttpStatus.PAYLOAD_TOO_LARGE);
    }

    /**
     * Handle file I/O exceptions
     * Status: 500 Internal Server Error
     */
    @ExceptionHandler(IOException.class)
    public ResponseEntity<ResponseDataDTO> handleIOException(IOException ex, HttpServletRequest request) {
        String errorRef = generateErrorReference();
        log.error("[{}] I/O error at {} - Time: {}", errorRef, request.getRequestURI(), getTimestamp(), ex);

        ResponseDataDTO response = new ResponseDataDTO();
        response.setStatus(false);
        response.setMessage("An error occurred while processing your request. Please try again.");
        
        Map<String, String> errorDetails = new HashMap<>();
        errorDetails.put("errorReference", errorRef);
        errorDetails.put("timestamp", getTimestamp());
        response.setResult(errorDetails);

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handle access denied exceptions
     * Status: 403 Forbidden
     */
    @ExceptionHandler({AccessDeniedException.class, java.nio.file.AccessDeniedException.class})
    public ResponseEntity<ResponseDataDTO> handleAccessDenied(Exception ex, HttpServletRequest request) {
        log.warn("Access denied at {}: {}", request.getRequestURI(), ex.getMessage());

        ResponseDataDTO response = new ResponseDataDTO();
        response.setStatus(false);
        response.setMessage("You do not have permission to access this resource");

        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    /**
     * Handle 404 Not Found
     * Status: 404 Not Found
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ResponseDataDTO> handleNotFound(NoHandlerFoundException ex, HttpServletRequest request) {
        log.warn("Endpoint not found: {} {}", ex.getHttpMethod(), ex.getRequestURL());

        ResponseDataDTO response = new ResponseDataDTO();
        response.setStatus(false);
        response.setMessage("The requested resource was not found");

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    // ========== CATCH-ALL EXCEPTION HANDLER ==========

    /**
     * Handle all other unhandled exceptions
     * Status: 500 Internal Server Error
     * SECURITY: This is the most critical handler - it catches everything else
     * and prevents stack traces from leaking to the client
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseDataDTO> handleGlobalException(Exception ex, HttpServletRequest request) {
        String errorRef = generateErrorReference();
        
        // Log full exception details server-side (stack trace included)
        log.error("[{}] Unhandled exception at {} - Time: {} - Exception Type: {}", 
                errorRef, request.getRequestURI(), getTimestamp(), ex.getClass().getName(), ex);

        // Return generic message to client (NO stack trace or internal details)
        ResponseDataDTO response = new ResponseDataDTO();
        response.setStatus(false);
        response.setMessage(GENERIC_ERROR_MESSAGE);
        
        // Provide error reference for support to track logs
        Map<String, String> errorDetails = new HashMap<>();
        errorDetails.put("errorReference", errorRef);
        errorDetails.put("timestamp", getTimestamp());
        response.setResult(errorDetails);

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

