package com.edouard.back_resto.service;

import com.edouard.back_resto.entity.Criteria;
import com.edouard.back_resto.mapper.CriteriaMapper;
import com.edouard.back_resto.model.dto.CriteriaDto;
import com.edouard.back_resto.repository.CriteriaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires pour CriteriaService")
class CriteriaServiceTest {

    @Mock
    private CriteriaMapper criteriaMapper;

    @Mock
    private CriteriaRepository criteriaRepository;

    @InjectMocks
    private CriteriaService criteriaService;

    private Criteria criteria1;
    private Criteria criteria2;
    private CriteriaDto criteriaDto1;
    private CriteriaDto criteriaDto2;

    @BeforeEach
    void setUp() {
        criteria1 = new Criteria();
        criteria1.setId(1L);
        criteria1.setName("Qualité");

        criteria2 = new Criteria();
        criteria2.setId(2L);
        criteria2.setName("Prix");

        criteriaDto1 = new CriteriaDto(1L, "Qualité");
        criteriaDto2 = new CriteriaDto(2L, "Prix");
    }

    @Test
    @DisplayName("findAll - Devrait retourner tous les critères")
    void findAll_ShouldReturnAllCriteria() {
        // Given
        List<Criteria> criteriaList = Arrays.asList(criteria1, criteria2);
        when(criteriaRepository.findAll()).thenReturn(criteriaList);
        when(criteriaMapper.toDto(criteria1)).thenReturn(criteriaDto1);
        when(criteriaMapper.toDto(criteria2)).thenReturn(criteriaDto2);

        // When
        List<CriteriaDto> result = criteriaService.findAll();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(criteriaDto1, result.get(0));
        assertEquals(criteriaDto2, result.get(1));
        verify(criteriaRepository, times(1)).findAll();
        verify(criteriaMapper, times(1)).toDto(criteria1);
        verify(criteriaMapper, times(1)).toDto(criteria2);
    }

    @Test
    @DisplayName("findAll - Devrait retourner une liste vide si aucun critère")
    void findAll_ShouldReturnEmptyList() {
        // Given
        when(criteriaRepository.findAll()).thenReturn(List.of());

        // When
        List<CriteriaDto> result = criteriaService.findAll();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(criteriaRepository, times(1)).findAll();
        verify(criteriaMapper, never()).toDto(any(Criteria.class));
    }
}


