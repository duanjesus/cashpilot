package com.cashpilot.controller;

import com.cashpilot.dto.response.PrevisaoSaldoResponseDTO;
import com.cashpilot.service.PrevisaoSaldoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/previsao-saldo")
@RequiredArgsConstructor
@Tag(name = "Previsão de Saldo", description = "Projeção de saldo futuro com base na média histórica de saldo líquido mensal")
public class PrevisaoSaldoController {

    private final PrevisaoSaldoService previsaoSaldoService;

    @GetMapping
    @Operation(summary = "Projetar saldo futuro com base na média histórica de saldo líquido mensal")
    public ResponseEntity<PrevisaoSaldoResponseDTO> getPrevisao(
            @RequestParam(required = false) Integer mesesHistorico,
            @RequestParam(required = false) Integer mesesProjecao) {
        return ResponseEntity.ok(previsaoSaldoService.getPrevisao(mesesHistorico, mesesProjecao));
    }

}
