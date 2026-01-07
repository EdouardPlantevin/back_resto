package com.edouard.back_resto.service;

import com.edouard.back_resto.entity.Restaurant;
import com.edouard.back_resto.entity.Squad;
import com.edouard.back_resto.entity.User;
import com.edouard.back_resto.mapper.RestaurantMapper;
import com.edouard.back_resto.model.dto.RestaurantDto;
import com.edouard.back_resto.model.request.RestaurantRequest;
import com.edouard.back_resto.repository.RestaurantRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires pour RestaurantService")
class RestaurantServiceTest {

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private SquadRepository squadRepository;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private RestaurantMapper restaurantMapper;

    @InjectMocks
    private RestaurantService restaurantService;

    private User testUser;
    private Squad testSquad;
    private Restaurant testRestaurant;
    private RestaurantRequest restaurantRequest;
    private RestaurantDto restaurantDto;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");

        testSquad = new Squad();
        testSquad.setId(1L);
        testSquad.setName("Test Squad");
        testSquad.setUsers(new HashSet<>(Arrays.asList(testUser)));

        testRestaurant = new Restaurant();
        testRestaurant.setId(1L);
        testRestaurant.setName("Test Restaurant");
        testRestaurant.setAddress("123 Test St");
        testRestaurant.setDescription("A test restaurant");
        testRestaurant.setPhone("1234567890");
        testRestaurant.setSquad(testSquad);

        restaurantRequest = new RestaurantRequest(
            "Test Restaurant",
            "123 Test St",
            "A test restaurant",
            "1234567890",
            1L
        );

        restaurantDto = new RestaurantDto(
            1L,
            testRestaurant.getCreatedAt(),
            "Test Restaurant",
            "123 Test St",
            "A test restaurant",
            "1234567890"
        );
    }

    @Test
    @DisplayName("findAll - Devrait retourner tous les restaurants d'une squad")
    void findAll_ShouldReturnAllRestaurants() {
        // Given
        Long squadId = 1L;
        List<Restaurant> restaurants = Arrays.asList(testRestaurant);
        when(currentUserService.getCurrentUser()).thenReturn(testUser);
        when(squadRepository.findByIdAndUsers(squadId, Set.of(testUser)))
            .thenReturn(Optional.of(testSquad));
        when(restaurantRepository.findAllBySquad(testSquad)).thenReturn(restaurants);
        when(restaurantMapper.toDto(testRestaurant)).thenReturn(restaurantDto);

        // When
        List<RestaurantDto> result = restaurantService.findAll(squadId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(restaurantDto, result.get(0));
        verify(currentUserService, times(1)).getCurrentUser();
        verify(squadRepository, times(1)).findByIdAndUsers(squadId, Set.of(testUser));
        verify(restaurantRepository, times(1)).findAllBySquad(testSquad);
        verify(restaurantMapper, times(1)).toDto(testRestaurant);
    }

    @Test
    @DisplayName("findAll - Devrait échouer si la squad n'existe pas ou l'utilisateur n'est pas dedans")
    void findAll_ShouldFailWhenSquadNotFoundOrUserNotInSquad() {
        // Given
        Long squadId = 999L;
        when(currentUserService.getCurrentUser()).thenReturn(testUser);
        when(squadRepository.findByIdAndUsers(squadId, Set.of(testUser)))
            .thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> restaurantService.findAll(squadId));
        
        assertTrue(exception.getMessage().contains("Squad not found or User not in this squad"));
        verify(restaurantRepository, never()).findAllBySquad(any());
    }

    @Test
    @DisplayName("create - Devrait créer un restaurant avec succès")
    void create_ShouldCreateRestaurantSuccessfully() {
        // Given
        when(squadRepository.findById(restaurantRequest.squadId()))
            .thenReturn(Optional.of(testSquad));
        when(restaurantRepository.findByNameAndSquad(restaurantRequest.name(), testSquad))
            .thenReturn(Optional.empty());
        when(restaurantRepository.save(any(Restaurant.class))).thenAnswer(invocation -> {
            Restaurant restaurant = invocation.getArgument(0);
            restaurant.setId(1L);
            return restaurant;
        });

        // When
        assertDoesNotThrow(() -> restaurantService.create(restaurantRequest));

        // Then
        verify(squadRepository, times(1)).findById(restaurantRequest.squadId());
        verify(restaurantRepository, times(1))
            .findByNameAndSquad(restaurantRequest.name(), testSquad);
        verify(restaurantRepository, times(1)).save(any(Restaurant.class));
    }

    @Test
    @DisplayName("create - Devrait échouer si la squad n'existe pas")
    void create_ShouldFailWhenSquadNotFound() {
        // Given
        when(squadRepository.findById(restaurantRequest.squadId()))
            .thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> restaurantService.create(restaurantRequest));
        
        assertEquals("Squad not found", exception.getMessage());
        verify(restaurantRepository, never()).save(any(Restaurant.class));
    }

    @Test
    @DisplayName("create - Devrait échouer si le restaurant existe déjà")
    void create_ShouldFailWhenRestaurantAlreadyExists() {
        // Given
        when(squadRepository.findById(restaurantRequest.squadId()))
            .thenReturn(Optional.of(testSquad));
        when(restaurantRepository.findByNameAndSquad(restaurantRequest.name(), testSquad))
            .thenReturn(Optional.of(testRestaurant));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> restaurantService.create(restaurantRequest));
        
        assertEquals("Restaurant already exists : Test Restaurant", exception.getMessage());
        verify(restaurantRepository, never()).save(any(Restaurant.class));
    }

    @Test
    @DisplayName("delete - Devrait supprimer un restaurant avec succès")
    void delete_ShouldDeleteRestaurantSuccessfully() {
        // Given
        Long restaurantId = 1L;
        when(currentUserService.getCurrentUser()).thenReturn(testUser);
        when(restaurantRepository.findById(restaurantId))
            .thenReturn(Optional.of(testRestaurant));

        // When
        assertDoesNotThrow(() -> restaurantService.delete(restaurantId));

        // Then
        verify(currentUserService, times(1)).getCurrentUser();
        verify(restaurantRepository, times(1)).findById(restaurantId);
        verify(restaurantRepository, times(1)).delete(testRestaurant);
    }

    @Test
    @DisplayName("delete - Devrait échouer si le restaurant n'existe pas")
    void delete_ShouldFailWhenRestaurantNotFound() {
        // Given
        Long restaurantId = 999L;
        when(currentUserService.getCurrentUser()).thenReturn(testUser);
        when(restaurantRepository.findById(restaurantId))
            .thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> restaurantService.delete(restaurantId));
        
        assertEquals("Restaurant not found", exception.getMessage());
        verify(restaurantRepository, never()).delete(any(Restaurant.class));
    }

    @Test
    @DisplayName("delete - Devrait échouer si l'utilisateur n'est pas dans la squad")
    void delete_ShouldFailWhenUserNotInSquad() {
        // Given
        Long restaurantId = 1L;
        User otherUser = new User();
        otherUser.setId(2L);
        testSquad.setUsers(new HashSet<>(Arrays.asList(otherUser)));

        when(currentUserService.getCurrentUser()).thenReturn(testUser);
        when(restaurantRepository.findById(restaurantId))
            .thenReturn(Optional.of(testRestaurant));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> restaurantService.delete(restaurantId));
        
        assertEquals("L'utilisateur n'est pas autorisé à effectuer cette action : Supprimer ce restaurant", exception.getMessage());
        verify(restaurantRepository, never()).delete(any(Restaurant.class));
    }
}


