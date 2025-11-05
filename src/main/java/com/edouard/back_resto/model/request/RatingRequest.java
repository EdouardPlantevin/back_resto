package com.edouard.back_resto.model.request;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

public record RatingRequest(
        @NotBlank
        @Length(min = 0, max = 10)
        Integer score,

        @NotBlank
        Long restaurantId,

        @NotBlank
        Long criteriaId
) {
}
