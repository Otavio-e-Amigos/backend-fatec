package br.com.fatec.backend.controller;

import br.com.fatec.backend.dto.common.RespostaPadraoDTO;
import br.com.fatec.backend.dto.professor.ProfessorRequisicaoDTO;
import br.com.fatec.backend.dto.professor.ProfessorRespostaDTO;
import br.com.fatec.backend.service.ProfessorService;
import jakarta.validation.Valid;
import br.com.fatec.backend.dto.professor.ProfessorAtualizacaoDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/professores")
public class ProfessorController {

    private final ProfessorService service;

    public ProfessorController(ProfessorService service) {
        this.service = service;
    }

    @PreAuthorize("hasAnyRole('TI', 'RESPONSAVEL')")
    @GetMapping
    public ResponseEntity<RespostaPadraoDTO<List<ProfessorRespostaDTO>>> listarTodos() {
        List<ProfessorRespostaDTO> professores = service.listarTodos();

        return ResponseEntity.ok(
                RespostaPadraoDTO.sucesso(
                        "Lista de docentes obtida com sucesso.",
                        professores
                )
        );
    }

    @PreAuthorize("hasAnyRole('TI', 'RESPONSAVEL')")
    @GetMapping("/{id}")
    public ResponseEntity<RespostaPadraoDTO<ProfessorRespostaDTO>> buscarPorId(
            @PathVariable Long id) {

        ProfessorRespostaDTO dados = service.buscarPorId(id);

        return ResponseEntity.ok(
                RespostaPadraoDTO.sucesso(
                        "Professor encontrado com sucesso.",
                        dados
                )
        );
    }

    @PreAuthorize("hasAnyRole('TI', 'RESPONSAVEL')")
    @PostMapping
    public ResponseEntity<RespostaPadraoDTO<ProfessorRespostaDTO>> criar(
            @Valid @RequestBody ProfessorRequisicaoDTO requisicao) {

        ProfessorRespostaDTO novoProfessor = service.criar(requisicao);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        RespostaPadraoDTO.criado(
                                "Professor cadastrado com sucesso.",
                                novoProfessor
                        )
                );
    }

    @PreAuthorize("hasAnyRole('TI', 'RESPONSAVEL')")
    @PutMapping("/{id}")
    public ResponseEntity<RespostaPadraoDTO<ProfessorRespostaDTO>> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody ProfessorAtualizacaoDTO requisicao) {

        ProfessorRespostaDTO professorAtualizado = service.atualizar(id, requisicao);

        return ResponseEntity.ok(
                RespostaPadraoDTO.sucesso(
                        "O professor foi alterado com sucesso.",
                        professorAtualizado
                )
        );
    }

    @PreAuthorize("hasAnyRole('TI', 'RESPONSAVEL')")
    @PatchMapping("/{id}/ativar")
    public ResponseEntity<RespostaPadraoDTO<Void>> ativar(
            @PathVariable Long id) {

        service.ativar(id);

        return ResponseEntity.ok(
                RespostaPadraoDTO.sucesso(
                        "O professor foi ativado com sucesso."
                )
        );
    }

    @PreAuthorize("hasAnyRole('TI', 'RESPONSAVEL')")
    @PatchMapping("/{id}/desativar")
    public ResponseEntity<RespostaPadraoDTO<Void>> desativar(
            @PathVariable Long id) {

        service.desativar(id);

        return ResponseEntity.ok(
                RespostaPadraoDTO.sucesso(
                        "O professor foi desativado com sucesso."
                )
        );
    }
}