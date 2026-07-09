package com.cashpilot.controller;

import com.cashpilot.dto.request.ExpenseRequestDTO;
import com.cashpilot.dto.request.MarkExpensePaidRequestDTO;
import com.cashpilot.dto.response.ExpenseResponseDTO;
import com.cashpilot.service.ExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/despesas")
@RequiredArgsConstructor
@Tag(name = "Despesas", description = "Lançamentos de despesas do usuário")
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping
    @Operation(summary = "Cadastrar despesa")
    public ResponseEntity<ExpenseResponseDTO> create(@Valid @RequestBody ExpenseRequestDTO dto) {
        ExpenseResponseDTO created = expenseService.create(dto);
        return ResponseEntity.created(URI.create("/api/v1/despesas/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Editar despesa")
    public ResponseEntity<ExpenseResponseDTO> update(@PathVariable Long id, @Valid @RequestBody ExpenseRequestDTO dto) {
        return ResponseEntity.ok(expenseService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir despesa")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        expenseService.delete(id);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar despesa por ID")
    public ResponseEntity<ExpenseResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(expenseService.findById(id));
    }

    @GetMapping
    @Operation(summary = "Listar despesas (paginado, com filtros opcionais)")
    public ResponseEntity<Page<ExpenseResponseDTO>> findAll(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) Long contaId,
            @RequestParam(required = false) Long cartaoId,
            @RequestParam(required = false) Boolean paga,
            @PageableDefault(size = 20, sort = "data") Pageable pageable) {
        return ResponseEntity.ok(expenseService.findAll(dataInicio, dataFim, categoriaId, contaId, cartaoId, paga, pageable));
    }

    @PatchMapping("/{id}/pagar")
    @Operation(summary = "Marcar despesa como paga")
    public ResponseEntity<ExpenseResponseDTO> markAsPaid(@PathVariable Long id,
                                                          @RequestBody(required = false) MarkExpensePaidRequestDTO dto) {
        return ResponseEntity.ok(expenseService.markAsPaid(id, dto));
    }

}
