package com.cashpilot.controller;

import com.cashpilot.dto.request.ConectarOpenFinanceRequestDTO;
import com.cashpilot.dto.response.BankAccountResponseDTO;
import com.cashpilot.dto.response.CreditCardResponseDTO;
import com.cashpilot.dto.response.InstituicaoMockDTO;
import com.cashpilot.entity.enums.TipoConexao;
import com.cashpilot.service.OpenFinanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Open Finance account-linking stub. Demonstrates the data model/API shape for a
 * future real integration — {@code /instituicoes} is a hardcoded illustrative list,
 * {@code /conectar} creates a real {@link com.cashpilot.entity.BankAccount}/
 * {@link com.cashpilot.entity.CreditCard} row tagged as Open Finance-origin, and
 * {@code /sincronizar} only bumps a "last synced" timestamp. No real bank API is
 * ever called and no transaction data is ever fabricated.
 */
@RestController
@RequestMapping("/api/v1/open-finance")
@RequiredArgsConstructor
@Tag(name = "Open Finance", description = "Stub de conexão de contas/cartões via Open Finance (demo, sem integração real)")
public class OpenFinanceController {

    private final OpenFinanceService openFinanceService;

    @GetMapping("/instituicoes")
    @Operation(summary = "Listar instituições mock disponíveis para conexão")
    public ResponseEntity<List<InstituicaoMockDTO>> listarInstituicoes() {
        return ResponseEntity.ok(openFinanceService.listarInstituicoes());
    }

    @PostMapping("/conectar")
    @Operation(summary = "Conectar uma conta ou cartão mock via Open Finance")
    public ResponseEntity<?> conectar(@Valid @RequestBody ConectarOpenFinanceRequestDTO dto) {
        if (dto.tipoConta() == TipoConexao.CARTAO) {
            CreditCardResponseDTO created = openFinanceService.conectarCartao(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        }
        BankAccountResponseDTO created = openFinanceService.conectarConta(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PatchMapping("/contas/{id}/sincronizar")
    @Operation(summary = "Simular sincronização de uma conta bancária conectada via Open Finance")
    public ResponseEntity<BankAccountResponseDTO> sincronizarConta(@PathVariable Long id) {
        return ResponseEntity.ok(openFinanceService.sincronizarConta(id));
    }

    @PatchMapping("/cartoes/{id}/sincronizar")
    @Operation(summary = "Simular sincronização de um cartão de crédito conectado via Open Finance")
    public ResponseEntity<CreditCardResponseDTO> sincronizarCartao(@PathVariable Long id) {
        return ResponseEntity.ok(openFinanceService.sincronizarCartao(id));
    }

}
