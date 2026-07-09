package com.cashpilot.controller;

import com.cashpilot.dto.response.RelatorioMensalDTO;
import com.cashpilot.service.RelatorioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/relatorios")
@RequiredArgsConstructor
@Tag(name = "Relatórios", description = "Relatórios financeiros agregados do usuário")
public class RelatorioController {

    private final RelatorioService relatorioService;

    @GetMapping("/mensal")
    @Operation(summary = "Relatório mensal de entradas, saídas, investimentos e saldo líquido dos últimos N meses")
    public ResponseEntity<List<RelatorioMensalDTO>> getRelatorioMensal(
            @RequestParam(required = false, defaultValue = "12") Integer meses) {
        return ResponseEntity.ok(relatorioService.getRelatorioMensal(meses));
    }

}
