package com.cashpilot.dto.request;

import com.cashpilot.entity.enums.TipoConexao;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ConectarOpenFinanceRequestDTO(

        @NotBlank(message = "O nome da instituição é obrigatório")
        @Size(max = 150, message = "O nome da instituição deve ter no máximo 150 caracteres")
        String instituicaoNome,

        @NotNull(message = "O tipo de conexão é obrigatório")
        TipoConexao tipoConta,

        @NotBlank(message = "O apelido é obrigatório")
        @Size(max = 100, message = "O apelido deve ter no máximo 100 caracteres")
        String apelido

) {
}
