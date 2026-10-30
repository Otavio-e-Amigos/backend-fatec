package br.com.fatec.backend.dto.folha_ponto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Prévia dos dados estruturados da Folha de Ponto para conferência")
public record FolhaPontoPreviewDTO(
        @Schema(description = "Dados do professor")
        ProfessorResumoDTO professor,

        @Schema(description = "Mês de referência", example = "9")
        Integer mes,

        @Schema(description = "Ano de referência", example = "2026")
        Integer ano,

        @Schema(description = "Título oficial formatado", example = "FOLHA DE FREQUÊNCIA – 09/2026")
        String titulo,

        @Schema(description = "Mapeamento das aulas alocadas na Grade Horária")
        List<AulaSlotDTO> gradeHoraria,

        @Schema(description = "Lista dos dias do mês com indicação de domingos e feriados")
        List<DiaMesDTO> dias,

        @Schema(description = "Avisos ou notas sobre o preenchimento")
        List<String> avisos
) {}
