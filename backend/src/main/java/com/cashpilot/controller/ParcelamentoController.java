package com.cashpilot.controller;

import com.cashpilot.dto.request.ParcelamentoRequestDTO;
import com.cashpilot.dto.response.ParcelamentoResponseDTO;
import com.cashpilot.service.ParcelamentoService;
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
@RequestMapping("/api/v1/parcelamentos")
@RequiredArgsConstructor
@Tag(name = "Parcelamentos", description = "Compras parceladas — geram as despesas de cada parcela automaticamente")
public class ParcelamentoController {

    private final ParcelamentoService parcelamentoService;

    @PostMapping
    @Operation(summary = "Cadastrar parcelamento (gera as despesas das parcelas automaticamente)")
    public ResponseEntity<ParcelamentoResponseDTO> create(@Valid @RequestBody ParcelamentoRequestDTO dto) {
        ParcelamentoResponseDTO created = parcelamentoService.create(dto);
        return ResponseEntity.created(URI.create("/api/v1/parcelamentos/" + created.id())).body(created);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir parcelamento (bloqueado se houver parcelas já pagas)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        parcelamentoService.delete(id);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar parcelamento por ID (com progresso de parcelas pagas)")
    public ResponseEntity<ParcelamentoResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(parcelamentoService.findById(id));
    }

    @GetMapping
    @Operation(summary = "Listar parcelamentos (paginado)")
    public ResponseEntity<Page<ParcelamentoResponseDTO>> findAll(
            @PageableDefault(size = 20, sort = "dataPrimeiraParcela") Pageable pageable) {
        return ResponseEntity.ok(parcelamentoService.findAll(pageable));
    }

}
