package com.pack.exceptions;

import com.pack.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidOtpException.class)
    public ResponseEntity<ApiResponse> handleInvalidOtp(
            InvalidOtpException ex) {

        return ResponseEntity.badRequest()
                .body(ApiResponse.error(400, ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse> handleIllegalArgument(
            IllegalArgumentException ex) {

        return ResponseEntity.badRequest()
                .body(ApiResponse.error(400, ex.getMessage()));
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiResponse> handleConflict(
            ConflictException ex) {

        return ResponseEntity.badRequest()
                .body(ApiResponse.error(400, ex.getMessage()));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request) {

        log.warn("Resource not found: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(
                        404,
                        "Not Found",
                        ex.getMessage(),
                        request.getRequestURI()));
    }

    @ExceptionHandler(UnauthorizedExpenseAccessException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedAccess(
            UnauthorizedExpenseAccessException ex,
            HttpServletRequest request) {

        log.warn("Unauthorized access attempt: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.of(
                        403,
                        "Forbidden",
                        ex.getMessage(),
                        request.getRequestURI()));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException ex,
            HttpServletRequest request) {

        log.warn("Business rule violation: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ErrorResponse.of(
                        422,
                        "Business Rule Violation",
                        ex.getMessage(),
                        request.getRequestURI()));
    }

    @ExceptionHandler(DuplicateMemberException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateMember(
            DuplicateMemberException ex,
            HttpServletRequest request) {

        return status(
                HttpStatus.CONFLICT,
                "Conflict",
                ex.getMessage(),
                request);
    }

    @ExceptionHandler(TeamInactiveException.class)
    public ResponseEntity<ErrorResponse> handleInactiveTeam(
            TeamInactiveException ex,
            HttpServletRequest request) {

        return status(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "Team Inactive",
                ex.getMessage(),
                request);
    }

    @ExceptionHandler(InvalidInviteTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidToken(
            InvalidInviteTokenException ex,
            HttpServletRequest request) {

        return status(
                HttpStatus.BAD_REQUEST,
                "Invalid Token",
                ex.getMessage(),
                request);
    }

    @ExceptionHandler(InsufficientRoleException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientRole(
            InsufficientRoleException ex,
            HttpServletRequest request) {

        return status(
                HttpStatus.FORBIDDEN,
                "Forbidden",
                ex.getMessage(),
                request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        List<ErrorResponse.FieldError> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fe -> new ErrorResponse.FieldError(
                        fe.getField(),
                        fe.getRejectedValue(),
                        fe.getDefaultMessage()))
                .toList();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(
                        400,
                        "Validation Failed",
                        "Request validation failed",
                        request.getRequestURI(),
                        fieldErrors));
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBindException(
            BindException ex,
            HttpServletRequest request) {

        List<ErrorResponse.FieldError> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fe -> new ErrorResponse.FieldError(
                        fe.getField(),
                        fe.getRejectedValue(),
                        fe.getDefaultMessage()))
                .toList();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(
                        400,
                        "Bind Exception",
                        "Parameter binding failed",
                        request.getRequestURI(),
                        fieldErrors));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParameter(
            MissingServletRequestParameterException ex,
            HttpServletRequest request) {

        String message = String.format(
                "Required parameter '%s' of type '%s' is missing",
                ex.getParameterName(),
                ex.getParameterType());

        return status(
                HttpStatus.BAD_REQUEST,
                "Missing Parameter",
                message,
                request);
    }

    // ─── Domain: 422 ──────────────────────────────────────────────────────────

    @ExceptionHandler(LastOwnerException.class)
    public ResponseEntity<ErrorResponse> handleLastOwner(
            LastOwnerException ex, HttpServletRequest req) {
        log.warn("Last-owner guard triggered: {}", ex.getMessage());
        return status(HttpStatus.UNPROCESSABLE_ENTITY, "Last Owner Protection", ex.getMessage(), req);
    }

    @ExceptionHandler(InvalidMembershipStateException.class)
    public ResponseEntity<ErrorResponse> handleInvalidState(
            InvalidMembershipStateException ex, HttpServletRequest req) {
        log.warn("Invalid membership state transition: {}", ex.getMessage());
        return status(HttpStatus.UNPROCESSABLE_ENTITY, "Invalid State Transition", ex.getMessage(), req);
    }

    // ─── Domain: 409 ──────────────────────────────────────────────────────────

    @ExceptionHandler(DuplicateBudgetException.class)
    public ResponseEntity<ErrorResponse> handleDuplicate(
            DuplicateBudgetException ex, HttpServletRequest req) {
        log.warn("Duplicate budget: {}", ex.getMessage());
        return status(HttpStatus.CONFLICT, "Conflict", ex.getMessage(), req);
    }

    @ExceptionHandler({ConcurrentModificationException.class, ObjectOptimisticLockingFailureException.class})
    public ResponseEntity<ErrorResponse> handleOptimisticLock(
            Exception ex, HttpServletRequest req) {
        log.warn("Optimistic locking conflict on {}: {}", req.getRequestURI(), ex.getMessage());
        return status(HttpStatus.CONFLICT, "Concurrent Modification",
                "This budget was modified by another request. Please refresh and try again.", req);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableMessage(
            HttpMessageNotReadableException ex,
            HttpServletRequest request) {

        log.warn("Malformed JSON request: {}", ex.getMessage());

        return status(
                HttpStatus.BAD_REQUEST,
                "Malformed Request",
                "Request body is malformed or missing",
                request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException ex,
            HttpServletRequest request) {

        log.error("Data integrity violation: {}", ex.getMessage());

        return status(
                HttpStatus.CONFLICT,
                "Data Conflict",
                "Data integrity constraint violated",
                request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request) {

        log.error("Unhandled exception on {}: {}",
                request.getRequestURI(),
                ex.getMessage(),
                ex);

        return status(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                "An unexpected error occurred. Please try again later.",
                request);
    }

    private ResponseEntity<ErrorResponse> status(
            HttpStatus status,
            String error,
            String message,
            HttpServletRequest request) {

        return ResponseEntity.status(status)
                .body(ErrorResponse.of(
                        status.value(),
                        error,
                        message,
                        request.getRequestURI()));
    }
}