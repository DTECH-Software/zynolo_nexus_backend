package com.zynolo_nexus.po_service.exception;

import com.zynolo_nexus.po_service.dto.api.MessageResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<MessageResponseDTO<Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        return ResponseEntity.badRequest().body(MessageResponseDTO.builder()
                .success(false)
                .message("Validation failed")
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
                .message("Unexpected server error")
                .data(null)
                .errors(ex.getMessage())
                .errorCode(500)
                .responseTime(LocalDateTime.now())
                .build());
    }
}
