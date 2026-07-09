package com.cashpilot.controller;

import com.cashpilot.dto.request.IncomeRequestDTO;
import com.cashpilot.dto.request.MarkIncomeReceivedRequestDTO;
import com.cashpilot.dto.response.IncomeResponseDTO;
import com.cashpilot.entity.Income;
import com.cashpilot.exception.BusinessException;
import com.cashpilot.export.ExcelExportUtil;
import com.cashpilot.export.PdfExportUtil;
import com.cashpilot.service.IncomeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/receitas")
@RequiredArgsConstructor
@Tag(name = "Receitas", description = "Lançamentos de receitas do usuário")
public class IncomeController {

    private final IncomeService incomeService;
    private final ExcelExportUtil excelExportUtil;
    private final PdfExportUtil pdfExportUtil;

    @PostMapping
    @Operation(summary = "Cadastrar receita")
    public ResponseEntity<IncomeResponseDTO> create(@Valid @RequestBody IncomeRequestDTO dto) {
        IncomeResponseDTO created = incomeService.create(dto);
        return ResponseEntity.created(URI.create("/api/v1/receitas/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Editar receita")
    public ResponseEntity<IncomeResponseDTO> update(@PathVariable Long id, @Valid @RequestBody IncomeRequestDTO dto) {
        return ResponseEntity.ok(incomeService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir receita")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        incomeService.delete(id);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar receita por ID")
    public ResponseEntity<IncomeResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(incomeService.findById(id));
    }

    @GetMapping
    @Operation(summary = "Listar receitas (paginado, com filtros opcionais)")
    public ResponseEntity<Page<IncomeResponseDTO>> findAll(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) Long contaId,
            @RequestParam(required = false) Boolean recebida,
            @PageableDefault(size = 20, sort = "data") Pageable pageable) {
        return ResponseEntity.ok(incomeService.findAll(dataInicio, dataFim, categoriaId, contaId, recebida, pageable));
    }

    @PatchMapping("/{id}/receber")
    @Operation(summary = "Marcar receita como recebida")
    public ResponseEntity<IncomeResponseDTO> markAsReceived(@PathVariable Long id,
                                                             @RequestBody(required = false) MarkIncomeReceivedRequestDTO dto) {
        return ResponseEntity.ok(incomeService.markAsReceived(id, dto));
    }

    @GetMapping("/exportar")
    @Operation(summary = "Exportar receitas filtradas em Excel (xlsx) ou PDF")
    public ResponseEntity<byte[]> exportar(
            @RequestParam String formato,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) Long contaId,
            @RequestParam(required = false) Boolean recebida) {

        List<Income> receitas = incomeService.findAllForExport(dataInicio, dataFim, categoriaId, contaId, recebida);

        byte[] conteudo;
        MediaType mediaType;
        String extensao;
        if ("xlsx".equalsIgnoreCase(formato)) {
            conteudo = excelExportUtil.buildReceitasWorkbook(receitas);
            mediaType = MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            extensao = "xlsx";
        } else if ("pdf".equalsIgnoreCase(formato)) {
            conteudo = pdfExportUtil.buildReceitasPdf(receitas);
            mediaType = MediaType.APPLICATION_PDF;
            extensao = "pdf";
        } else {
            throw new BusinessException("Formato de exportação inválido: use 'xlsx' ou 'pdf'");
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"receitas." + extensao + "\"")
                .body(conteudo);
    }

}
