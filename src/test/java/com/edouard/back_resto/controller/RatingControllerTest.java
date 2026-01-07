package com.edouard.back_resto.controller;

import com.edouard.back_resto.model.request.RatingRequest;
import com.edouard.back_resto.service.RatingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@DisplayName("Tests unitaires pour RatingController")
class RatingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RatingService ratingService;

    @Autowired
    private ObjectMapper objectMapper;

    private List<RatingRequest> ratingRequests;

    @BeforeEach
    void setUp() {
        RatingRequest request1 = new RatingRequest(8, 1L, 1L);
        RatingRequest request2 = new RatingRequest(7, 1L, 2L);
        ratingRequests = Arrays.asList(request1, request2);
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("create - Devrait retourner 200 avec succès")
    void create_ShouldReturn200Successfully() throws Exception {
        // Given
        doNothing().when(ratingService).createOrUpdate(anyList());

        // When & Then
        mockMvc.perform(post("/api/rating/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ratingRequests)))
                .andExpect(status().isOk());

        verify(ratingService, times(1)).createOrUpdate(anyList());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("create - Devrait retourner 400 si la requête est invalide")
    void create_ShouldReturn400WhenInvalidRequest() throws Exception {
        // Given
        RatingRequest invalidRequest = new RatingRequest(-1, null, null);
        List<RatingRequest> invalidRequests = Arrays.asList(invalidRequest);

        // When & Then
        mockMvc.perform(post("/api/rating/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequests)))
                .andExpect(status().isBadRequest());

        verify(ratingService, never()).createOrUpdate(anyList());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("create - Devrait retourner 400 si le score est hors limites")
    void create_ShouldReturn400WhenScoreOutOfBounds() throws Exception {
        // Given
        RatingRequest invalidRequest = new RatingRequest(11, 1L, 1L);
        List<RatingRequest> invalidRequests = Arrays.asList(invalidRequest);

        // When & Then
        mockMvc.perform(post("/api/rating/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequests)))
                .andExpect(status().isBadRequest());

        verify(ratingService, never()).createOrUpdate(anyList());
    }
}

