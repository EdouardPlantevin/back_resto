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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires pour CustomUserDetailsService")
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setUsername("testuser");
        testUser.setPassword("encodedPassword");
        testUser.setRole("ROLE_USER");
    }

    @Test
    @DisplayName("loadUserByUsername - Devrait charger un utilisateur avec succès")
    void loadUserByUsername_ShouldLoadUserSuccessfully() {
        // Given
        String email = "test@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));

        // When
        UserDetails result = customUserDetailsService.loadUserByUsername(email);

        // Then
        assertNotNull(result);
        assertEquals(testUser.getEmail(), result.getUsername());
        assertEquals(testUser.getPassword(), result.getPassword());
        assertTrue(result.getAuthorities().stream()
            .anyMatch(auth -> auth.getAuthority().equals("ROLE_USER")));
        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    @DisplayName("loadUserByUsername - Devrait échouer si l'utilisateur n'existe pas")
    void loadUserByUsername_ShouldFailWhenUserNotFound() {
        // Given
        String email = "unknown@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When & Then
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, 
            () -> customUserDetailsService.loadUserByUsername(email));
        
        assertTrue(exception.getMessage().contains("User not found with email : " + email));
        verify(userRepository, times(1)).findByEmail(email);
    }
}


