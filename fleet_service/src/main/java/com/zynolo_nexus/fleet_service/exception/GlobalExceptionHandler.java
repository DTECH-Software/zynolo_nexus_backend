package com.zynolo_nexus.fleet_service.exception;

import com.zynolo_nexus.fleet_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.fleet_service.service.support.ResponseMessageHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final ResponseMessageHelper messages;

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<MessageResponseDTO<Object>> handleUnreadable(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest().body(MessageResponseDTO.builder()
                .success(false)
                .message(messages.get("common.error.unreadable"))
                .data(null)
                .errors(ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : null)
                .errorCode(400)
                .responseTime(LocalDateTime.now())
                .build());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<MessageResponseDTO<Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        return ResponseEntity.badRequest().body(MessageResponseDTO.builder()
                .success(false)
                .message(messages.get("common.error.validation"))
                .data(null)
                .errors(errors)
                .errorCode(400)
                .responseTime(LocalDateTime.now())
                .build());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<MessageResponseDTO<Object>> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(MessageResponseDTO.builder()
                .success(false)
                .message(ex.getMessage())
                .data(null)
                .errors(null)
                .errorCode(404)
                .responseTime(LocalDateTime.now())
                .build());
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<MessageResponseDTO<Object>> handleBadRequest(BadRequestException ex) {
        return ResponseEntity.badRequest().body(MessageResponseDTO.builder()
                .success(false)
                .message(ex.getMessage())
                .data(null)
                .errors(null)
                .errorCode(400)
                .responseTime(LocalDateTime.now())
                .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<MessageResponseDTO<Object>> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(MessageResponseDTO.builder()
                .success(false)
                .message(messages.get("common.error.unexpected"))
                .data(null)
                .errors(ex.getMessage())
                .errorCode(500)
                .responseTime(LocalDateTime.now())
                .build());
    }
}
