package com.vms.common;

import com.vms.common.exception.ApiException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // 1. Our custom API exceptions
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiError> handleApiException(
            ApiException ex,
            HttpServletRequest req
    ) {
        HttpStatus status = ex.getStatus();

        ApiError error = ApiError.of(
                status,
                ex.getMessage(),
                req.getRequestURI()
        );

        return ResponseEntity
                .status(status)
                .body(error);
    }

    // 2. @Valid request body validation errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest req
    ) {
        Map<String, String> fieldErrors =
                ex.getBindingResult()
                        .getFieldErrors()
                        .stream()
                        .collect(Collectors.toMap(
                                error -> error.getField(),
                                error -> error.getDefaultMessage(),
                                (existing, replacement) -> existing
                        ));

        ApiError error = ApiError.of(
                HttpStatus.BAD_REQUEST,
                "Validation failed",
                req.getRequestURI(),
                fieldErrors
        );

        return ResponseEntity
                .badRequest()
                .body(error);
    }

    // 3. @Validated query/path parameter validation
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest req
    ) {
        Map<String, String> fieldErrors =
                ex.getConstraintViolations()
                        .stream()
                        .collect(Collectors.toMap(
                                violation -> violation
                                        .getPropertyPath()
                                        .toString(),
                                violation -> violation.getMessage(),
                                (existing, replacement) -> existing
                        ));

        ApiError error = ApiError.of(
                HttpStatus.BAD_REQUEST,
                "Validation failed",
                req.getRequestURI(),
                fieldErrors
        );

        return ResponseEntity
                .badRequest()
                .body(error);
    }

    // 4. Malformed JSON / request body
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleMalformedRequest(
            HttpMessageNotReadableException ex,
            HttpServletRequest req
    ) {
        ApiError error = ApiError.of(
                HttpStatus.BAD_REQUEST,
                "Malformed request body",
                req.getRequestURI()
        );

        return ResponseEntity
                .badRequest()
                .body(error);
    }

    // 5. Invalid username/password
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> handleBadCredentials(
            BadCredentialsException ex,
            HttpServletRequest req
    ) {
        ApiError error = ApiError.of(
                HttpStatus.UNAUTHORIZED,
                "Invalid credentials",
                req.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(error);
    }

    // 6. Authenticated user doesn't have permission
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest req
    ) {
        ApiError error = ApiError.of(
                HttpStatus.FORBIDDEN,
                "Access denied",
                req.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(error);
    }

    // 7. Database constraint violations
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrityViolation(
            DataIntegrityViolationException ex,
            HttpServletRequest req
    ) {
        ApiError error = ApiError.of(
                HttpStatus.CONFLICT,
                "Duplicate or invalid data",
                req.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(error);
    }

    // 8. Anything unexpected
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnhandledException(
            Exception ex,
            HttpServletRequest req
    ) {
        log.error("Unhandled", ex);

        ApiError error = ApiError.of(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Something went wrong",
                req.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(error);
    }
}