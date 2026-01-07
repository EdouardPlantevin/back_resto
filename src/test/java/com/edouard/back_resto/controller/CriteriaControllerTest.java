package com.edouard.back_resto.controller;

import com.edouard.back_resto.model.dto.CriteriaDto;
import com.edouard.back_resto.service.CriteriaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@DisplayName("Tests unitaires pour CriteriaController")
class CriteriaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CriteriaService criteriaService;

    private List<CriteriaDto> criteriaList;

    @BeforeEach
    void setUp() {
        CriteriaDto criteria1 = new CriteriaDto(1L, "Qualité");
        CriteriaDto criteria2 = new CriteriaDto(2L, "Prix");
        criteriaList = Arrays.asList(criteria1, criteria2);
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("findAll - Devrait retourner 200 avec la liste des critères")
    void findAll_ShouldReturn200WithCriteriaList() throws Exception {
        // Given
        when(criteriaService.findAll()).thenReturn(criteriaList);

        // When & Then
        mockMvc.perform(get("/api/criteria"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Qualité"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].name").value("Prix"));

        verify(criteriaService, times(1)).findAll();
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("findAll - Devrait retourner 200 avec une liste vide")
    void findAll_ShouldReturn200WithEmptyList() throws Exception {
        // Given
        when(criteriaService.findAll()).thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/api/criteria"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(criteriaService, times(1)).findAll();
    }
}

