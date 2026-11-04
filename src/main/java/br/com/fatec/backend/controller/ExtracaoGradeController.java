package br.com.fatec.backend.controller;

import br.com.fatec.backend.dto.common.RespostaPadraoDTO;
import br.com.fatec.backend.dto.extracao_grade.GradeExtraidaDTO;
import br.com.fatec.backend.service.extracao.ExtracaoGradeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/grades/importacao")
@Tag(name = "Extração de Grade (PDF)", description = "Operações para extração e validação de grades horárias a partir de arquivos PDF")
public class ExtracaoGradeController {

    private final ExtracaoGradeService service;

    public ExtracaoGradeController(ExtracaoGradeService service) {
        this.service = service;
    }

    @Operation(
            summary = "Extrai os dados de uma grade horária em PDF",
            description = "Lê o PDF com o Google Gemini e retorna os dados estruturados da grade. " +
                    "Utiliza a chave de API fornecida na requisição ou a cadastrada no perfil do usuário. " +
                    "Caso não haja chave de usuário ou esta atinja o limite de quota, utiliza automaticamente a chave padrão do servidor."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Extração realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Arquivo inválido, vazio ou não é um PDF legítimo"),
            @ApiResponse(responseCode = "401", description = "Chave de API do Gemini incorreta ou sem permissão"),
            @ApiResponse(responseCode = "429", description = "Limite de requisições excedido"),
            @ApiResponse(responseCode = "503", description = "Serviço do Google Gemini temporariamente indisponível")
    })
    @PostMapping(value = "/extrair", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RespostaPadraoDTO<GradeExtraidaDTO>> extrair(
            @Parameter(description = "Arquivo PDF da grade horária oficial", required = true)
            @RequestParam("arquivo") MultipartFile arquivo,
            @Parameter(description = "Chave da API do Gemini opcional (sobrescreve a chave do usuário e a do sistema para esta chamada)")
            @RequestParam(value = "apiKey", required = false) String apiKey,
            @Parameter(description = "Chave da API do Gemini via Header opcional", hidden = true)
            @RequestHeader(value = "X-Gemini-Api-Key", required = false) String apiKeyHeader) {

        String chaveFinal = (apiKey != null && !apiKey.isBlank()) ? apiKey : apiKeyHeader;
        GradeExtraidaDTO resultado = service.extrair(arquivo, chaveFinal);

        return ResponseEntity.ok(RespostaPadraoDTO.sucesso(
                "Dados da grade extraídos com sucesso.", resultado));
    }

    @Operation(
            summary = "Valida os dados extraídos de uma grade",
            description = "Valida regras de negócio da grade extraída (conflito de horários, quantidade de aulas, integridade dos campos)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dados válidos"),
            @ApiResponse(responseCode = "400", description = "Problemas e inconsistências encontrados nos dados")
    })
    @PostMapping("/validar")
    public ResponseEntity<RespostaPadraoDTO<GradeExtraidaDTO>> validar(
            @Valid @RequestBody GradeExtraidaDTO dados) {
        return ResponseEntity.ok(RespostaPadraoDTO.sucesso(
                "Dados validados com sucesso.", service.validar(dados)));
    }
}
