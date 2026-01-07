package com.edouard.back_resto.model.dto;

import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;

public record CriteriaDto(
        @Id
        Long id,

        @NotNull
        String name
) {
}
