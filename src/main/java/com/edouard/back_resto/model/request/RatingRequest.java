package com.edouard.back_resto.model.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import org.hibernate.validator.constraints.Length;

public record RatingRequest(
        @NotNull(message = "Score is required")
        @Min(value = 0, message = "Score must be positive or zero")
        @Max(value = 10, message = "Score must be between 0 and 10")
        @PositiveOrZero(message = "Score must be positive or zero")
        Integer score,

        @NotNull(message = "Restaurant id is required")
        Long restaurantId,

        @NotNull(message = "Criteria id is required")
        Long criteriaId
) {
}
