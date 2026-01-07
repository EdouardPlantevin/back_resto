package com.edouard.back_resto.mapper;

import com.edouard.back_resto.entity.Criteria;
import com.edouard.back_resto.model.dto.CriteriaDto;
import org.springframework.stereotype.Component;

@Component
public class CriteriaMapper {
    public CriteriaDto toDto(Criteria criteria) {
        return new CriteriaDto(
                criteria.getId(),
                criteria.getName()
        );
    }
}