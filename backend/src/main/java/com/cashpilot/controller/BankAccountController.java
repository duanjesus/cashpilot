package com.cashpilot.controller;

import com.cashpilot.dto.request.BankAccountRequestDTO;
import com.cashpilot.dto.response.BankAccountResponseDTO;
import com.cashpilot.service.BankAccountService;
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

    @GetMapping
    @Operation(summary = "Listar contas bancárias do usuário")
    public ResponseEntity<List<BankAccountResponseDTO>> findAll() {
        return ResponseEntity.ok(bankAccountService.findAll());
    }

}
