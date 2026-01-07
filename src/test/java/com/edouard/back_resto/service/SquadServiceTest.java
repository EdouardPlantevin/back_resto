package com.edouard.back_resto.service;

import com.edouard.back_resto.entity.Squad;
import com.edouard.back_resto.entity.User;
import com.edouard.back_resto.model.request.SquadRequest;
import com.edouard.back_resto.repository.SquadRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires pour SquadService")
class SquadServiceTest {

    @Mock
    private SquadRepository squadRepository;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private SquadService squadService;

    private User testUser;
    private Squad testSquad;
    private SquadRequest squadRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setUsername("testuser");
        testUser.setSquads(new HashSet<>());

        testSquad = new Squad();
        testSquad.setId(1L);
        testSquad.setName("Test Squad");
        testSquad.setCodeJoin("ABC12345");
        testSquad.setLeader(testUser);
        testSquad.setUsers(new HashSet<>());

        squadRequest = new SquadRequest("Test Squad");
    }

    @Test
    @DisplayName("createSquad - Devrait créer une squad avec succès")
    void createSquad_ShouldCreateSquadSuccessfully() {
        // Given
        when(currentUserService.getCurrentUser()).thenReturn(testUser);
        when(squadRepository.findByCodeJoin(anyString())).thenReturn(Optional.empty());
        when(squadRepository.save(any(Squad.class))).thenAnswer(invocation -> {
            Squad squad = invocation.getArgument(0);
            squad.setId(1L);
            return squad;
        });

        // When
        assertDoesNotThrow(() -> squadService.createSquad(squadRequest));

        // Then
        verify(currentUserService, times(1)).getCurrentUser();
        verify(squadRepository, atLeastOnce()).findByCodeJoin(anyString());
        verify(squadRepository, times(1)).save(any(Squad.class));
        assertEquals(1, testUser.getSquads().size());
    }

    @Test
    @DisplayName("createSquad - Devrait échouer si l'utilisateur a déjà 3 squads")
    void createSquad_ShouldFailWhenUserHasMaxSquads() {
        // Given
        Set<Squad> existingSquads = new HashSet<>();
        for (int i = 0; i < 3; i++) {
            Squad squad = new Squad();
            squad.setId((long) i);
            existingSquads.add(squad);
        }
        testUser.setSquads(existingSquads);

        when(currentUserService.getCurrentUser()).thenReturn(testUser);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> squadService.createSquad(squadRequest));
        
        assertTrue(exception.getMessage().contains("You can't create/join more than 3 squads"));
        verify(squadRepository, never()).save(any(Squad.class));
    }

    @Test
    @DisplayName("joinSquad - Devrait rejoindre une squad avec succès")
    void joinSquad_ShouldJoinSquadSuccessfully() {
        // Given
        String codeJoin = "ABC12345";
        when(currentUserService.getCurrentUser()).thenReturn(testUser);
        when(squadRepository.findByCodeJoin(codeJoin)).thenReturn(Optional.of(testSquad));
        when(squadRepository.save(any(Squad.class))).thenReturn(testSquad);

        // When
        assertDoesNotThrow(() -> squadService.joinSquad(codeJoin));

        // Then
        verify(currentUserService, times(1)).getCurrentUser();
        verify(squadRepository, times(1)).findByCodeJoin(codeJoin);
        verify(squadRepository, times(1)).save(testSquad);
        assertTrue(testSquad.getUsers().contains(testUser));
        assertTrue(testUser.getSquads().contains(testSquad));
    }

    @Test
    @DisplayName("joinSquad - Devrait échouer si la squad n'existe pas")
    void joinSquad_ShouldFailWhenSquadNotFound() {
        // Given
        String codeJoin = "INVALID";
        when(currentUserService.getCurrentUser()).thenReturn(testUser);
        when(squadRepository.findByCodeJoin(codeJoin)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> squadService.joinSquad(codeJoin));
        
        assertTrue(exception.getMessage().contains("Squad not found"));
        verify(squadRepository, never()).save(any(Squad.class));
    }

    @Test
    @DisplayName("joinSquad - Devrait échouer si l'utilisateur est déjà dans la squad")
    void joinSquad_ShouldFailWhenUserAlreadyInSquad() {
        // Given
        String codeJoin = "ABC12345";
        testSquad.getUsers().add(testUser);
        testUser.getSquads().add(testSquad);

        when(currentUserService.getCurrentUser()).thenReturn(testUser);
        when(squadRepository.findByCodeJoin(codeJoin)).thenReturn(Optional.of(testSquad));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> squadService.joinSquad(codeJoin));
        
        assertTrue(exception.getMessage().contains("You are already in this squad"));
        verify(squadRepository, never()).save(any(Squad.class));
    }

    @Test
    @DisplayName("joinSquad - Devrait échouer si l'utilisateur a déjà 3 squads")
    void joinSquad_ShouldFailWhenUserHasMaxSquads() {
        // Given
        String codeJoin = "ABC12345";
        Set<Squad> existingSquads = new HashSet<>();
        for (int i = 0; i < 3; i++) {
            Squad squad = new Squad();
            squad.setId((long) i);
            existingSquads.add(squad);
        }
        testUser.setSquads(existingSquads);

        when(currentUserService.getCurrentUser()).thenReturn(testUser);
        when(squadRepository.findByCodeJoin(codeJoin)).thenReturn(Optional.of(testSquad));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> squadService.joinSquad(codeJoin));
        
        assertTrue(exception.getMessage().contains("You can't create/join more than 3 squads"));
        verify(squadRepository, never()).save(any(Squad.class));
    }

    @Test
    @DisplayName("leaveSquad - Devrait quitter une squad avec succès")
    void leaveSquad_ShouldLeaveSquadSuccessfully() {
        // Given
        Long squadId = 1L;
        testSquad.getUsers().add(testUser);
        testUser.getSquads().add(testSquad);

        // Ajouter un autre utilisateur pour que la squad ne soit pas vide
        User otherUser = new User();
        otherUser.setId(2L);
        testSquad.getUsers().add(otherUser);

        when(currentUserService.getCurrentUser()).thenReturn(testUser);
        when(squadRepository.findById(squadId)).thenReturn(Optional.of(testSquad));
        when(squadRepository.save(any(Squad.class))).thenReturn(testSquad);

        // When
        assertDoesNotThrow(() -> squadService.leaveSquad(squadId));

        // Then
        verify(currentUserService, times(1)).getCurrentUser();
        verify(squadRepository, times(1)).findById(squadId);
        verify(squadRepository, times(1)).save(testSquad);
        verify(squadRepository, never()).delete(any(Squad.class));
        assertFalse(testSquad.getUsers().contains(testUser));
        assertFalse(testUser.getSquads().contains(testSquad));
    }

    @Test
    @DisplayName("leaveSquad - Devrait supprimer la squad si elle devient vide")
    void leaveSquad_ShouldDeleteSquadWhenEmpty() {
        // Given
        Long squadId = 1L;
        testSquad.getUsers().add(testUser);
        testUser.getSquads().add(testSquad);

        when(currentUserService.getCurrentUser()).thenReturn(testUser);
        when(squadRepository.findById(squadId)).thenReturn(Optional.of(testSquad));

        // When
        assertDoesNotThrow(() -> squadService.leaveSquad(squadId));

        // Then
        verify(squadRepository, times(1)).findById(squadId);
        verify(squadRepository, times(1)).delete(testSquad);
        verify(squadRepository, never()).save(any(Squad.class));
        assertTrue(testSquad.getUsers().isEmpty());
    }

    @Test
    @DisplayName("leaveSquad - Devrait échouer si la squad n'existe pas")
    void leaveSquad_ShouldFailWhenSquadNotFound() {
        // Given
        Long squadId = 999L;
        when(currentUserService.getCurrentUser()).thenReturn(testUser);
        when(squadRepository.findById(squadId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> squadService.leaveSquad(squadId));
        
        assertTrue(exception.getMessage().contains("Squad not found"));
        verify(squadRepository, never()).save(any(Squad.class));
        verify(squadRepository, never()).delete(any(Squad.class));
    }

    @Test
    @DisplayName("leaveSquad - Devrait échouer si l'utilisateur n'est pas dans la squad")
    void leaveSquad_ShouldFailWhenUserNotInSquad() {
        // Given
        Long squadId = 1L;
        when(currentUserService.getCurrentUser()).thenReturn(testUser);
        when(squadRepository.findById(squadId)).thenReturn(Optional.of(testSquad));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> squadService.leaveSquad(squadId));
        
        assertTrue(exception.getMessage().contains("User is not in this squad"));
        verify(squadRepository, never()).save(any(Squad.class));
        verify(squadRepository, never()).delete(any(Squad.class));
    }

    @Test
    @DisplayName("deleteSquad - Devrait supprimer une squad avec succès")
    void deleteSquad_ShouldDeleteSquadSuccessfully() {
        // Given
        Long squadId = 1L;
        testSquad.setLeader(testUser);
        testSquad.setUsers(new HashSet<>());
        testSquad.getUsers().add(testUser);
        testUser.setSquads(new HashSet<>());
        testUser.getSquads().add(testSquad);

        // Ajouter un autre utilisateur
        User otherUser = new User();
        otherUser.setId(2L);
        otherUser.setSquads(new HashSet<>());
        testSquad.getUsers().add(otherUser);
        otherUser.getSquads().add(testSquad);

        when(currentUserService.getCurrentUser()).thenReturn(testUser);
        when(squadRepository.findById(squadId)).thenReturn(Optional.of(testSquad));
        doNothing().when(squadRepository).delete(any(Squad.class));

        // When
        assertDoesNotThrow(() -> squadService.deleteSquad(squadId));

        // Then
        verify(currentUserService, times(1)).getCurrentUser();
        verify(squadRepository, times(1)).findById(squadId);
        verify(squadRepository, times(1)).delete(testSquad);
    }

    @Test
    @DisplayName("deleteSquad - Devrait échouer si la squad n'existe pas")
    void deleteSquad_ShouldFailWhenSquadNotFound() {
        // Given
        Long squadId = 999L;
        when(currentUserService.getCurrentUser()).thenReturn(testUser);
        when(squadRepository.findById(squadId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> squadService.deleteSquad(squadId));
        
        assertTrue(exception.getMessage().contains("Squad not found"));
        verify(squadRepository, never()).delete(any(Squad.class));
    }

    @Test
    @DisplayName("deleteSquad - Devrait échouer si l'utilisateur n'est pas le leader")
    void deleteSquad_ShouldFailWhenUserIsNotLeader() {
        // Given
        Long squadId = 1L;
        User otherLeader = new User();
        otherLeader.setId(2L);
        testSquad.setLeader(otherLeader);

        when(currentUserService.getCurrentUser()).thenReturn(testUser);
        when(squadRepository.findById(squadId)).thenReturn(Optional.of(testSquad));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> squadService.deleteSquad(squadId));
        
        assertTrue(exception.getMessage().contains("User is not the leader of this squad"));
        verify(squadRepository, never()).delete(any(Squad.class));
    }

    @Test
    @DisplayName("createSquad - Devrait générer un code unique")
    void createSquad_ShouldGenerateUniqueCode() {
        // Given
        when(currentUserService.getCurrentUser()).thenReturn(testUser);
        when(squadRepository.findByCodeJoin(anyString())).thenReturn(Optional.empty());
        when(squadRepository.save(any(Squad.class))).thenAnswer(invocation -> {
            Squad squad = invocation.getArgument(0);
            squad.setId(1L);
            return squad;
        });

        // When
        squadService.createSquad(squadRequest);

        // Then
        verify(squadRepository, atLeastOnce()).findByCodeJoin(anyString());
        // Vérifier que le code généré a la bonne longueur
        Squad savedSquad = testUser.getSquads().iterator().next();
        assertNotNull(savedSquad.getCodeJoin());
        assertEquals(8, savedSquad.getCodeJoin().length());
    }

    @Test
    @DisplayName("createSquad - Devrait régénérer le code si collision")
    void createSquad_ShouldRegenerateCodeOnCollision() {
        // Given
        when(currentUserService.getCurrentUser()).thenReturn(testUser);
        // Premier appel retourne un code existant, deuxième retourne vide (code unique)
        when(squadRepository.findByCodeJoin(anyString()))
            .thenReturn(Optional.of(new Squad())) // Collision
            .thenReturn(Optional.empty()); // Code unique
        when(squadRepository.save(any(Squad.class))).thenAnswer(invocation -> {
            Squad squad = invocation.getArgument(0);
            squad.setId(1L);
            return squad;
        });

        // When
        assertDoesNotThrow(() -> squadService.createSquad(squadRequest));

        // Then
        verify(squadRepository, atLeast(2)).findByCodeJoin(anyString());
    }
}


