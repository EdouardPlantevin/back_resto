package com.edouard.back_resto.controller;

import com.edouard.back_resto.entity.User;
import com.edouard.back_resto.model.request.LoginRequest;
import com.edouard.back_resto.model.request.RefreshTokenRequest;
import com.edouard.back_resto.model.request.RegisterRequest;
import com.edouard.back_resto.model.response.AuthResponse;
import com.edouard.back_resto.model.response.MessageResponse;
import com.edouard.back_resto.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@DisplayName("Tests unitaires pour AuthController")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private RefreshTokenRequest refreshTokenRequest;
    private User testUser;
    private AuthResponse authResponse;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest("test@example.com", "testuser", "password123");
        loginRequest = new LoginRequest("test@example.com", "password123");
        refreshTokenRequest = new RefreshTokenRequest("refreshToken123");

        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setUsername("testuser");

        authResponse = new AuthResponse(
            "accessToken123",
            "refreshToken123",
            "Bearer",
            3600L
        );
    }

    @Test
    @DisplayName("register - Devrait retourner 200 avec l'utilisateur créé")
    void register_ShouldReturn200WithUser() throws Exception {
        // Given
        when(authService.register(any(RegisterRequest.class))).thenReturn(testUser);

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.username").value("testuser"));

        verify(authService, times(1)).register(any(RegisterRequest.class));
    }

    @Test
    @DisplayName("register - Devrait retourner 400 si l'email est déjà pris")
    void register_ShouldReturn400WhenEmailAlreadyTaken() throws Exception {
        // Given
        when(authService.register(any(RegisterRequest.class)))
            .thenThrow(new RuntimeException("Email is already taken!"));

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Email is already taken!"));

        verify(authService, times(1)).register(any(RegisterRequest.class));
    }

    @Test
    @DisplayName("register - Devrait retourner 400 si la requête est invalide")
    void register_ShouldReturn400WhenInvalidRequest() throws Exception {
        // Given
        RegisterRequest invalidRequest = new RegisterRequest("", "", "");

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).register(any(RegisterRequest.class));
    }

    @Test
    @DisplayName("login - Devrait retourner 200 avec AuthResponse")
    void login_ShouldReturn200WithAuthResponse() throws Exception {
        // Given
        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("accessToken123"))
                .andExpect(jsonPath("$.refreshToken").value("refreshToken123"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(3600L));

        verify(authService, times(1)).login(any(LoginRequest.class));
    }

    @Test
    @DisplayName("login - Devrait retourner 400 si les identifiants sont invalides")
    void login_ShouldReturn400WhenInvalidCredentials() throws Exception {
        // Given
        when(authService.login(any(LoginRequest.class)))
            .thenThrow(new RuntimeException("Invalid username or password"));

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid username or password"));

        verify(authService, times(1)).login(any(LoginRequest.class));
    }

    @Test
    @DisplayName("login - Devrait retourner 400 si la requête est invalide")
    void login_ShouldReturn400WhenInvalidRequest() throws Exception {
        // Given
        LoginRequest invalidRequest = new LoginRequest("", "");

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).login(any(LoginRequest.class));
    }

    @Test
    @DisplayName("refreshToken - Devrait retourner 200 avec nouveau AuthResponse")
    void refreshToken_ShouldReturn200WithNewAuthResponse() throws Exception {
        // Given
        AuthResponse newAuthResponse = new AuthResponse(
            "newAccessToken123",
            "newRefreshToken123",
            "Bearer",
            3600L
        );
        when(authService.refreshToken(anyString())).thenReturn(newAuthResponse);

        // When & Then
        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshTokenRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("newAccessToken123"))
                .andExpect(jsonPath("$.refreshToken").value("newRefreshToken123"));

        verify(authService, times(1)).refreshToken("refreshToken123");
    }

    @Test
    @DisplayName("refreshToken - Devrait retourner 400 si le token est invalide")
    void refreshToken_ShouldReturn400WhenTokenInvalid() throws Exception {
        // Given
        when(authService.refreshToken(anyString()))
            .thenThrow(new RuntimeException("Invalid refresh token"));

        // When & Then
        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshTokenRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid or expired refresh token"));

        verify(authService, times(1)).refreshToken("refreshToken123");
    }

    @Test
    @DisplayName("refreshToken - Devrait retourner 400 si la requête est invalide")
    void refreshToken_ShouldReturn400WhenInvalidRequest() throws Exception {
        // Given
        RefreshTokenRequest invalidRequest = new RefreshTokenRequest("");

        // When & Then
        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).refreshToken(anyString());
    }

    @Test
    @DisplayName("logout - Devrait retourner 200 avec message de succès")
    void logout_ShouldReturn200WithSuccessMessage() throws Exception {
        // Given
        doNothing().when(authService).logout(anyString());

        // When & Then
        mockMvc.perform(post("/api/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshTokenRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logout successful"));

        verify(authService, times(1)).logout("refreshToken123");
    }

    @Test
    @DisplayName("logout - Devrait retourner 400 si le logout échoue")
    void logout_ShouldReturn400WhenLogoutFails() throws Exception {
        // Given
        doThrow(new RuntimeException("Logout failed")).when(authService).logout(anyString());

        // When & Then
        mockMvc.perform(post("/api/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshTokenRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Logout failed"));

        verify(authService, times(1)).logout("refreshToken123");
    }

    @Test
    @DisplayName("logout - Devrait retourner 400 si la requête est invalide")
    void logout_ShouldReturn400WhenInvalidRequest() throws Exception {
        // Given
        RefreshTokenRequest invalidRequest = new RefreshTokenRequest("");

        // When & Then
        mockMvc.perform(post("/api/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).logout(anyString());
    }
}

