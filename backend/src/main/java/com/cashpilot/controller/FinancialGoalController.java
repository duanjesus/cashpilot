package com.cashpilot.controller;

import com.cashpilot.dto.request.FinancialGoalRequestDTO;
import com.cashpilot.dto.request.UpdateGoalProgressRequestDTO;
import com.cashpilot.dto.response.FinancialGoalResponseDTO;
import com.cashpilot.service.FinancialGoalService;
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
@RequestMapping("/api/v1/metas")
@RequiredArgsConstructor
@Tag(name = "Metas Financeiras", description = "Cadastro e acompanhamento de metas financeiras do usuário")
public class FinancialGoalController {

    private final FinancialGoalService financialGoalService;

    @PostMapping
    @Operation(summary = "Cadastrar meta financeira")
    public ResponseEntity<FinancialGoalResponseDTO> create(@Valid @RequestBody FinancialGoalRequestDTO dto) {
        FinancialGoalResponseDTO created = financialGoalService.create(dto);
        return ResponseEntity.created(URI.create("/api/v1/metas/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Editar meta financeira")
    public ResponseEntity<FinancialGoalResponseDTO> update(@PathVariable Long id, @Valid @RequestBody FinancialGoalRequestDTO dto) {
        return ResponseEntity.ok(financialGoalService.update(id, dto));
    }

    @PatchMapping("/{id}/progresso")
    @Operation(summary = "Atualizar o valor atual (progresso) de uma meta financeira")
    public ResponseEntity<FinancialGoalResponseDTO> updateProgress(@PathVariable Long id,
                                                                    @Valid @RequestBody UpdateGoalProgressRequestDTO dto) {
        return ResponseEntity.ok(financialGoalService.updateProgress(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir meta financeira")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        financialGoalService.delete(id);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar meta financeira por ID")
    public ResponseEntity<FinancialGoalResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(financialGoalService.findById(id));
    }

    @GetMapping
    @Operation(summary = "Listar metas financeiras do usuário")
    public ResponseEntity<List<FinancialGoalResponseDTO>> findAll() {
        return ResponseEntity.ok(financialGoalService.findAll());
    }

}
