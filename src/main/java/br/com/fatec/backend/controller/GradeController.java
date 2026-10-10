package br.com.fatec.backend.controller;

import br.com.fatec.backend.dto.common.RespostaPadraoDTO;
import br.com.fatec.backend.dto.grade.GradeRequisicaoDTO;
import br.com.fatec.backend.dto.grade.GradeRespostaDTO;
import br.com.fatec.backend.service.GradeService;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class GradeController {

    private final GradeService service;

    public GradeController(GradeService service) {
        this.service = service;
    }

    @GetMapping("/grades")
    public ResponseEntity<Page<GradeRespostaDTO>> listar(
            @ParameterObject @PageableDefault(sort = "id") Pageable pageable) {
        return ResponseEntity.ok(service.listar(pageable));
    }

    @GetMapping("/professores/{id}/grades")
    public ResponseEntity<RespostaPadraoDTO<List<GradeRespostaDTO>>> listarPorProfessor(@PathVariable Long id) {
        List<GradeRespostaDTO> grades = service.listarPorProfessor(id);
        return ResponseEntity.ok(RespostaPadraoDTO.sucesso(
                "Histórico de grades do professor recuperado com sucesso.", grades));
    }

    @GetMapping("/grades/{id}")
    public ResponseEntity<RespostaPadraoDTO<GradeRespostaDTO>> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(RespostaPadraoDTO.sucesso(
                "Grade encontrada com sucesso.", service.buscarPorId(id)));
    }

    @PostMapping("/grades")
    public ResponseEntity<RespostaPadraoDTO<GradeRespostaDTO>> criar(
            @Valid @RequestBody GradeRequisicaoDTO requisicao) {
        return ResponseEntity.status(HttpStatus.CREATED).body(RespostaPadraoDTO.criado(
                "Grade cadastrada com sucesso.", service.criar(requisicao)));
    }

    @PutMapping("/grades/{id}")
    public ResponseEntity<RespostaPadraoDTO<GradeRespostaDTO>> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody GradeRequisicaoDTO requisicao) {
        return ResponseEntity.ok(RespostaPadraoDTO.sucesso(
                "Grade alterada com sucesso.", service.atualizar(id, requisicao)));
    }
}