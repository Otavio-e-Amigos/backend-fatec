package br.com.fatec.backend.controller;

import br.com.fatec.backend.dto.common.RespostaPadraoDTO;
import br.com.fatec.backend.dto.disciplina.DisciplinaRequisicaoDTO;
import br.com.fatec.backend.dto.disciplina.DisciplinaRespostaDTO;
import br.com.fatec.backend.service.DisciplinaService;
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
@RequestMapping("/api/disciplinas")
public class DisciplinaController {

    private final DisciplinaService service;

    public DisciplinaController(DisciplinaService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<Page<DisciplinaRespostaDTO>> listar(
            @RequestParam(required = false, defaultValue = "") String busca,
            @ParameterObject @PageableDefault(sort = "nome") Pageable pageable) {
        return ResponseEntity.ok(service.listar(busca, pageable));
    }

    @GetMapping("/todas")
    public ResponseEntity<List<DisciplinaRespostaDTO>> listarTodos() {
        return ResponseEntity.ok(service.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RespostaPadraoDTO<DisciplinaRespostaDTO>> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(RespostaPadraoDTO.sucesso(
                "Disciplina encontrada com sucesso.", service.buscarPorId(id)));
    }

    @PostMapping
    public ResponseEntity<RespostaPadraoDTO<DisciplinaRespostaDTO>> criar(
            @Valid @RequestBody DisciplinaRequisicaoDTO requisicao) {
        return ResponseEntity.status(HttpStatus.CREATED).body(RespostaPadraoDTO.criado(
                "Disciplina cadastrada com sucesso.", service.criar(requisicao)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RespostaPadraoDTO<DisciplinaRespostaDTO>> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody DisciplinaRequisicaoDTO requisicao) {
        return ResponseEntity.ok(RespostaPadraoDTO.sucesso(
                "Disciplina alterada com sucesso.", service.atualizar(id, requisicao)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<RespostaPadraoDTO<Void>> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.ok(RespostaPadraoDTO.sucesso("Disciplina excluída com sucesso.", null));
    }
}