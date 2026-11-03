package org.example.javaframe.common;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BizException.class)
        public ResponseEntity<Result<Void>> handleBiz(BizException ex) {
            return status(ex.getErrorCode());
}

    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            HandlerMethodValidationException.class,
            ConstraintViolationException.class
    })
    public ResponseEntity<Result<FieldErrors>> handleValid(Exception ex) {
        List<FieldErrors.FieldErrorItem> fields = new ArrayList<>();
        if (ex instanceof MethodArgumentNotValidException invalid) {
            collect(invalid.getBindingResult().getFieldErrors(), invalid.getBindingResult().getGlobalErrors(), fields);
        } else if (ex instanceof HandlerMethodValidationException invalid) {
            invalid.getParameterValidationResults().forEach(result -> {
                String name = result.getMethodParameter().getParameterName();
                String field = name == null ? "request" : name;
                result.getResolvableErrors().forEach(error -> fields.add(item(field, error.getDefaultMessage())));
            });
        } else if (ex instanceof ConstraintViolationException invalid) {
            for (ConstraintViolation<?> violation : invalid.getConstraintViolations()) {
                fields.add(item(violation.getPropertyPath().toString(), violation.getMessage()));
            }
        }
        Result<FieldErrors> body = Result.fail(
                ErrorCode.BAD_REQUEST.getCode(),
                ErrorCode.BAD_REQUEST.getMessage(),
                new FieldErrors(List.copyOf(fields)));
        return ResponseEntity.badRequest().body(body);
    }
    @ExceptionHandler(HttpMessageNotReadableException.class)
public ResponseEntity<Result<Void>> handleUnreadable(HttpMessageNotReadableException ex) {
    return status(ErrorCode.BAD_REQUEST);
}

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Result<Void>> handleNotFound(NoResourceFoundException ex) {
        return status(ErrorCode.NOT_FOUND);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Result<Void>> handleMethod(HttpRequestMethodNotSupportedException ex) {
        return status(ErrorCode.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleUnknown(Exception ex) {
        log.error("未处理异常", ex);
        return status(ErrorCode.SYSTEM_ERROR);
    }

    private static ResponseEntity<Result<Void>> status(ErrorCode errorCode) {
        return ResponseEntity.status(statusOf(errorCode))
                .body(Result.fail(errorCode.getCode(), errorCode.getMessage()));
    }

    private static HttpStatus statusOf(ErrorCode errorCode) {
        return switch (errorCode) {
            case BAD_REQUEST -> HttpStatus.BAD_REQUEST;
            case UNAUTHORIZED, REFRESH_INVALID -> HttpStatus.UNAUTHORIZED;
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case METHOD_NOT_ALLOWED -> HttpStatus.METHOD_NOT_ALLOWED;
            case SYSTEM_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }

    private static void collect(List<FieldError> fieldErrors, List<ObjectError> globalErrors,
                                List<FieldErrors.FieldErrorItem> fields) {
        for (FieldError error : fieldErrors) {
            fields.add(item(error.getField(), error.getDefaultMessage()));
        }
        for (ObjectError error : globalErrors) {
            fields.add(item(error.getObjectName(), error.getDefaultMessage()));
        }
    }

    private static FieldErrors.FieldErrorItem item(String field, String message) {
        String text = message == null || message.isBlank() ? ErrorCode.BAD_REQUEST.getMessage() : message;
        return new FieldErrors.FieldErrorItem(field, text);
    }
}