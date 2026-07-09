package com.cashpilot.controller;

import com.cashpilot.dto.request.SimulacaoComparacaoRequestDTO;
import com.cashpilot.dto.response.SimulacaoComparacaoResponseDTO;
import com.cashpilot.service.SimulacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/simulacoes")
@RequiredArgsConstructor
@Tag(name = "Simulação Financeira", description = "Comparação de cenários de evolução patrimonial")
public class SimulacaoController {

    private final SimulacaoService simulacaoService;

    @PostMapping("/comparar")
    @Operation(summary = "Comparar de 2 a 3 cenários de simulação financeira ao longo de um horizonte em meses")
    public ResponseEntity<SimulacaoComparacaoResponseDTO> comparar(@Valid @RequestBody SimulacaoComparacaoRequestDTO dto) {
        return ResponseEntity.ok(simulacaoService.comparar(dto));
    }

}
