package com.chatsito.api.config;

import java.util.List;
import java.util.Map;

import com.chatsito.api.auth.LegacyAuthenticationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class LegacyValidationExceptionHandler {
    @ExceptionHandler(LegacyAuthenticationException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    Map<String, Object> handleAuthentication(LegacyAuthenticationException exception) {
        return Map.of("detail", exception.getMessage());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    Map<String, Object> handleMissingParameter(MissingServletRequestParameterException exception) {
        return Map.of("detail", List.of(new ValidationDetail(
                "missing",
                List.of("query", exception.getParameterName()),
                "Field required",
                null)));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    Map<String, Object> handleInvalidParameter(MethodArgumentTypeMismatchException exception) {
        return Map.of("detail", List.of(new ValidationDetail(
                "int_parsing",
                List.of("query", exception.getName()),
                "Input should be a valid integer, unable to parse string as an integer",
                String.valueOf(exception.getValue()))));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    Map<String, Object> handleInvalidBody(MethodArgumentNotValidException exception) {
        var details = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> "Email".equals(error.getCode())
                        ? new ValidationDetail(
                                "value_error",
                                List.of("body", error.getField()),
                                "value is not a valid email address",
                                error.getRejectedValue())
                        : new ValidationDetail(
                                "missing",
                                List.of("body", error.getField()),
                                "Field required",
                                error.getRejectedValue()))
                .toList();
        return Map.of("detail", details);
    }

    private record ValidationDetail(String type, List<String> loc, String msg, Object input) {
    }
}
