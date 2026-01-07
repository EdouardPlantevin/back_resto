package com.edouard.back_resto.service;

import com.edouard.back_resto.entity.Criteria;
import com.edouard.back_resto.entity.Rating;
import com.edouard.back_resto.entity.Restaurant;
import com.edouard.back_resto.entity.Squad;
import com.edouard.back_resto.entity.User;
import com.edouard.back_resto.exception.RestaurantNotFoundException;
import com.edouard.back_resto.exception.UserUnauthorizeException;
import com.edouard.back_resto.model.request.RatingRequest;
import com.edouard.back_resto.repository.CriteriaRepository;
import com.edouard.back_resto.repository.RatingRepository;
import com.edouard.back_resto.repository.RestaurantRepository;
import jakarta.persistence.EntityNotFoundException;
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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires pour RatingService")
class RatingServiceTest {

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private RatingRepository ratingRepository;

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private CriteriaRepository criteriaRepository;

    @InjectMocks
    private RatingService ratingService;

    private User testUser;
    private Squad testSquad;
    private Restaurant testRestaurant;
    private Criteria criteria1;
    private Criteria criteria2;
    private RatingRequest ratingRequest1;
    private RatingRequest ratingRequest2;

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
        testRestaurant.setSquad(testSquad);

        criteria1 = new Criteria();
        criteria1.setId(1L);
        criteria1.setName("Qualité");

        criteria2 = new Criteria();
        criteria2.setId(2L);
        criteria2.setName("Prix");

        ratingRequest1 = new RatingRequest(8, 1L, 1L);
        ratingRequest2 = new RatingRequest(7, 1L, 2L);
    }

    @Test
    @DisplayName("createOrUpdate - Devrait créer de nouveaux ratings")
    void createOrUpdate_ShouldCreateNewRatings() {
        // Given
        List<RatingRequest> requests = Arrays.asList(ratingRequest1, ratingRequest2);
        when(currentUserService.getCurrentUser()).thenReturn(testUser);
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(testRestaurant));
        when(criteriaRepository.findAllById(any())).thenReturn(Arrays.asList(criteria1, criteria2));
        when(ratingRepository.findByRestaurantAndUser(testRestaurant, testUser)).thenReturn(Collections.emptyList());
        when(ratingRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        assertDoesNotThrow(() -> ratingService.createOrUpdate(requests));

        // Then
        verify(currentUserService, times(1)).getCurrentUser();
        verify(restaurantRepository, times(1)).findById(1L);
        verify(criteriaRepository, times(1)).findAllById(any());
        verify(ratingRepository, times(1)).findByRestaurantAndUser(testRestaurant, testUser);
        verify(ratingRepository, times(2)).saveAll(anyList());
    }

    @Test
    @DisplayName("createOrUpdate - Devrait mettre à jour des ratings existants")
    void createOrUpdate_ShouldUpdateExistingRatings() {
        // Given
        List<RatingRequest> requests = Arrays.asList(ratingRequest1);
        Rating existingRating = new Rating();
        existingRating.setId(1L);
        existingRating.setRestaurant(testRestaurant);
        existingRating.setUser(testUser);
        existingRating.setCriteria(criteria1);
        existingRating.setScore(5);

        when(currentUserService.getCurrentUser()).thenReturn(testUser);
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(testRestaurant));
        when(criteriaRepository.findAllById(any())).thenReturn(Arrays.asList(criteria1));
        when(ratingRepository.findByRestaurantAndUser(testRestaurant, testUser))
            .thenReturn(Arrays.asList(existingRating));
        when(ratingRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        assertDoesNotThrow(() -> ratingService.createOrUpdate(requests));

        // Then
        assertEquals(8, existingRating.getScore());
        verify(ratingRepository, times(1)).saveAll(anyList());
    }

    @Test
    @DisplayName("createOrUpdate - Ne devrait rien faire si la liste est vide")
    void createOrUpdate_ShouldDoNothingWhenListEmpty() {
        // When
        assertDoesNotThrow(() -> ratingService.createOrUpdate(Collections.emptyList()));

        // Then
        verify(currentUserService, never()).getCurrentUser();
        verify(restaurantRepository, never()).findById(any());
    }

    @Test
    @DisplayName("createOrUpdate - Ne devrait rien faire si la liste est null")
    void createOrUpdate_ShouldDoNothingWhenListNull() {
        // When
        assertDoesNotThrow(() -> ratingService.createOrUpdate(null));

        // Then
        verify(currentUserService, never()).getCurrentUser();
        verify(restaurantRepository, never()).findById(any());
    }

    @Test
    @DisplayName("createOrUpdate - Devrait échouer si le restaurant n'existe pas")
    void createOrUpdate_ShouldFailWhenRestaurantNotFound() {
        // Given
        List<RatingRequest> requests = Arrays.asList(ratingRequest1);
        when(currentUserService.getCurrentUser()).thenReturn(testUser);
        when(restaurantRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        RestaurantNotFoundException exception = assertThrows(RestaurantNotFoundException.class, 
            () -> ratingService.createOrUpdate(requests));
        
        assertTrue(exception.getMessage().contains("Restaurant with id 1 not found"));
        verify(criteriaRepository, never()).findAllById(any());
    }

    @Test
    @DisplayName("createOrUpdate - Devrait échouer si l'utilisateur n'est pas dans la squad")
    void createOrUpdate_ShouldFailWhenUserNotInSquad() {
        // Given
        List<RatingRequest> requests = Arrays.asList(ratingRequest1);
        User otherUser = new User();
        otherUser.setId(2L);
        testSquad.setUsers(new HashSet<>(Arrays.asList(otherUser)));

        when(currentUserService.getCurrentUser()).thenReturn(testUser);
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(testRestaurant));

        // When & Then
        UserUnauthorizeException exception = assertThrows(UserUnauthorizeException.class, 
            () -> ratingService.createOrUpdate(requests));
        
        assertTrue(exception.getMessage().contains("Vous ne faites pas partie de la squad de ce restaurant"));
        verify(criteriaRepository, never()).findAllById(any());
    }

    @Test
    @DisplayName("createOrUpdate - Devrait échouer si un critère n'existe pas")
    void createOrUpdate_ShouldFailWhenCriteriaNotFound() {
        // Given
        List<RatingRequest> requests = Arrays.asList(ratingRequest1);
        when(currentUserService.getCurrentUser()).thenReturn(testUser);
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(testRestaurant));
        when(criteriaRepository.findAllById(any())).thenReturn(Collections.emptyList());

        // When & Then
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, 
            () -> ratingService.createOrUpdate(requests));
        
        assertTrue(exception.getMessage().contains("Critéria not found: 1"));
    }
}


