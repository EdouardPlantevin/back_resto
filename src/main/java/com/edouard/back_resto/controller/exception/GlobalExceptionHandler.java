package com.edouard.back_resto.controller.exception;

import com.edouard.back_resto.model.response.MessageResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<MessageResponse> handleRuntimeException(RuntimeException e) {
        return ResponseEntity
                .badRequest()
                .body(new MessageResponse(e.getMessage()));
    }
}
