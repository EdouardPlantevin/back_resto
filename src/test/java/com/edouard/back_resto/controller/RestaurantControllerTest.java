package com.edouard.back_resto.controller;

import com.edouard.back_resto.model.dto.RestaurantDto;
import com.edouard.back_resto.model.request.RestaurantRequest;
import com.edouard.back_resto.service.RestaurantService;
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
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@DisplayName("Tests unitaires pour RestaurantController")
class RestaurantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RestaurantService restaurantService;

    @Autowired
    private ObjectMapper objectMapper;

    private RestaurantRequest restaurantRequest;
    private RestaurantDto restaurantDto;
    private List<RestaurantDto> restaurantList;

    @BeforeEach
    void setUp() {
        restaurantRequest = new RestaurantRequest(
            "Test Restaurant",
            "123 Test St",
            "A test restaurant",
            "1234567890",
            1L
        );

        restaurantDto = new RestaurantDto(
            1L,
            new Date(),
            "Test Restaurant",
            "123 Test St",
            "A test restaurant",
            "1234567890"
        );

        restaurantList = Arrays.asList(restaurantDto);
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("findAll - Devrait retourner 200 avec la liste des restaurants")
    void findAll_ShouldReturn200WithRestaurantList() throws Exception {
        // Given
        when(restaurantService.findAll(anyLong())).thenReturn(restaurantList);

        // When & Then
        mockMvc.perform(get("/api/restaurants/")
                .param("squadId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Test Restaurant"));

        verify(restaurantService, times(1)).findAll(1L);
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("findAll - Devrait retourner 200 avec une liste vide")
    void findAll_ShouldReturn200WithEmptyList() throws Exception {
        // Given
        when(restaurantService.findAll(anyLong())).thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/api/restaurants/")
                .param("squadId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(restaurantService, times(1)).findAll(1L);
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("create - Devrait retourner 200 avec succès")
    void create_ShouldReturn200Successfully() throws Exception {
        // Given
        doNothing().when(restaurantService).create(any(RestaurantRequest.class));

        // When & Then
        mockMvc.perform(post("/api/restaurants/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(restaurantRequest)))
                .andExpect(status().isOk());

        verify(restaurantService, times(1)).create(any(RestaurantRequest.class));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("create - Devrait retourner 400 si la requête est invalide")
    void create_ShouldReturn400WhenInvalidRequest() throws Exception {
        // Given - name est vide et squadId est null (invalide)
        RestaurantRequest invalidRequest = new RestaurantRequest("", "123 Test St", "Description", "1234567890", null);

        // When & Then
        mockMvc.perform(post("/api/restaurants/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(restaurantService, never()).create(any(RestaurantRequest.class));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("delete - Devrait retourner 200 avec succès")
    void delete_ShouldReturn200Successfully() throws Exception {
        // Given
        doNothing().when(restaurantService).delete(anyLong());

        // When & Then
        mockMvc.perform(delete("/api/restaurants/1"))
                .andExpect(status().isOk());

        verify(restaurantService, times(1)).delete(1L);
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("delete - Devrait retourner 400 si l'ID est invalide")
    void delete_ShouldReturn400WhenInvalidId() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/restaurants/abc"))
                .andExpect(status().isBadRequest());

        verify(restaurantService, never()).delete(anyLong());
    }
}

