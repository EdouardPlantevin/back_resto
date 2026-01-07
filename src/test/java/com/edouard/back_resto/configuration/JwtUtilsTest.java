package com.edouard.back_resto.configuration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;
import java.util.Collections;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires pour JwtUtils")
class JwtUtilsTest {

    @InjectMocks
    private JwtUtils jwtUtils;

    private String testEmail;
    private String validSecretKey;
    private long accessTokenExpiration;
    private long refreshTokenExpiration;

    @BeforeEach
    void setUp() {
        testEmail = "test@example.com";
        // Générer une clé secrète valide en Base64 (minimum 256 bits pour HS256)
        validSecretKey = Base64.getEncoder().encodeToString(
            "test-secret-key-for-testing-purposes-only-very-long-key-required".getBytes()
        );
        accessTokenExpiration = 3600L; // 1 heure
        refreshTokenExpiration = 86400L; // 24 heures

        // Injecter les valeurs via ReflectionTestUtils
        ReflectionTestUtils.setField(jwtUtils, "secretKey", validSecretKey);
        ReflectionTestUtils.setField(jwtUtils, "accessTokenExpiration", accessTokenExpiration);
        ReflectionTestUtils.setField(jwtUtils, "refreshTokenExpiration", refreshTokenExpiration);
    }

    @Test
    @DisplayName("generateAccessToken - Devrait générer un access token valide")
    void generateAccessToken_ShouldGenerateValidToken() {
        // When
        String token = jwtUtils.generateAccessToken(testEmail);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertEquals(testEmail, jwtUtils.extractEmail(token));
        assertTrue(jwtUtils.isAccessToken(token));
        assertFalse(jwtUtils.isRefreshToken(token));
    }

    @Test
    @DisplayName("generateRefreshToken - Devrait générer un refresh token valide")
    void generateRefreshToken_ShouldGenerateValidToken() {
        // When
        String token = jwtUtils.generateRefreshToken(testEmail);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertEquals(testEmail, jwtUtils.extractEmail(token));
        assertTrue(jwtUtils.isRefreshToken(token));
        assertFalse(jwtUtils.isAccessToken(token));
    }

    @Test
    @DisplayName("extractEmail - Devrait extraire l'email d'un token valide")
    void extractEmail_ShouldExtractEmailFromToken() {
        // Given
        String token = jwtUtils.generateAccessToken(testEmail);

        // When
        String extractedEmail = jwtUtils.extractEmail(token);

        // Then
        assertEquals(testEmail, extractedEmail);
    }

    @Test
    @DisplayName("extractTokenType - Devrait extraire le type de token")
    void extractTokenType_ShouldExtractTokenType() {
        // Given
        String accessToken = jwtUtils.generateAccessToken(testEmail);
        String refreshToken = jwtUtils.generateRefreshToken(testEmail);

        // When
        String accessTokenType = jwtUtils.extractTokenType(accessToken);
        String refreshTokenType = jwtUtils.extractTokenType(refreshToken);

        // Then
        assertEquals("access", accessTokenType);
        assertEquals("refresh", refreshTokenType);
    }

    @Test
    @DisplayName("isAccessToken - Devrait retourner true pour un access token")
    void isAccessToken_ShouldReturnTrueForAccessToken() {
        // Given
        String accessToken = jwtUtils.generateAccessToken(testEmail);
        String refreshToken = jwtUtils.generateRefreshToken(testEmail);

        // When & Then
        assertTrue(jwtUtils.isAccessToken(accessToken));
        assertFalse(jwtUtils.isAccessToken(refreshToken));
    }

    @Test
    @DisplayName("isRefreshToken - Devrait retourner true pour un refresh token")
    void isRefreshToken_ShouldReturnTrueForRefreshToken() {
        // Given
        String accessToken = jwtUtils.generateAccessToken(testEmail);
        String refreshToken = jwtUtils.generateRefreshToken(testEmail);

        // When & Then
        assertTrue(jwtUtils.isRefreshToken(refreshToken));
        assertFalse(jwtUtils.isRefreshToken(accessToken));
    }

    @Test
    @DisplayName("validateToken - Devrait valider un token valide")
    void validateToken_ShouldValidateValidToken() {
        // Given
        String token = jwtUtils.generateAccessToken(testEmail);

        // When
        boolean isValid = jwtUtils.validateToken(token);

        // Then
        assertTrue(isValid);
    }

    @Test
    @DisplayName("validateToken - Devrait valider un token avec UserDetails valide")
    void validateToken_ShouldValidateTokenWithValidUserDetails() {
        // Given
        String token = jwtUtils.generateAccessToken(testEmail);
        UserDetails userDetails = User.builder()
                .username(testEmail)
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        // When
        boolean isValid = jwtUtils.validateToken(token, userDetails);

        // Then
        assertTrue(isValid);
    }

    @Test
    @DisplayName("validateToken - Devrait échouer si l'email ne correspond pas")
    void validateToken_ShouldFailWhenEmailDoesNotMatch() {
        // Given
        String token = jwtUtils.generateAccessToken(testEmail);
        UserDetails userDetails = User.builder()
                .username("other@example.com")
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        // When
        boolean isValid = jwtUtils.validateToken(token, userDetails);

        // Then
        assertFalse(isValid);
    }

    @Test
    @DisplayName("validateToken - Devrait échouer pour un token expiré")
    void validateToken_ShouldFailForExpiredToken() throws InterruptedException {
        // Given - Créer un token avec une expiration très courte
        ReflectionTestUtils.setField(jwtUtils, "accessTokenExpiration", -1000L); // 1 seconde
        String token = jwtUtils.generateAccessToken(testEmail);

        // When & Then - Le token expiré doit lever une exception lors de la validation
        assertThrows(Exception.class, () -> jwtUtils.validateToken(token));
        
        // Restaurer la valeur originale
        ReflectionTestUtils.setField(jwtUtils, "accessTokenExpiration", accessTokenExpiration);
    }

    @Test
    @DisplayName("getAccessTokenExpiration - Devrait retourner la durée d'expiration")
    void getAccessTokenExpiration_ShouldReturnExpirationTime() {
        // When
        long expiration = jwtUtils.getAccessTokenExpiration();

        // Then
        assertEquals(accessTokenExpiration, expiration);
    }

    @Test
    @DisplayName("extractEmail - Devrait lever une exception pour un token invalide")
    void extractEmail_ShouldThrowExceptionForInvalidToken() {
        // Given
        String invalidToken = "invalid.token.here";

        // When & Then
        assertThrows(Exception.class, () -> jwtUtils.extractEmail(invalidToken));
    }

    @Test
    @DisplayName("validateToken - Devrait échouer pour un token avec une signature invalide")
    void validateToken_ShouldFailForTokenWithInvalidSignature() {
        // Given - Créer un token avec une autre clé secrète
        String otherSecretKey = Base64.getEncoder().encodeToString(
            "different-secret-key-for-testing-purposes-only".getBytes()
        );
        ReflectionTestUtils.setField(jwtUtils, "secretKey", otherSecretKey);
        String tokenWithDifferentKey = jwtUtils.generateAccessToken(testEmail);
        
        // Restaurer la clé originale
        ReflectionTestUtils.setField(jwtUtils, "secretKey", validSecretKey);

        // When & Then
        assertThrows(Exception.class, () -> jwtUtils.validateToken(tokenWithDifferentKey));
    }

    @Test
    @DisplayName("generateAccessToken - Devrait générer des tokens valides pour le même email")
    void generateAccessToken_ShouldGenerateValidTokens() {
        // When
        String token1 = jwtUtils.generateAccessToken(testEmail);
        String token2 = jwtUtils.generateAccessToken(testEmail);

        // Then - Les deux tokens doivent être valides et contenir le même email
        assertNotNull(token1);
        assertNotNull(token2);
        assertEquals(testEmail, jwtUtils.extractEmail(token1));
        assertEquals(testEmail, jwtUtils.extractEmail(token2));
        assertTrue(jwtUtils.isAccessToken(token1));
        assertTrue(jwtUtils.isAccessToken(token2));
        // Note: Les tokens peuvent être identiques s'ils sont générés dans la même milliseconde
        // mais ils sont tous deux valides
    }
}

