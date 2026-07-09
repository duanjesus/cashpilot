package com.cashpilot.controller;

import com.cashpilot.dto.response.FluxoCaixaResponseDTO;
import com.cashpilot.service.FluxoCaixaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/fluxo-caixa")
@RequiredArgsConstructor
@Tag(name = "Fluxo de Caixa", description = "Projeção de saldo futuro com base em despesas/receitas pendentes e assinaturas ativas")
public class FluxoCaixaController {

    private final FluxoCaixaService fluxoCaixaService;

    @GetMapping
    @Operation(summary = "Obter a projeção de fluxo de caixa para os próximos N dias (padrão 30)")
    public ResponseEntity<FluxoCaixaResponseDTO> getFluxoCaixa(@RequestParam(required = false) Integer dias) {
        return ResponseEntity.ok(fluxoCaixaService.getFluxoCaixa(dias));
    }

}
