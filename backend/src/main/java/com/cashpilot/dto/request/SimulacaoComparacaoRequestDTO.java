package com.cashpilot.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record SimulacaoComparacaoRequestDTO(

        @NotNull(message = "O horizonte em meses é obrigatório")
        @Min(value = 1, message = "O horizonte deve ser de pelo menos 1 mês")
        @Max(value = 600, message = "O horizonte deve ser de no máximo 600 meses")
        Integer horizonteMeses,

        @NotNull(message = "Os cenários são obrigatórios")
        @Size(min = 2, max = 3, message = "Informe entre 2 e 3 cenários")
        @Valid
        List<CenarioSimulacaoRequestDTO> cenarios

) {
}
