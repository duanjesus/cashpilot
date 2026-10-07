package com.cashpilot.controller;

import com.cashpilot.dto.response.DashboardSummaryResponseDTO;
import com.cashpilot.dto.response.SaldoHistoricoPontoDTO;
import com.cashpilot.service.DashboardService;
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
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Indicadores agregados do usuário")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/resumo")
    @Operation(summary = "Resumo financeiro do usuário (saldo, entradas/saídas do mês, meta principal, próximas contas)")
    public ResponseEntity<DashboardSummaryResponseDTO> getResumo() {
        return ResponseEntity.ok(dashboardService.getResumo());
    }

    @GetMapping("/evolucao-saldo")
    @Operation(summary = "Saldo realizado dia a dia, somado das contas ativas, nos últimos N dias")
    public ResponseEntity<List<SaldoHistoricoPontoDTO>> getEvolucaoSaldo(@RequestParam(required = false) Integer dias) {
        return ResponseEntity.ok(dashboardService.getEvolucaoSaldo(dias));
    }

}
