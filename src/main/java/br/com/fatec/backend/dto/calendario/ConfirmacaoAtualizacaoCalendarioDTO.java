package br.com.fatec.backend.dto.calendario;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados opcionais da confirmação de atualização do calendário acadêmico")
public record ConfirmacaoAtualizacaoCalendarioDTO(
        @Size(max = 500, message = "A observação deve ter no máximo 500 caracteres")
        @Schema(description = "Observação livre (ex: 'Calendário 2027 publicado pela CPS em 10/02')")
        String observacao
) {
}
