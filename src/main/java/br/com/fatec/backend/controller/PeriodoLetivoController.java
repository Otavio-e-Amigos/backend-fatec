package br.com.fatec.backend.controller;

import br.com.fatec.backend.dto.common.RespostaPadraoDTO;
import br.com.fatec.backend.dto.periodo_letivo.PeriodoLetivoRequisicaoDTO;
import br.com.fatec.backend.dto.periodo_letivo.PeriodoLetivoRespostaDTO;
import br.com.fatec.backend.service.PeriodoLetivoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/periodos-letivos")
public class PeriodoLetivoController {

    private final PeriodoLetivoService service;

    public PeriodoLetivoController(PeriodoLetivoService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<RespostaPadraoDTO<PeriodoLetivoRespostaDTO>> criar(
            @Valid @RequestBody PeriodoLetivoRequisicaoDTO requisicao) {

        PeriodoLetivoRespostaDTO dto = service.criar(requisicao);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(RespostaPadraoDTO.criado("Período letivo cadastrado com sucesso.", dto));
    }

    @GetMapping
    public ResponseEntity<RespostaPadraoDTO<List<PeriodoLetivoRespostaDTO>>> listarTodos() {
        List<PeriodoLetivoRespostaDTO> lista = service.listarTodos();
        return ResponseEntity.ok(RespostaPadraoDTO.sucesso("Períodos letivos listados com sucesso.", lista));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RespostaPadraoDTO<PeriodoLetivoRespostaDTO>> buscarPorId(@PathVariable Long id) {
        PeriodoLetivoRespostaDTO dto = service.buscarPorId(id);
        return ResponseEntity.ok(RespostaPadraoDTO.sucesso("Período letivo encontrado com sucesso.", dto));
    }
}