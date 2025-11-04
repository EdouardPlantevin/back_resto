package com.edouard.back_resto.controller;

import com.edouard.back_resto.model.request.SquadRequest;
import com.edouard.back_resto.model.response.MessageResponse;
import com.edouard.back_resto.service.SquadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Squad")
@RestController
@RequestMapping("/api/squad")
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
@RequiredArgsConstructor
public class SquadController {

    private final SquadService squadService;

    @PostMapping("/create")
    @Operation(security = {@SecurityRequirement(name = "bearerAuth")})
    public ResponseEntity<MessageResponse> createSquad(@Valid @RequestBody SquadRequest squadRequest) {
        try {
            squadService.createSquad(squadRequest);
            return ResponseEntity.ok(new MessageResponse("Squad created successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

}
