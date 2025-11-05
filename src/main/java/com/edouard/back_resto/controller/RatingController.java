package com.edouard.back_resto.controller;

import com.edouard.back_resto.model.request.RatingRequest;
import com.edouard.back_resto.model.response.MessageResponse;
import com.edouard.back_resto.service.RatingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Rating")
@RestController
@RequestMapping("/api/rating")
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;

    @PostMapping("/create")
    @Operation(security = {@SecurityRequirement(name = "bearerAuth")})
    public void create(@Valid @RequestBody RatingRequest ratingRequest) {
        ratingService.create(ratingRequest);
    }

}
