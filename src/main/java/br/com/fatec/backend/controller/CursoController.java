package br.com.fatec.backend.controller;

import br.com.fatec.backend.dto.common.RespostaPadraoDTO;
import br.com.fatec.backend.dto.curso.CursoRequisicaoDTO;
import br.com.fatec.backend.dto.curso.CursoRespostaDTO;
import br.com.fatec.backend.service.CursoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cursos")
public class CursoController {

    private final CursoService service;

    public CursoController(CursoService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<CursoRespostaDTO>> listar() {
        return ResponseEntity.ok(service.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RespostaPadraoDTO<CursoRespostaDTO>> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(RespostaPadraoDTO.sucesso(
                "Curso encontrado com sucesso.", service.buscarPorId(id)));
    }

    @PostMapping
    public ResponseEntity<RespostaPadraoDTO<CursoRespostaDTO>> criar(
            @Valid @RequestBody CursoRequisicaoDTO requisicao) {
        return ResponseEntity.status(HttpStatus.CREATED).body(RespostaPadraoDTO.criado(
                "Curso cadastrado com sucesso.", service.criar(requisicao)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RespostaPadraoDTO<CursoRespostaDTO>> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody CursoRequisicaoDTO requisicao) {
        return ResponseEntity.ok(RespostaPadraoDTO.sucesso(
                "Curso alterado com sucesso.", service.atualizar(id, requisicao)));
    }
}