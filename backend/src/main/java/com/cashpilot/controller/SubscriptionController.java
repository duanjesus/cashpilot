package com.cashpilot.controller;

import com.cashpilot.dto.request.SubscriptionRequestDTO;
import com.cashpilot.dto.response.ExpenseResponseDTO;
import com.cashpilot.dto.response.SubscriptionResponseDTO;
import com.cashpilot.service.SubscriptionService;
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
@RequestMapping("/api/v1/assinaturas")
@RequiredArgsConstructor
@Tag(name = "Assinaturas", description = "Assinaturas recorrentes — geram despesas mensais automaticamente")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @PostMapping
    @Operation(summary = "Cadastrar assinatura recorrente")
    public ResponseEntity<SubscriptionResponseDTO> create(@Valid @RequestBody SubscriptionRequestDTO dto) {
        SubscriptionResponseDTO created = subscriptionService.create(dto);
        return ResponseEntity.created(URI.create("/api/v1/assinaturas/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Editar assinatura recorrente")
    public ResponseEntity<SubscriptionResponseDTO> update(@PathVariable Long id, @Valid @RequestBody SubscriptionRequestDTO dto) {
        return ResponseEntity.ok(subscriptionService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir assinatura (despesas já geradas são preservadas)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        subscriptionService.delete(id);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar assinatura por ID")
    public ResponseEntity<SubscriptionResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(subscriptionService.findById(id));
    }

    @GetMapping
    @Operation(summary = "Listar assinaturas do usuário")
    public ResponseEntity<List<SubscriptionResponseDTO>> findAll() {
        return ResponseEntity.ok(subscriptionService.findAll());
    }

    @PostMapping("/gerar-pendentes")
    @Operation(summary = "Gerar manualmente as cobranças pendentes das assinaturas ativas do usuário")
    public ResponseEntity<List<ExpenseResponseDTO>> gerarPendentes() {
        return ResponseEntity.ok(subscriptionService.gerarPendentes());
    }

}
