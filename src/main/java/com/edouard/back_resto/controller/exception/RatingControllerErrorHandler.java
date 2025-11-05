package com.edouard.back_resto.controller.exception;

import com.edouard.back_resto.exception.RestaurantNotFoundException;
import com.edouard.back_resto.exception.UserUnauthorizeException;
import com.edouard.back_resto.model.response.ApiError;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class RatingControllerErrorHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidationException(MethodArgumentNotValidException ex) {
        var errors = new HashMap<String, String>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return errors;
    }

    @ExceptionHandler(RestaurantNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleRestaurantNotFoundException(RestaurantNotFoundException ex) {
        return new ApiError(ex.getMessage());
    }

    @ExceptionHandler(UserUnauthorizeException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiError handleUserUnauthorizeException(UserUnauthorizeException ex) {
        return new ApiError(ex.getMessage());
    }

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleEntityNotFoundException(EntityNotFoundException ex) {
        return new ApiError(ex.getMessage());
    }

}
