package com.edouard.back_resto.service;

import com.edouard.back_resto.configuration.JwtUtils;
import com.edouard.back_resto.entity.RefreshToken;
import com.edouard.back_resto.entity.User;
import com.edouard.back_resto.repository.RefreshTokenRepository;
import com.edouard.back_resto.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires pour RefreshTokenService")
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    private User testUser;
    private RefreshToken refreshToken;

    @BeforeEach
    void setUp() {
        // Set refresh token expiration via reflection
        ReflectionTestUtils.setField(refreshTokenService, "refreshTokenExpiration", 3600L);

        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setUsername("testuser");

        refreshToken = new RefreshToken();
        refreshToken.setId(1L);
        refreshToken.setToken("refreshToken123");
        refreshToken.setUser(testUser);
        refreshToken.setExpiryDate(Instant.now().plusSeconds(3600));
        refreshToken.setRevoked(false);
    }

    @Test
    @DisplayName("createRefreshToken - Devrait créer un nouveau refresh token")
    void createRefreshToken_ShouldCreateNewToken() {
        // Given
        String email = "test@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(jwtUtils.generateRefreshToken(email)).thenReturn("newRefreshToken123");
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> {
            RefreshToken token = invocation.getArgument(0);
            token.setId(1L);
            return token;
        });

        // When
        RefreshToken result = refreshTokenService.createRefreshToken(email);

        // Then
        assertNotNull(result);
        assertEquals("newRefreshToken123", result.getToken());
        assertEquals(testUser, result.getUser());
        verify(userRepository, times(1)).findByEmail(email);
        verify(refreshTokenRepository, times(1)).deleteByUser(testUser);
        verify(jwtUtils, times(1)).generateRefreshToken(email);
        verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("createRefreshToken - Devrait échouer si l'utilisateur n'existe pas")
    void createRefreshToken_ShouldFailWhenUserNotFound() {
        // Given
        String email = "unknown@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> refreshTokenService.createRefreshToken(email));
        
        assertTrue(exception.getMessage().contains("User not found: " + email));
        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("verifyRefreshToken - Devrait vérifier un token valide")
    void verifyRefreshToken_ShouldVerifyValidToken() {
        // Given
        String token = "refreshToken123";
        when(refreshTokenRepository.findByToken(token)).thenReturn(Optional.of(refreshToken));
        when(jwtUtils.validateToken(token)).thenReturn(true);
        when(jwtUtils.isRefreshToken(token)).thenReturn(true);

        // When
        assertDoesNotThrow(() -> refreshTokenService.verifyRefreshToken(token));

        // Then
        verify(refreshTokenRepository, times(1)).findByToken(token);
        verify(jwtUtils, times(1)).validateToken(token);
        verify(jwtUtils, times(1)).isRefreshToken(token);
        verify(refreshTokenRepository, never()).delete(any(RefreshToken.class));
    }

    @Test
    @DisplayName("verifyRefreshToken - Devrait échouer si le token n'existe pas")
    void verifyRefreshToken_ShouldFailWhenTokenNotFound() {
        // Given
        String token = "invalidToken";
        when(refreshTokenRepository.findByToken(token)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> refreshTokenService.verifyRefreshToken(token));
        
        assertEquals("Refresh token not found", exception.getMessage());
        verify(jwtUtils, never()).validateToken(anyString());
    }

    @Test
    @DisplayName("verifyRefreshToken - Devrait échouer si le token est invalide")
    void verifyRefreshToken_ShouldFailWhenTokenInvalid() {
        // Given
        String token = "refreshToken123";
        refreshToken.setRevoked(true); // Token révoqué
        when(refreshTokenRepository.findByToken(token)).thenReturn(Optional.of(refreshToken));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> refreshTokenService.verifyRefreshToken(token));
        
        assertTrue(exception.getMessage().contains("Refresh token is expired or revoked"));
        verify(refreshTokenRepository, times(1)).delete(refreshToken);
    }

    @Test
    @DisplayName("verifyRefreshToken - Devrait échouer si le token JWT est invalide")
    void verifyRefreshToken_ShouldFailWhenJwtInvalid() {
        // Given
        String token = "refreshToken123";
        when(refreshTokenRepository.findByToken(token)).thenReturn(Optional.of(refreshToken));
        when(jwtUtils.validateToken(token)).thenReturn(false);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> refreshTokenService.verifyRefreshToken(token));
        
        assertEquals("Refresh token is expired or revoked", exception.getMessage());
        verify(refreshTokenRepository, times(1)).delete(refreshToken);
    }

    @Test
    @DisplayName("verifyRefreshToken - Devrait échouer si ce n'est pas un refresh token")
    void verifyRefreshToken_ShouldFailWhenNotRefreshToken() {
        // Given
        String token = "refreshToken123";
        when(refreshTokenRepository.findByToken(token)).thenReturn(Optional.of(refreshToken));
        when(jwtUtils.validateToken(token)).thenReturn(true);
        when(jwtUtils.isRefreshToken(token)).thenReturn(false);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> refreshTokenService.verifyRefreshToken(token));
        
        assertEquals("Refresh token is expired or revoked", exception.getMessage());
        verify(refreshTokenRepository, times(1)).delete(refreshToken);
    }

    @Test
    @DisplayName("revokeRefreshToken - Devrait révoquer un token existant")
    void revokeRefreshToken_ShouldRevokeExistingToken() {
        // Given
        String token = "refreshToken123";
        when(refreshTokenRepository.findByToken(token)).thenReturn(Optional.of(refreshToken));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(refreshToken);

        // When
        refreshTokenService.revokeRefreshToken(token);

        // Then
        assertTrue(refreshToken.isRevoked());
        verify(refreshTokenRepository, times(1)).findByToken(token);
        verify(refreshTokenRepository, times(1)).save(refreshToken);
    }

    @Test
    @DisplayName("revokeRefreshToken - Ne devrait rien faire si le token n'existe pas")
    void revokeRefreshToken_ShouldDoNothingWhenTokenNotFound() {
        // Given
        String token = "invalidToken";
        when(refreshTokenRepository.findByToken(token)).thenReturn(Optional.empty());

        // When
        assertDoesNotThrow(() -> refreshTokenService.revokeRefreshToken(token));

        // Then
        verify(refreshTokenRepository, times(1)).findByToken(token);
        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
    }
}


