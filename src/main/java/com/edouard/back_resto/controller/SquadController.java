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
import org.springframework.web.bind.annotation.*;

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
        squadService.createSquad(squadRequest);
        return ResponseEntity.ok(new MessageResponse("Squad created successfully"));
    }

    @PostMapping("/join")
    @Operation(security = {@SecurityRequirement(name = "bearerAuth")})
    public ResponseEntity<MessageResponse> joinSquad(String codeJoin) {
        squadService.joinSquad(codeJoin);
        return ResponseEntity.ok(new MessageResponse("Squad joined successfully"));
    }

    @PostMapping("/leave")
    @Operation(security = {@SecurityRequirement(name = "bearerAuth")})
    public ResponseEntity<MessageResponse> leaveSquad(Long squadId) {
        squadService.leaveSquad(squadId);
        return ResponseEntity.ok(new MessageResponse("Squad leaved successfully"));
    }

    @DeleteMapping("/{id}")
    @Operation(security = {@SecurityRequirement(name = "bearerAuth")})
    public ResponseEntity<MessageResponse> deleteSquad(@PathVariable Long id) {
        squadService.deleteSquad(id);
        return ResponseEntity.ok(new MessageResponse("Squad deleted successfully"));
    }

}
