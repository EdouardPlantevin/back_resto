package com.edouard.back_resto.model.dto;

import jakarta.persistence.Id;

import java.util.Date;

public record RestaurantDto(
        @Id
        Long id,

        Date createdAt,
        String name,
        String address,
        String description,
        String phone
) {
}
