package com.edouard.back_resto.service;

import com.edouard.back_resto.configuration.JwtUtils;
import com.edouard.back_resto.entity.RefreshToken;
import com.edouard.back_resto.entity.User;
import com.edouard.back_resto.model.request.LoginRequest;
import com.edouard.back_resto.model.request.RegisterRequest;
import com.edouard.back_resto.model.response.AuthResponse;
import com.edouard.back_resto.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires pour AuthService")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private RefreshToken refreshToken;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setUsername("testuser");
        testUser.setPassword("encodedPassword");
        testUser.setRole("ROLE_USER");

        registerRequest = new RegisterRequest("test@example.com", "testuser", "password123");
        loginRequest = new LoginRequest("test@example.com", "password123");

        refreshToken = new RefreshToken();
        refreshToken.setToken("refreshToken123");
        refreshToken.setUser(testUser);
    }

    @Test
    @DisplayName("register - Devrait enregistrer un nouvel utilisateur avec succès")
    void register_ShouldRegisterUserSuccessfully() {
        // Given
        when(userRepository.findByEmail(registerRequest.email())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(registerRequest.password())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        // When
        User result = authService.register(registerRequest);

        // Then
        assertNotNull(result);
        assertEquals(registerRequest.email(), result.getEmail());
        assertEquals(registerRequest.username(), result.getUsername());
        assertEquals("ROLE_USER", result.getRole());
        verify(userRepository, times(1)).findByEmail(registerRequest.email());
        verify(passwordEncoder, times(1)).encode(registerRequest.password());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("register - Devrait échouer si l'email est déjà pris")
    void register_ShouldFailWhenEmailAlreadyTaken() {
        // Given
        when(userRepository.findByEmail(registerRequest.email())).thenReturn(Optional.of(testUser));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> authService.register(registerRequest));
        
        assertEquals("Email is already taken!", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("login - Devrait authentifier un utilisateur avec succès")
    void login_ShouldAuthenticateUserSuccessfully() {
        // Given
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
        when(jwtUtils.generateAccessToken(loginRequest.email())).thenReturn("accessToken123");
        when(refreshTokenService.createRefreshToken(loginRequest.email())).thenReturn(refreshToken);
        when(jwtUtils.getAccessTokenExpiration()).thenReturn(3600L);

        // When
        AuthResponse result = authService.login(loginRequest);

        // Then
        assertNotNull(result);
        assertEquals("accessToken123", result.accessToken());
        assertEquals("refreshToken123", result.refreshToken());
        assertEquals("Bearer", result.tokenType());
        assertEquals(3600L, result.expiresIn());
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtils, times(1)).generateAccessToken(loginRequest.email());
        verify(refreshTokenService, times(1)).createRefreshToken(loginRequest.email());
    }

    @Test
    @DisplayName("login - Devrait échouer si l'authentification échoue")
    void login_ShouldFailWhenAuthenticationFails() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenThrow(new BadCredentialsException("Bad credentials"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> authService.login(loginRequest));
        
        assertEquals("Invalid username or password : test@example.com", exception.getMessage());
        verify(jwtUtils, never()).generateAccessToken(anyString());
        verify(refreshTokenService, never()).createRefreshToken(anyString());
    }

    @Test
    @DisplayName("login - Devrait échouer si l'authentification n'est pas authentifiée")
    void login_ShouldFailWhenNotAuthenticated() {
        // Given
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(false);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> authService.login(loginRequest));
        
        assertEquals("Invalid username or password : test@example.com", exception.getMessage());
        verify(jwtUtils, never()).generateAccessToken(anyString());
    }

    @Test
    @DisplayName("refreshToken - Devrait rafraîchir le token avec succès")
    void refreshToken_ShouldRefreshTokenSuccessfully() {
        // Given
        String refreshTokenString = "refreshToken123";
        RefreshToken newRefreshToken = new RefreshToken();
        newRefreshToken.setToken("newRefreshToken123");

        when(jwtUtils.extractEmail(refreshTokenString)).thenReturn("test@example.com");
        when(jwtUtils.generateAccessToken("test@example.com")).thenReturn("newAccessToken123");
        when(refreshTokenService.createRefreshToken("test@example.com")).thenReturn(newRefreshToken);
        when(jwtUtils.getAccessTokenExpiration()).thenReturn(3600L);

        // When
        AuthResponse result = authService.refreshToken(refreshTokenString);

        // Then
        assertNotNull(result);
        assertEquals("newAccessToken123", result.accessToken());
        assertEquals("newRefreshToken123", result.refreshToken());
        assertEquals("Bearer", result.tokenType());
        verify(refreshTokenService, times(1)).verifyRefreshToken(refreshTokenString);
        verify(jwtUtils, times(1)).extractEmail(refreshTokenString);
        verify(jwtUtils, times(1)).generateAccessToken("test@example.com");
        verify(refreshTokenService, times(1)).createRefreshToken("test@example.com");
    }

    @Test
    @DisplayName("logout - Devrait révoquer le refresh token")
    void logout_ShouldRevokeRefreshToken() {
        // Given
        String refreshTokenString = "refreshToken123";

        // When
        authService.logout(refreshTokenString);

        // Then
        verify(refreshTokenService, times(1)).revokeRefreshToken(refreshTokenString);
    }
}


