package br.com.fatec.backend.controller;

import br.com.fatec.backend.dto.common.RespostaPadraoDTO;
import br.com.fatec.backend.dto.folha_ponto.FolhaPontoPreviewDTO;
import br.com.fatec.backend.dto.folha_ponto.FolhaPontoRequisicaoDTO;
import br.com.fatec.backend.service.FolhaPontoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/folhas-ponto")
@Tag(name = "Folha de Ponto", description = "Endpoints para prévia estruturada e geração do documento oficial DOCX da Folha de Ponto")
public class FolhaPontoController {

    private static final String DOCX_MEDIA_TYPE = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";

    private final FolhaPontoService folhaPontoService;

    public FolhaPontoController(FolhaPontoService folhaPontoService) {
        this.folhaPontoService = folhaPontoService;
    }

    @PostMapping("/preview")
    @Operation(summary = "Obter prévia da folha de ponto", description = "Gera uma prévia com dados do professor, slots da grade e calendário mensal para conferência antes da emissão")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Prévia gerada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetros inválidos"),
            @ApiResponse(responseCode = "404", description = "Professor ou grade não encontrados")
    })
    public ResponseEntity<RespostaPadraoDTO<FolhaPontoPreviewDTO>> previewPost(@Valid @RequestBody FolhaPontoRequisicaoDTO requisicao) {
        FolhaPontoPreviewDTO preview = folhaPontoService.montarPreview(requisicao);
        return ResponseEntity.ok(RespostaPadraoDTO.sucesso("Prévia da folha de ponto gerada com sucesso.", preview));
    }

    @GetMapping("/preview")
    @Operation(summary = "Obter prévia da folha de ponto via GET", description = "Gera a prévia informando os parâmetros na query string")
    public ResponseEntity<RespostaPadraoDTO<FolhaPontoPreviewDTO>> previewGet(
            @RequestParam Long professorId,
            @RequestParam(required = false) Long gradeId,
            @RequestParam Integer mes,
            @RequestParam Integer ano) {
        FolhaPontoRequisicaoDTO requisicao = new FolhaPontoRequisicaoDTO(professorId, gradeId, mes, ano);
        FolhaPontoPreviewDTO preview = folhaPontoService.montarPreview(requisicao);
        return ResponseEntity.ok(RespostaPadraoDTO.sucesso("Prévia da folha de ponto gerada com sucesso.", preview));
    }

    @PostMapping("/gerar")
    @Operation(summary = "Gerar documento DOCX da Folha de Ponto", description = "Consulta os dados no banco, preenche o modelo oficial da Folha de Ponto e retorna o arquivo .docx para download")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Documento DOCX gerado e retornado para download"),
            @ApiResponse(responseCode = "400", description = "Parâmetros inválidos"),
            @ApiResponse(responseCode = "404", description = "Professor ou grade não encontrados")
    })
    public ResponseEntity<byte[]> gerarPost(@Valid @RequestBody FolhaPontoRequisicaoDTO requisicao) {
        byte[] docxBytes = folhaPontoService.gerarFolhaPonto(requisicao);
        String nomeArquivo = String.format("Folha_de_Ponto_Professor_%d_%02d_%d.docx",
                requisicao.professorId(), requisicao.mes(), requisicao.ano());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(DOCX_MEDIA_TYPE))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nomeArquivo + "\"")
                .body(docxBytes);
    }

    @GetMapping("/gerar")
    @Operation(summary = "Gerar documento DOCX da Folha de Ponto via GET", description = "Permite o download direto do documento DOCX via link ou requisição GET")
    public ResponseEntity<byte[]> gerarGet(
            @RequestParam Long professorId,
            @RequestParam(required = false) Long gradeId,
            @RequestParam Integer mes,
            @RequestParam Integer ano) {
        FolhaPontoRequisicaoDTO requisicao = new FolhaPontoRequisicaoDTO(professorId, gradeId, mes, ano);
        byte[] docxBytes = folhaPontoService.gerarFolhaPonto(requisicao);
        String nomeArquivo = String.format("Folha_de_Ponto_Professor_%d_%02d_%d.docx",
                requisicao.professorId(), requisicao.mes(), requisicao.ano());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(DOCX_MEDIA_TYPE))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nomeArquivo + "\"")
                .body(docxBytes);
    }
}
