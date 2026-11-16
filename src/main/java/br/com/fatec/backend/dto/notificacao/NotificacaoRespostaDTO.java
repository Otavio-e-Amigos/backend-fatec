package br.com.fatec.backend.dto.notificacao;

import br.com.fatec.backend.entity.Notificacao;
import br.com.fatec.backend.enums.CategoriaNotificacao;
import br.com.fatec.backend.enums.TipoNotificacao;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Notificação interna do usuário autenticado")
public record NotificacaoRespostaDTO(
        Long id,
        CategoriaNotificacao categoria,
        TipoNotificacao tipo,
        String titulo,
        String mensagem,
        @Schema(description = "Tipo do recurso relacionado (ex: REGISTRO_EXTRACAO_GRADE, REGISTRO_FOLHA_PONTO, CALENDARIO)")
        String referenciaTipo,
        @Schema(description = "Identificador do recurso relacionado, quando houver")
        Long referenciaId,
        boolean lida,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime lidaEm,
        @Schema(description = "Indica uma pendência que exige ação (ex: atualizar o calendário acadêmico)")
        boolean pendente,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime resolvidaEm,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime expiraEm,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime criadaEm
) {
    public static NotificacaoRespostaDTO de(Notificacao n) {
        return new NotificacaoRespostaDTO(
                n.getId(), n.getCategoria(), n.getTipo(), n.getTitulo(), n.getMensagem(),
                n.getReferenciaTipo(), n.getReferenciaId(), n.isLida(), n.getLidaEm(),
                n.isPendente(), n.getResolvidaEm(), n.getExpiraEm(), n.getCreatedAt());
    }
}
