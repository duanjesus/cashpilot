package com.cashpilot.service.impl;

import com.cashpilot.dto.request.ProjectionRequestDTO;
import com.cashpilot.dto.response.ProjectionPreferenceResponseDTO;
import com.cashpilot.dto.response.ProjectionResponseDTO;
import com.cashpilot.entity.ProjectionPreference;
import com.cashpilot.entity.User;
import com.cashpilot.exception.ResourceNotFoundException;
import com.cashpilot.repository.ProjectionPreferenceRepository;
import com.cashpilot.security.CurrentUserProvider;
import com.cashpilot.service.ProjectionCalculator;
import com.cashpilot.service.ProjectionResult;
import com.cashpilot.service.ProjectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectionServiceImpl implements ProjectionService {

    private final ProjectionPreferenceRepository projectionPreferenceRepository;
    private final ProjectionCalculator projectionCalculator;
    private final CurrentUserProvider currentUserProvider;

    @Override
    public ProjectionResponseDTO calcular(ProjectionRequestDTO dto) {
        User user = currentUserProvider.getCurrentUser();

        ProjectionResult result = projectionCalculator.calculate(
                dto.salario(), dto.despesasFixas(), dto.despesasVariaveis(), dto.investimentoMensal(),
                dto.patrimonioAtual(), dto.valorAlvo(), dto.taxaRetornoMensal());

        upsertPreference(user, dto);

        return new ProjectionResponseDTO(
                result.atingivel(),
                result.aporteMensal(),
                result.mesesParaAtingir(),
                result.anos(),
                result.mesesRestantes(),
                result.dataEstimada(),
                result.valorFinalProjetado(),
                dto.patrimonioAtual(),
                dto.valorAlvo(),
                dto.taxaRetornoMensal()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectionPreferenceResponseDTO getUltimaSimulacao() {
        Long userId = currentUserProvider.getCurrentUserId();
        ProjectionPreference preference = projectionPreferenceRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Nenhuma simulação de projeção salva para este usuário"));

        return new ProjectionPreferenceResponseDTO(
                preference.getSalario(),
                preference.getDespesasFixas(),
                preference.getDespesasVariaveis(),
                preference.getInvestimentoMensal(),
                preference.getPatrimonioAtual(),
                preference.getValorAlvo(),
                preference.getTaxaRetornoMensal()
        );
    }

    private void upsertPreference(User user, ProjectionRequestDTO dto) {
        ProjectionPreference preference = projectionPreferenceRepository.findByUserId(user.getId())
                .orElseGet(() -> ProjectionPreference.builder().user(user).build());

        preference.setSalario(dto.salario());
        preference.setDespesasFixas(dto.despesasFixas());
        preference.setDespesasVariaveis(dto.despesasVariaveis());
        preference.setInvestimentoMensal(dto.investimentoMensal());
        preference.setPatrimonioAtual(dto.patrimonioAtual());
        preference.setValorAlvo(dto.valorAlvo());
        preference.setTaxaRetornoMensal(dto.taxaRetornoMensal());

        projectionPreferenceRepository.save(preference);
    }

}
