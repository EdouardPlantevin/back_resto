package com.edouard.back_resto.controller.exception;

import com.edouard.back_resto.exception.RestaurantNotFoundException;
import com.edouard.back_resto.exception.UserUnauthorizeException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Tests unitaires pour RatingControllerErrorHandler")
class RatingControllerErrorHandlerTest {

    private RatingControllerErrorHandler errorHandler;

    @BeforeEach
    void setUp() {
        errorHandler = new RatingControllerErrorHandler();
    }

    @Test
    @DisplayName("handleValidationException - Devrait retourner 400 avec les erreurs de validation")
    void handleValidationException_ShouldReturn400WithValidationErrors() {
        // Given
        MethodArgumentNotValidException ex = createMethodArgumentNotValidException();

        // When
        var result = errorHandler.handleValidationException(ex);

        // Then
        assertNotNull(result);
        assertTrue(result.containsKey("field1"));
        assertEquals("Error message 1", result.get("field1"));
        assertTrue(result.containsKey("field2"));
        assertEquals("Error message 2", result.get("field2"));
    }

    @Test
    @DisplayName("handleRestaurantNotFoundException - Devrait retourner 404 avec message d'erreur")
    void handleRestaurantNotFoundException_ShouldReturn404WithErrorMessage() {
        // Given
        RestaurantNotFoundException ex = new RestaurantNotFoundException(1L);

        // When
        var result = errorHandler.handleRestaurantNotFoundException(ex);

        // Then
        assertNotNull(result);
        assertTrue(result.error().contains("1"));
    }

    @Test
    @DisplayName("handleUserUnauthorizeException - Devrait retourner 401 avec message d'erreur")
    void handleUserUnauthorizeException_ShouldReturn401WithErrorMessage() {
        // Given
        UserUnauthorizeException ex = new UserUnauthorizeException("Test message");

        // When
        var result = errorHandler.handleUserUnauthorizeException(ex);

        // Then
        assertNotNull(result);
        assertTrue(result.error().contains("Test message"));
    }

    @Test
    @DisplayName("handleEntityNotFoundException - Devrait retourner 404 avec message d'erreur")
    void handleEntityNotFoundException_ShouldReturn404WithErrorMessage() {
        // Given
        EntityNotFoundException ex = new EntityNotFoundException("Entity not found");

        // When
        var result = errorHandler.handleEntityNotFoundException(ex);

        // Then
        assertNotNull(result);
        assertEquals("Entity not found", result.error());
    }

    private MethodArgumentNotValidException createMethodArgumentNotValidException() {
        // Créer un mock de BindingResult avec des erreurs
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError1 = new FieldError("object", "field1", "Error message 1");
        FieldError fieldError2 = new FieldError("object", "field2", "Error message 2");
        
        when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError1, fieldError2));
        
        return new MethodArgumentNotValidException(null, bindingResult);
    }
}

