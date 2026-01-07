package com.edouard.back_resto.service;

import com.edouard.back_resto.mapper.CriteriaMapper;
import com.edouard.back_resto.model.dto.CriteriaDto;
import com.edouard.back_resto.repository.CriteriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CriteriaService {

    private final CriteriaMapper criteriaMapper;
    private final CriteriaRepository criteriaRepository;

    public List<CriteriaDto> findAll() {
        return criteriaRepository.findAll()
                .stream()
                .map(criteriaMapper::toDto)
                .toList();
    }

}
