package br.com.fatec.backend.controller;

import br.com.fatec.backend.dto.common.RespostaPadraoDTO;
import br.com.fatec.backend.dto.extracao_grade.GradeExtraidaDTO;
import br.com.fatec.backend.service.extracao.ExtracaoGradeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/grades/importacao")
@Tag(name = "Extração de Grade (PDF)")
public class ExtracaoGradeController {

    private final ExtracaoGradeService service;

    public ExtracaoGradeController(ExtracaoGradeService service) {
        this.service = service;
    }

    @Operation(summary = "Extrai os dados de uma grade horária em PDF",
            description = "Lê o PDF com o Gemini e devolve os dados extraídos. Não grava nada no banco.")
    @PostMapping(value = "/extrair", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RespostaPadraoDTO<GradeExtraidaDTO>> extrair(
            @RequestParam("arquivo") MultipartFile arquivo) {
        return ResponseEntity.ok(RespostaPadraoDTO.sucesso(
                "Dados da grade extraídos com sucesso.", service.extrair(arquivo)));
    }
    @PostMapping("/validar")
    public ResponseEntity<RespostaPadraoDTO<GradeExtraidaDTO>> validar(
            @Valid @RequestBody GradeExtraidaDTO dados) {
        return ResponseEntity.ok(RespostaPadraoDTO.sucesso(
                "Dados validados com sucesso.", service.validar(dados)));
    }
}