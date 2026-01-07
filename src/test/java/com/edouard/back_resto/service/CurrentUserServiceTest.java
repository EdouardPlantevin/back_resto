package com.edouard.back_resto.service;

import com.edouard.back_resto.entity.User;
import com.edouard.back_resto.repository.UserRepository;
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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires pour CurrentUserService")
class CurrentUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private CurrentUserService currentUserService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setUsername("testuser");
        testUser.setPassword("password");
        testUser.setRole("ROLE_USER");
    }

    @Test
    @DisplayName("getCurrentUser - Devrait retourner l'utilisateur authentifié")
    void getCurrentUser_ShouldReturnAuthenticatedUser() {
        // Given
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // When
        User result = currentUserService.getCurrentUser();

        // Then
        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        verify(securityContext, times(1)).getAuthentication();
        verify(userRepository, times(1)).findByEmail("test@example.com");
    }

    @Test
    @DisplayName("getCurrentUser - Devrait échouer si aucun utilisateur n'est authentifié")
    void getCurrentUser_ShouldFailWhenNoAuthentication() {
        // Given
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(null);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> currentUserService.getCurrentUser());
        
        assertEquals("No authenticated user found", exception.getMessage());
        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    @DisplayName("getCurrentUser - Devrait échouer si l'authentification n'est pas authentifiée")
    void getCurrentUser_ShouldFailWhenNotAuthenticated() {
        // Given
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> currentUserService.getCurrentUser());
        
        assertEquals("No authenticated user found", exception.getMessage());
        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    @DisplayName("getCurrentUser - Devrait échouer si l'utilisateur n'est pas trouvé")
    void getCurrentUser_ShouldFailWhenUserNotFound() {
        // Given
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("unknown@example.com");
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> currentUserService.getCurrentUser());
        
        assertTrue(exception.getMessage().contains("User not found with email: unknown@example.com"));
        verify(userRepository, times(1)).findByEmail("unknown@example.com");
    }

    @Test
    @DisplayName("getCurrentUserEmail - Devrait retourner l'email de l'utilisateur authentifié")
    void getCurrentUserEmail_ShouldReturnEmail() {
        // Given
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("test@example.com");

        // When
        String result = currentUserService.getCurrentUserEmail();

        // Then
        assertEquals("test@example.com", result);
        verify(securityContext, times(1)).getAuthentication();
    }

    @Test
    @DisplayName("getCurrentUserEmail - Devrait échouer si aucun utilisateur n'est authentifié")
    void getCurrentUserEmail_ShouldFailWhenNoAuthentication() {
        // Given
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(null);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> currentUserService.getCurrentUserEmail());
        
        assertEquals("No authenticated user found", exception.getMessage());
    }

    @Test
    @DisplayName("getCurrentUserEmail - Devrait échouer si l'authentification n'est pas authentifiée")
    void getCurrentUserEmail_ShouldFailWhenNotAuthenticated() {
        // Given
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> currentUserService.getCurrentUserEmail());
        
        assertEquals("No authenticated user found", exception.getMessage());
    }
}


