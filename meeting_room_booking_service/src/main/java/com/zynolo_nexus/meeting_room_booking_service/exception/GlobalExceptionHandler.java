package com.zynolo_nexus.meeting_room_booking_service.exception;

import com.zynolo_nexus.meeting_room_booking_service.dto.api.MessageResponseDTO;
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
        return ResponseEntity.badRequest().body(error("Validation failed", errors, 400));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<MessageResponseDTO<Object>> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error(ex.getMessage(), null, 404));
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<MessageResponseDTO<Object>> handleBadRequest(BadRequestException ex) {
        return ResponseEntity.badRequest().body(error(ex.getMessage(), null, 400));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<MessageResponseDTO<Object>> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(error("Unexpected server error", ex.getMessage(), 500));
    }

    private MessageResponseDTO<Object> error(String message, Object errors, int errorCode) {
        return MessageResponseDTO.builder()
                .success(false)
                .message(message)
                .data(null)
                .errors(errors)
                .errorCode(errorCode)
                .responseTime(LocalDateTime.now())
                .build();
    }
}
