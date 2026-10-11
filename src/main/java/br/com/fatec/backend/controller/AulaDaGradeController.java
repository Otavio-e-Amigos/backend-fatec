package br.com.fatec.backend.controller;

import br.com.fatec.backend.dto.common.RespostaPadraoDTO;
import br.com.fatec.backend.dto.aula_da_grade.AulaDaGradeRequisicaoDTO;
import br.com.fatec.backend.dto.aula_da_grade.AulaDaGradeRespostaDTO;
import br.com.fatec.backend.service.AulaDaGradeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AulaDaGradeController {

    private final AulaDaGradeService service;

    public AulaDaGradeController(AulaDaGradeService service) {
        this.service = service;
    }

    @GetMapping("/disciplinas-grade/{id}/aulas")
    public ResponseEntity<RespostaPadraoDTO<List<AulaDaGradeRespostaDTO>>> listarPorDisciplinaDaGrade(@PathVariable Long id) {
        List<AulaDaGradeRespostaDTO> lista = service.listarPorDisciplinaDaGrade(id);
        return ResponseEntity.ok(RespostaPadraoDTO.sucesso("Aulas recuperadas com sucesso.", lista));
    }

    @PostMapping("/aulas-grade/disciplinas-grade/{id}/aulas")
    public ResponseEntity<RespostaPadraoDTO<AulaDaGradeRespostaDTO>> registrarAula(
            @PathVariable Long id,
            @Valid @RequestBody AulaDaGradeRequisicaoDTO requisicao) {
        AulaDaGradeRespostaDTO resposta = service.registrarAula(id, requisicao);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(RespostaPadraoDTO.criado("Horário de aula registrado com sucesso.", resposta));
    }

    @DeleteMapping("/aulas-grade/{id}")
    public ResponseEntity<RespostaPadraoDTO<Void>> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.ok(RespostaPadraoDTO.sucesso("Horário de aula removido com sucesso.", null));
    }
}