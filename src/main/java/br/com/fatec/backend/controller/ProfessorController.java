package br.com.fatec.backend.controller;

import br.com.fatec.backend.dto.common.RespostaPadraoDTO;
import br.com.fatec.backend.dto.professor.ProfessorRequisicaoDTO;
import br.com.fatec.backend.dto.professor.ProfessorRespostaDTO;
import br.com.fatec.backend.dto.professor.ProfessorStatusRequisicaoDTO;
import br.com.fatec.backend.service.ProfessorService;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RestController
@RequestMapping("/api/professores")
public class ProfessorController {

    private final ProfessorService service;

    public ProfessorController(ProfessorService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<Page<ProfessorRespostaDTO>> listar(
            @RequestParam(required = false, defaultValue = "") String busca,
            @ParameterObject Pageable pageable) {

        return ResponseEntity.ok(service.listar(busca, pageable));
    }

    @GetMapping("/todos")
    public ResponseEntity<List<ProfessorRespostaDTO>> listarTodos() {

        return ResponseEntity.ok(service.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RespostaPadraoDTO<ProfessorRespostaDTO>> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(RespostaPadraoDTO.sucesso(
                "Professor encontrado com sucesso.", service.buscarPorId(id)));
    }

    @PostMapping
    public ResponseEntity<RespostaPadraoDTO<ProfessorRespostaDTO>> criar(
            @Valid @RequestBody ProfessorRequisicaoDTO requisicao) {
        return ResponseEntity.status(HttpStatus.CREATED).body(RespostaPadraoDTO.criado(
                "Professor cadastrado com sucesso.", service.criar(requisicao)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RespostaPadraoDTO<ProfessorRespostaDTO>> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody ProfessorRequisicaoDTO requisicao) {
        return ResponseEntity.ok(RespostaPadraoDTO.sucesso(
                "Professor alterado com sucesso.", service.atualizar(id, requisicao)));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<RespostaPadraoDTO<Void>> alterarStatus(
            @PathVariable Long id,
            @Valid @RequestBody ProfessorStatusRequisicaoDTO requisicao) {
        service.alterarStatus(id, requisicao.status());
        return ResponseEntity.ok(RespostaPadraoDTO.sucesso("Status do professor alterado com sucesso."));
    }
}