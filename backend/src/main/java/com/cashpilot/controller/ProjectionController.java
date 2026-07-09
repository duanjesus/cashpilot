package com.cashpilot.controller;

import com.cashpilot.dto.request.ProjectionRequestDTO;
import com.cashpilot.dto.response.ProjectionPreferenceResponseDTO;
import com.cashpilot.dto.response.ProjectionResponseDTO;
import com.cashpilot.service.ProjectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/projecao")
@RequiredArgsConstructor
@Tag(name = "Projeção", description = "Simulação de quando o usuário atingirá seu patrimônio alvo")
public class ProjectionController {

    private final ProjectionService projectionService;

    @PostMapping("/calcular")
    @Operation(summary = "Calcular projeção patrimonial e salvar como última simulação do usuário")
    public ResponseEntity<ProjectionResponseDTO> calcular(@Valid @RequestBody ProjectionRequestDTO dto) {
        return ResponseEntity.ok(projectionService.calcular(dto));
    }

    @GetMapping("/ultima-simulacao")
    @Operation(summary = "Buscar a última simulação de projeção salva pelo usuário")
    public ResponseEntity<ProjectionPreferenceResponseDTO> getUltimaSimulacao() {
        return ResponseEntity.ok(projectionService.getUltimaSimulacao());
    }

}
