package com.edouard.back_resto.controller;

import com.edouard.back_resto.model.request.SquadRequest;
import com.edouard.back_resto.service.SquadService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@DisplayName("Tests unitaires pour SquadController")
class SquadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SquadService squadService;

    @Autowired
    private ObjectMapper objectMapper;

    private SquadRequest squadRequest;

    @BeforeEach
    void setUp() {
        squadRequest = new SquadRequest("Test Squad");
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("createSquad - Devrait retourner 200 avec message de succès")
    void createSquad_ShouldReturn200WithSuccessMessage() throws Exception {
        // Given
        doNothing().when(squadService).createSquad(any(SquadRequest.class));

        // When & Then
        mockMvc.perform(post("/api/squad/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(squadRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Squad created successfully"));

        verify(squadService, times(1)).createSquad(any(SquadRequest.class));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("createSquad - Devrait retourner 400 si la requête est invalide")
    void createSquad_ShouldReturn400WhenInvalidRequest() throws Exception {
        // Given
        SquadRequest invalidRequest = new SquadRequest("");

        // When & Then
        mockMvc.perform(post("/api/squad/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(squadService, never()).createSquad(any(SquadRequest.class));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("joinSquad - Devrait retourner 200 avec message de succès")
    void joinSquad_ShouldReturn200WithSuccessMessage() throws Exception {
        // Given
        doNothing().when(squadService).joinSquad(anyString());

        // When & Then
        mockMvc.perform(post("/api/squad/join")
                .param("codeJoin", "ABC12345"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Squad joined successfully"));

        verify(squadService, times(1)).joinSquad("ABC12345");
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("leaveSquad - Devrait retourner 200 avec message de succès")
    void leaveSquad_ShouldReturn200WithSuccessMessage() throws Exception {
        // Given
        doNothing().when(squadService).leaveSquad(anyLong());

        // When & Then
        mockMvc.perform(post("/api/squad/leave")
                .param("squadId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Squad leaved successfully"));

        verify(squadService, times(1)).leaveSquad(1L);
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("deleteSquad - Devrait retourner 200 avec message de succès")
    void deleteSquad_ShouldReturn200WithSuccessMessage() throws Exception {
        // Given
        doNothing().when(squadService).deleteSquad(anyLong());

        // When & Then
        mockMvc.perform(delete("/api/squad/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Squad deleted successfully"));

        verify(squadService, times(1)).deleteSquad(1L);
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("deleteSquad - Devrait retourner 400 si l'ID est invalide")
    void deleteSquad_ShouldReturn400WhenInvalidId() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/squad/abc"))
                .andExpect(status().isBadRequest());

        verify(squadService, never()).deleteSquad(anyLong());
    }
}

