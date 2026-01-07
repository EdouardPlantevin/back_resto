package com.edouard.back_resto.controller;

import com.edouard.back_resto.model.dto.RestaurantDto;
import com.edouard.back_resto.model.request.RestaurantRequest;
import com.edouard.back_resto.service.RestaurantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Restaurant")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/restaurants")
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
public class RestaurantController {

    private final RestaurantService restaurantService;

    @GetMapping("/")
    @Operation(security = {@SecurityRequirement(name = "bearerAuth")})
    public List<RestaurantDto> findAll(Long squadId) {
        return restaurantService.findAll(squadId);
    }

    @PostMapping("/create")
    @Operation(security = {@SecurityRequirement(name = "bearerAuth")})
    public void create(@Valid @RequestBody RestaurantRequest restaurantRequest) {
        restaurantService.create(restaurantRequest);
    }

    @DeleteMapping("/{id}")
    @Operation(security = {@SecurityRequirement(name = "bearerAuth")})
    public void delete(@PathVariable Long id) {
        restaurantService.delete(id);
    }


}
