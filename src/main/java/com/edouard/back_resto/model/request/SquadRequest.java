package com.edouard.back_resto.model.request;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

public record SquadRequest(
        @NotBlank
        @Length(min = 2, max = 25)
        String name
) {
}
