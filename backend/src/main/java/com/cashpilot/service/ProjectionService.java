package com.cashpilot.service;

import com.cashpilot.dto.request.ProjectionRequestDTO;
import com.cashpilot.dto.response.ProjectionPreferenceResponseDTO;
import com.cashpilot.dto.response.ProjectionResponseDTO;

public interface ProjectionService {

    ProjectionResponseDTO calcular(ProjectionRequestDTO dto);

    ProjectionPreferenceResponseDTO getUltimaSimulacao();

}
