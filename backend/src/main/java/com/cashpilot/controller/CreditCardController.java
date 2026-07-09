package com.cashpilot.controller;

import com.cashpilot.dto.request.CreditCardRequestDTO;
import com.cashpilot.dto.response.CreditCardResponseDTO;
import com.cashpilot.service.CreditCardService;
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
@RequestMapping("/api/v1/cartoes")
@RequiredArgsConstructor
@Tag(name = "Cartões de Crédito", description = "Cadastro e gestão de cartões de crédito do usuário")
public class CreditCardController {

    private final CreditCardService creditCardService;

    @PostMapping
    @Operation(summary = "Cadastrar cartão de crédito")
    public ResponseEntity<CreditCardResponseDTO> create(@Valid @RequestBody CreditCardRequestDTO dto) {
        CreditCardResponseDTO created = creditCardService.create(dto);
        return ResponseEntity.created(URI.create("/api/v1/cartoes/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Editar cartão de crédito")
    public ResponseEntity<CreditCardResponseDTO> update(@PathVariable Long id, @Valid @RequestBody CreditCardRequestDTO dto) {
        return ResponseEntity.ok(creditCardService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir cartão de crédito")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        creditCardService.delete(id);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar cartão de crédito por ID")
    public ResponseEntity<CreditCardResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(creditCardService.findById(id));
    }

    @GetMapping
    @Operation(summary = "Listar cartões de crédito do usuário")
    public ResponseEntity<List<CreditCardResponseDTO>> findAll() {
        return ResponseEntity.ok(creditCardService.findAll());
    }

}
