package br.com.fatec.backend.controller;

import br.com.fatec.backend.dto.common.RespostaPadraoDTO;

import br.com.fatec.backend.dto.disciplina_da_grade.DisciplinaDaGradeRequisicaoDTO;
import br.com.fatec.backend.dto.disciplina_da_grade.DisciplinaDaGradeRespostaDTO;
import br.com.fatec.backend.service.DisciplinaDaGradeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class DisciplinaDaGradeController {

    private final DisciplinaDaGradeService service;

    public DisciplinaDaGradeController(DisciplinaDaGradeService service) {
        this.service = service;
    }

    @GetMapping("/grades/{id}/disciplinas")
    public ResponseEntity<RespostaPadraoDTO<List<DisciplinaDaGradeRespostaDTO>>> listarPorGrade(@PathVariable Long id) {
        List<DisciplinaDaGradeRespostaDTO> lista = service.listarPorGrade(id);
        return ResponseEntity.ok(RespostaPadraoDTO.sucesso(
                "Disciplinas da grade recuperadas com sucesso.", lista));
    }

    @GetMapping("/disciplinas-grade/{id}")
    public ResponseEntity<RespostaPadraoDTO<DisciplinaDaGradeRespostaDTO>> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(RespostaPadraoDTO.sucesso(
                "Associação encontrada com sucesso.", service.buscarPorId(id)));
    }

    @PostMapping("/disciplinas-grade")
    public ResponseEntity<RespostaPadraoDTO<DisciplinaDaGradeRespostaDTO>> criar(
            @Valid @RequestBody DisciplinaDaGradeRequisicaoDTO requisicao) {
        return ResponseEntity.status(HttpStatus.CREATED).body(RespostaPadraoDTO.criado(
                "Disciplina associada à grade com sucesso.", service.criar(requisicao)));
    }

    @PutMapping("/disciplinas-grade/{id}")
    public ResponseEntity<RespostaPadraoDTO<DisciplinaDaGradeRespostaDTO>> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody DisciplinaDaGradeRequisicaoDTO requisicao) {
        return ResponseEntity.ok(RespostaPadraoDTO.sucesso(
                "Associação alterada com sucesso.", service.atualizar(id, requisicao)));
    }

    @DeleteMapping("/disciplinas-grade/{id}")
    public ResponseEntity<RespostaPadraoDTO<Void>> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.ok(RespostaPadraoDTO.sucesso("Associação removida com sucesso.", null));
    }
}