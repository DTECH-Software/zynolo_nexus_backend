package com.zynolo_nexus.setting_service.exception;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.zynolo_nexus.setting_service.dto.api.MessageResponseDTO;

import lombok.RequiredArgsConstructor;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<MessageResponseDTO<Object>> handleBadRequest(BadRequestException ex) {
        return buildResponse(ex.getMessage(), HttpStatus.BAD_REQUEST, null);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<MessageResponseDTO<Object>> handleNotFound(NotFoundException ex) {
        return buildResponse(ex.getMessage(), HttpStatus.NOT_FOUND, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<MessageResponseDTO<Object>> handleValidation(MethodArgumentNotValidException ex) {
        String errors = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(this::formatFieldError)
                .collect(Collectors.joining("; "));
        MessageResponseDTO<Object> body = MessageResponseDTO.builder()
                .success(false)
                .message(resolveMessage("validation.error", "Validation failed"))
                .data(null)
                .errors(errors)
                .errorCode(HttpStatus.BAD_REQUEST.value())
                .responseTime(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<MessageResponseDTO<Object>> handleUnexpected(Exception ex) {
        MessageResponseDTO<Object> body = MessageResponseDTO.builder()
                .success(false)
                .message(resolveMessage("common.error.unexpected", "Unexpected server error"))
                .data(null)
                .errors(ex.getMessage())
                .errorCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .responseTime(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    private ResponseEntity<MessageResponseDTO<Object>> buildResponse(String keyOrMessage,
                                                                     HttpStatus status,
                                                                     Object errors) {
        String message = resolveMessage(keyOrMessage, keyOrMessage);
        MessageResponseDTO<Object> body = MessageResponseDTO.builder()
                .success(false)
                .message(message)
                .data(null)
                .errors(errors)
                .errorCode(status.value())
                .responseTime(LocalDateTime.now())
                .build();
        return ResponseEntity.status(status).body(body);
    }

    private String resolveMessage(String key, String defaultMsg) {
        return messageSource.getMessage(key, null, defaultMsg, LocaleContextHolder.getLocale());
    }

    private String formatFieldError(FieldError error) {
        return error.getField() + ": " + error.getDefaultMessage();
    }
}
