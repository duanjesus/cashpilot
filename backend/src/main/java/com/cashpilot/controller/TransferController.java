package com.cashpilot.controller;

import com.cashpilot.dto.request.TransferRequestDTO;
import com.cashpilot.dto.response.TransferResponseDTO;
import com.cashpilot.service.TransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/transferencias")
@RequiredArgsConstructor
@Tag(name = "Transferências", description = "Movimentações de valores entre contas bancárias do próprio usuário (somente inclusão)")
public class TransferController {

    private final TransferService transferService;

    @PostMapping
    @Operation(summary = "Registrar transferência")
    public ResponseEntity<TransferResponseDTO> create(@Valid @RequestBody TransferRequestDTO dto) {
        TransferResponseDTO created = transferService.create(dto);
        return ResponseEntity.created(URI.create("/api/v1/transferencias/" + created.id())).body(created);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir transferência")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        transferService.delete(id);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar transferência por ID")
    public ResponseEntity<TransferResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(transferService.findById(id));
    }

    @GetMapping
    @Operation(summary = "Listar transferências (paginado)")
    public ResponseEntity<Page<TransferResponseDTO>> findAll(@PageableDefault(size = 20, sort = "data") Pageable pageable) {
        return ResponseEntity.ok(transferService.findAll(pageable));
    }

}
