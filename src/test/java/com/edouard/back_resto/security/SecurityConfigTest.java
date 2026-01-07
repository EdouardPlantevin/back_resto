package com.edouard.back_resto.security;

import com.edouard.back_resto.service.RestaurantService;
import com.edouard.back_resto.model.request.RegisterRequest;
import com.edouard.back_resto.model.request.LoginRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Tests de sécurité pour les endpoints")
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RestaurantService restaurantService;

    @Test
    @DisplayName("Les endpoints /api/auth/* devraient être accessibles sans authentification")
    void authEndpoints_ShouldBeAccessibleWithoutAuthentication() throws Exception {
        // Given
        RegisterRequest registerRequest = new RegisterRequest("test@example.com", "testuser", "password123");
        LoginRequest loginRequest = new LoginRequest("test@example.com", "password123");

        // When & Then - Register endpoint
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assertTrue(status == 200 || status == 400, 
                        "Status should be 200 or 400, not 401/403. Got: " + status);
                });

        // When & Then - Login endpoint
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assertTrue(status == 200 || status == 400, 
                        "Status should be 200 or 400, not 401/403. Got: " + status);
                });
    }

    @Test
    @DisplayName("Les endpoints Swagger devraient être accessibles sans authentification")
    void swaggerEndpoints_ShouldBeAccessibleWithoutAuthentication() throws Exception {
        // When & Then
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assertTrue(status == 200 || status == 404, 
                        "Status should be 200 or 404, not 401/403. Got: " + status);
                });

        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assertTrue(status == 200 || status == 404, 
                        "Status should be 200 or 404, not 401/403. Got: " + status);
                });
    }

    @Test
    @DisplayName("Les endpoints protégés devraient nécessiter une authentification")
    void protectedEndpoints_ShouldRequireAuthentication() throws Exception {
        // Note: Maintenant .anyRequest().authenticated() est activé
        // Ce test vérifie que les endpoints protégés retournent 401/403 sans authentification
        when(restaurantService.findAll(anyLong())).thenReturn(List.of());
        
        // When & Then - Restaurant endpoint (GET avec paramètre) sans authentification
        // Devrait retourner 401 ou 403 car l'endpoint nécessite une authentification
        mockMvc.perform(get("/api/restaurants/")
                .param("squadId", "1"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    // Devrait être 401 (Unauthorized) ou 403 (Forbidden) sans authentification
                    assertTrue(status == 401 || status == 403, 
                        "Status should be 401 or 403 (unauthorized/forbidden) without authentication. Got: " + status);
                });
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(username = "test@example.com")
    @DisplayName("Les endpoints protégés devraient être accessibles avec authentification")
    void protectedEndpoints_ShouldBeAccessibleWithAuthentication() throws Exception {
        // Note: Maintenant .anyRequest().authenticated() est activé
        // Ce test vérifie que les endpoints protégés sont accessibles avec authentification
        when(restaurantService.findAll(anyLong())).thenReturn(List.of());
        
        // When & Then - Restaurant endpoint (GET avec paramètre) avec authentification
        mockMvc.perform(get("/api/restaurants/")
                .param("squadId", "1"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    // Devrait être 200 (OK) avec authentification
                    assertTrue(status == 200 || status == 400 || status == 500, 
                        "Status should be 200, 400, or 500 with authentication, not 401/403. Got: " + status);
                });
    }
}

