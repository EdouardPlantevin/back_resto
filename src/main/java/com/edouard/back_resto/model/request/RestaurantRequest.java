package com.edouard.back_resto.model.request;

import jakarta.validation.constraints.NotBlank;

public record RestaurantRequest(
        @NotBlank
        String name,

        String address,
        String description,
        String phone,

        @NotBlank
        Long squadId
) {
}
