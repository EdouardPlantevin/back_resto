package com.edouard.back_resto.filter;

import com.edouard.back_resto.configuration.JwtUtils;
import com.edouard.back_resto.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.Base64;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires pour JwtFilter")
class JwtFilterTest {

    @Mock
    private CustomUserDetailsService customUserDetailsService;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtFilter jwtFilter;

    private String testEmail;
    private String validAccessToken;
    private String validRefreshToken;
    private UserDetails userDetails;
    private String validSecretKey;

    @BeforeEach
    void setUp() {
        // Nettoyer le SecurityContext avant chaque test
        SecurityContextHolder.clearContext();

        testEmail = "test@example.com";
        validSecretKey = Base64.getEncoder().encodeToString(
            "test-secret-key-for-testing-purposes-only-very-long-key-required".getBytes()
        );

        // Créer une instance réelle de JwtUtils pour générer des tokens valides
        JwtUtils realJwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(realJwtUtils, "secretKey", validSecretKey);
        ReflectionTestUtils.setField(realJwtUtils, "accessTokenExpiration", 3600L);
        ReflectionTestUtils.setField(realJwtUtils, "refreshTokenExpiration", 86400L);

        validAccessToken = realJwtUtils.generateAccessToken(testEmail);
        validRefreshToken = realJwtUtils.generateRefreshToken(testEmail);

        userDetails = User.builder()
                .username(testEmail)
                .password("password")
                .authorities(Collections.emptyList())
                .build();
    }

    @Test
    @DisplayName("doFilterInternal - Devrait authentifier avec un access token valide")
    void doFilterInternal_ShouldAuthenticateWithValidAccessToken() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validAccessToken);
        when(jwtUtils.extractEmail(validAccessToken)).thenReturn(testEmail);
        when(jwtUtils.isAccessToken(validAccessToken)).thenReturn(true);
        when(customUserDetailsService.loadUserByUsername(testEmail)).thenReturn(userDetails);
        when(jwtUtils.validateToken(validAccessToken, userDetails)).thenReturn(true);

        // When
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(jwtUtils, times(1)).extractEmail(validAccessToken);
        verify(jwtUtils, times(1)).isAccessToken(validAccessToken);
        verify(customUserDetailsService, times(1)).loadUserByUsername(testEmail);
        verify(jwtUtils, times(1)).validateToken(validAccessToken, userDetails);
        verify(filterChain, times(1)).doFilter(request, response);

        // Vérifier que l'authentification a été définie dans le SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertEquals(testEmail, authentication.getName());
    }

    @Test
    @DisplayName("doFilterInternal - Ne devrait pas authentifier sans header Authorization")
    void doFilterInternal_ShouldNotAuthenticateWithoutAuthorizationHeader() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn(null);

        // When
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(jwtUtils, never()).extractEmail(anyString());
        verify(customUserDetailsService, never()).loadUserByUsername(anyString());
        verify(filterChain, times(1)).doFilter(request, response);

        // Vérifier que l'authentification n'a pas été définie
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication);
    }

    @Test
    @DisplayName("doFilterInternal - Ne devrait pas authentifier avec un header invalide")
    void doFilterInternal_ShouldNotAuthenticateWithInvalidHeader() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn("InvalidHeader token");

        // When
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(jwtUtils, never()).extractEmail(anyString());
        verify(customUserDetailsService, never()).loadUserByUsername(anyString());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("doFilterInternal - Ne devrait pas authentifier avec un refresh token")
    void doFilterInternal_ShouldNotAuthenticateWithRefreshToken() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validRefreshToken);
        when(jwtUtils.extractEmail(validRefreshToken)).thenReturn(testEmail);
        when(jwtUtils.isAccessToken(validRefreshToken)).thenReturn(false);

        // When
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(jwtUtils, times(1)).extractEmail(validRefreshToken);
        verify(jwtUtils, times(1)).isAccessToken(validRefreshToken);
        verify(customUserDetailsService, never()).loadUserByUsername(anyString());
        verify(filterChain, times(1)).doFilter(request, response);

        // Vérifier que l'authentification n'a pas été définie
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication);
    }

    @Test
    @DisplayName("doFilterInternal - Ne devrait pas authentifier si le token est invalide")
    void doFilterInternal_ShouldNotAuthenticateWithInvalidToken() throws ServletException, IOException {
        // Given
        String invalidToken = "invalid.token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + invalidToken);
        when(jwtUtils.extractEmail(invalidToken)).thenReturn(testEmail);
        when(jwtUtils.isAccessToken(invalidToken)).thenReturn(true);
        when(customUserDetailsService.loadUserByUsername(testEmail)).thenReturn(userDetails);
        when(jwtUtils.validateToken(invalidToken, userDetails)).thenReturn(false);

        // When
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(jwtUtils, times(1)).extractEmail(invalidToken);
        verify(jwtUtils, times(1)).isAccessToken(invalidToken);
        verify(customUserDetailsService, times(1)).loadUserByUsername(testEmail);
        verify(jwtUtils, times(1)).validateToken(invalidToken, userDetails);
        verify(filterChain, times(1)).doFilter(request, response);

        // Vérifier que l'authentification n'a pas été définie
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication);
    }

    @Test
    @DisplayName("doFilterInternal - Ne devrait pas authentifier si l'authentification existe déjà")
    void doFilterInternal_ShouldNotAuthenticateIfAuthenticationAlreadyExists() throws ServletException, IOException {
        // Given
        Authentication existingAuth = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(existingAuth);
        SecurityContextHolder.setContext(securityContext);

        when(request.getHeader("Authorization")).thenReturn("Bearer " + validAccessToken);
        when(jwtUtils.extractEmail(validAccessToken)).thenReturn(testEmail);
        // Note: isAccessToken ne sera pas appelé car l'authentification existe déjà

        // When
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(jwtUtils, times(1)).extractEmail(validAccessToken);
        verify(jwtUtils, never()).isAccessToken(anyString()); // Ne devrait pas être appelé
        verify(customUserDetailsService, never()).loadUserByUsername(anyString());
        verify(filterChain, times(1)).doFilter(request, response);

        // Nettoyer
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("doFilterInternal - Ne devrait pas authentifier si l'email est null")
    void doFilterInternal_ShouldNotAuthenticateIfEmailIsNull() throws ServletException, IOException {
        // Given
        String invalidToken = "invalid.token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + invalidToken);
        when(jwtUtils.extractEmail(invalidToken)).thenReturn(null);

        // When
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(jwtUtils, times(1)).extractEmail(invalidToken);
        verify(jwtUtils, never()).isAccessToken(anyString());
        verify(customUserDetailsService, never()).loadUserByUsername(anyString());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("doFilterInternal - Devrait toujours appeler filterChain.doFilter")
    void doFilterInternal_ShouldAlwaysCallFilterChain() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn(null);

        // When
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("doFilterInternal - Devrait gérer les exceptions lors de l'extraction de l'email")
    void doFilterInternal_ShouldHandleExceptionWhenExtractingEmail() throws ServletException, IOException {
        // Given
        String invalidToken = "invalid.token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + invalidToken);
        when(jwtUtils.extractEmail(invalidToken)).thenThrow(new RuntimeException("Invalid token"));

        // When & Then
        assertThrows(RuntimeException.class, () -> 
            jwtFilter.doFilterInternal(request, response, filterChain)
        );
    }
}

