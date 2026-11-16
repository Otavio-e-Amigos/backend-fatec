package br.com.fatec.backend.controller;

import br.com.fatec.backend.dto.common.RespostaPadraoDTO;
import br.com.fatec.backend.dto.relatorio.RelatorioExtracaoGradeDTO;
import br.com.fatec.backend.dto.relatorio.RelatorioFolhaPontoDTO;
import br.com.fatec.backend.service.relatorio.ArquivoRelatorio;
import br.com.fatec.backend.service.relatorio.RelatorioExportacaoService;
import br.com.fatec.backend.service.relatorio.RelatorioExtracaoGradeService;
import br.com.fatec.backend.service.relatorio.RelatorioFolhaPontoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/relatorios")
@Tag(name = "Relatórios", description = "Relatórios gerenciais de extração de grades e de geração de folhas de ponto, com exportação em PDF e DOCX")
public class RelatorioController {

    private static final String DESC_PERIODO = "Recorte do relatório: DIARIO, SEMANAL (segunda a domingo) ou MENSAL";
    private static final String DESC_DATA = "Qualquer data dentro do período desejado (yyyy-MM-dd). Padrão: hoje. Não pode ser futura.";

    private final RelatorioExtracaoGradeService relatorioExtracaoGradeService;
    private final RelatorioFolhaPontoService relatorioFolhaPontoService;
    private final RelatorioExportacaoService exportacaoService;

    public RelatorioController(RelatorioExtracaoGradeService relatorioExtracaoGradeService,
                               RelatorioFolhaPontoService relatorioFolhaPontoService,
                               RelatorioExportacaoService exportacaoService) {
        this.relatorioExtracaoGradeService = relatorioExtracaoGradeService;
        this.relatorioFolhaPontoService = relatorioFolhaPontoService;
        this.exportacaoService = exportacaoService;
    }

    // -------------------------------------------------------------------------
    // Extração de grades
    // -------------------------------------------------------------------------

    @GetMapping("/extracoes-grade")
    @Operation(summary = "Relatório de extração de grades",
            description = "Quantidade de grades extraídas no período, taxa de sucesso, disciplinas e aulas extraídas, "
                    + "tempo de processamento, distribuição por dia, usuário e período letivo, falhas e detalhamento.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Relatório gerado"),
            @ApiResponse(responseCode = "400", description = "Período ou data inválidos")
    })
    public ResponseEntity<RespostaPadraoDTO<RelatorioExtracaoGradeDTO>> extracaoGrades(
            @Parameter(description = DESC_PERIODO, example = "MENSAL") @RequestParam String periodo,
            @Parameter(description = DESC_DATA, example = "2026-11-16")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return ResponseEntity.ok(RespostaPadraoDTO.sucesso("Relatório de extração de grades gerado com sucesso.",
                relatorioExtracaoGradeService.gerar(periodo, data)));
    }

    @GetMapping("/extracoes-grade/exportar")
    @Operation(summary = "Exportar relatório de extração de grades (PDF ou DOCX)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Arquivo gerado para download"),
            @ApiResponse(responseCode = "400", description = "Período, data ou formato inválidos")
    })
    public ResponseEntity<byte[]> exportarExtracaoGrades(
            @Parameter(description = DESC_PERIODO, example = "MENSAL") @RequestParam String periodo,
            @Parameter(description = DESC_DATA, example = "2026-11-16")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
            @Parameter(description = "PDF ou DOCX", example = "PDF") @RequestParam String formato) {
        return download(exportacaoService.exportarExtracaoGrades(periodo, data, formato));
    }

    // -------------------------------------------------------------------------
    // Folhas de ponto
    // -------------------------------------------------------------------------

    @GetMapping("/folhas-ponto")
    @Operation(summary = "Relatório de geração de folhas de ponto",
            description = "Sem professorId: visão de todos os professores, com comparação individual e geral. "
                    + "Com professorId: relatório individual com identificação do professor, grades vinculadas e principais disciplinas.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Relatório gerado"),
            @ApiResponse(responseCode = "400", description = "Período ou data inválidos"),
            @ApiResponse(responseCode = "404", description = "Professor não encontrado")
    })
    public ResponseEntity<RespostaPadraoDTO<RelatorioFolhaPontoDTO>> folhasPonto(
            @Parameter(description = DESC_PERIODO, example = "MENSAL") @RequestParam String periodo,
            @Parameter(description = DESC_DATA, example = "2026-11-16")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
            @Parameter(description = "ID do professor (opcional; omitido = todos os professores)")
            @RequestParam(required = false) Long professorId) {
        return ResponseEntity.ok(RespostaPadraoDTO.sucesso("Relatório de folhas de ponto gerado com sucesso.",
                relatorioFolhaPontoService.gerar(periodo, data, professorId)));
    }

    @GetMapping("/folhas-ponto/exportar")
    @Operation(summary = "Exportar relatório de folhas de ponto (PDF ou DOCX)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Arquivo gerado para download"),
            @ApiResponse(responseCode = "400", description = "Período, data ou formato inválidos"),
            @ApiResponse(responseCode = "404", description = "Professor não encontrado")
    })
    public ResponseEntity<byte[]> exportarFolhasPonto(
            @Parameter(description = DESC_PERIODO, example = "MENSAL") @RequestParam String periodo,
            @Parameter(description = DESC_DATA, example = "2026-11-16")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
            @Parameter(description = "ID do professor (opcional)") @RequestParam(required = false) Long professorId,
            @Parameter(description = "PDF ou DOCX", example = "PDF") @RequestParam String formato) {
        return download(exportacaoService.exportarFolhasPonto(periodo, data, professorId, formato));
    }

    private ResponseEntity<byte[]> download(ArquivoRelatorio arquivo) {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(arquivo.mediaType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + arquivo.nomeArquivo() + "\"")
                .body(arquivo.conteudo());
    }
}
