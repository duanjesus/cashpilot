package com.cashpilot.controller;

import com.cashpilot.dto.request.BankAccountRequestDTO;
import com.cashpilot.dto.response.BankAccountResponseDTO;
import com.cashpilot.dto.response.CapturaSaldoResponseDTO;
import com.cashpilot.dto.response.SaldoHistoricoPontoDTO;
import com.cashpilot.service.BankAccountService;
import com.cashpilot.service.SaldoHistoricoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/contas")
@RequiredArgsConstructor
@Tag(name = "Contas Bancárias", description = "Cadastro e gestão de contas bancárias do usuário")
public class BankAccountController {

    private final BankAccountService bankAccountService;
    private final SaldoHistoricoService saldoHistoricoService;

    @PostMapping
    @Operation(summary = "Cadastrar conta bancária")
    public ResponseEntity<BankAccountResponseDTO> create(@Valid @RequestBody BankAccountRequestDTO dto) {
        BankAccountResponseDTO created = bankAccountService.create(dto);
        return ResponseEntity.created(URI.create("/api/v1/contas/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Editar conta bancária")
    public ResponseEntity<BankAccountResponseDTO> update(@PathVariable Long id, @Valid @RequestBody BankAccountRequestDTO dto) {
        return ResponseEntity.ok(bankAccountService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir conta bancária")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        bankAccountService.delete(id);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar conta bancária por ID")
    public ResponseEntity<BankAccountResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(bankAccountService.findById(id));
    }

    @GetMapping("/{id}/historico-saldo")
    @Operation(summary = "Saldo realizado dia a dia de uma conta nos últimos N dias")
    public ResponseEntity<List<SaldoHistoricoPontoDTO>> getHistoricoSaldo(@PathVariable Long id,
                                                                          @RequestParam(defaultValue = "30") int dias) {
        return ResponseEntity.ok(saldoHistoricoService.getHistoricoConta(id, dias));
    }

    @PostMapping("/historico-saldo/capturar")
    @Operation(summary = "Registrar manualmente os saldos diários pendentes (mesmo trabalho do job noturno)")
    public ResponseEntity<CapturaSaldoResponseDTO> capturarSaldos() {
        return ResponseEntity.ok(saldoHistoricoService.capturarPendentes());
    }

    @GetMapping
    @Operation(summary = "Listar contas bancárias do usuário")
    public ResponseEntity<List<BankAccountResponseDTO>> findAll() {
        return ResponseEntity.ok(bankAccountService.findAll());
    }

}
