package com.edouard.back_resto.controller;


import com.edouard.back_resto.model.dto.CriteriaDto;
import com.edouard.back_resto.service.CriteriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/criteria")
@RequiredArgsConstructor
public class CriteriaController {


    private final CriteriaService criteriaService;

    @GetMapping()
    @Operation(security = {@SecurityRequirement(name = "bearerAuth")})
    public List<CriteriaDto> findAll() {
        return criteriaService.findAll();
    }


}
