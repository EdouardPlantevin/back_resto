package com.edouard.back_resto.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RestaurantRequest(
        @NotBlank
        String name,

        String address,
        String description,
        String phone,

        @NotNull
        Long squadId
) {
}
