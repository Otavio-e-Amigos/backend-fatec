package br.com.fatec.backend.controller;

import br.com.fatec.backend.dto.calendario.EventoCalendarioRequisicaoDTO;
import br.com.fatec.backend.dto.calendario.EventoCalendarioRespostaDTO;
import br.com.fatec.backend.dto.common.RespostaPadraoDTO;
import br.com.fatec.backend.service.CalendarioAcademicoService;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/calendario")
@Tag(name = "Calendário Acadêmico", description = "CRUD em memória para gestão de feriados e dias sem aula do calendário acadêmico FATEC")
public class CalendarioController {

    private final CalendarioAcademicoService calendarioService;

    public CalendarioController(CalendarioAcademicoService calendarioService) {
        this.calendarioService = calendarioService;
    }

    // -------------------------------------------------------------------------
    // GET /api/calendario  — Lista todos os eventos
    // -------------------------------------------------------------------------
    @GetMapping
    @Operation(
        summary = "Listar todos os eventos",
        description = "Retorna todos os eventos do calendário acadêmico cadastrados em memória, ordenados por data."
    )
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    public ResponseEntity<RespostaPadraoDTO<List<EventoCalendarioRespostaDTO>>> listarTodos() {
        List<EventoCalendarioRespostaDTO> eventos = calendarioService.listarTodos();
        return ResponseEntity.ok(RespostaPadraoDTO.sucesso("Eventos do calendário listados com sucesso.", eventos));
    }

    // -------------------------------------------------------------------------
    // GET /api/calendario/{data}  — Busca evento por data
    // -------------------------------------------------------------------------
    @GetMapping("/{data}")
    @Operation(
        summary = "Buscar evento por data",
        description = "Retorna o evento cadastrado para a data informada (formato: YYYY-MM-DD)."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Evento encontrado"),
        @ApiResponse(responseCode = "404", description = "Nenhum evento encontrado para a data")
    })
    public ResponseEntity<RespostaPadraoDTO<EventoCalendarioRespostaDTO>> buscarPorData(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        EventoCalendarioRespostaDTO evento = calendarioService.buscarPorData(data);
        return ResponseEntity.ok(RespostaPadraoDTO.sucesso("Evento encontrado.", evento));
    }

    // -------------------------------------------------------------------------
    // POST /api/calendario  — Adiciona novo evento
    // -------------------------------------------------------------------------
    @PostMapping
    @Operation(
        summary = "Adicionar evento ao calendário",
        description = "Cadastra um novo feriado ou dia sem aula em memória. Não é possível cadastrar duas vezes a mesma data."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Evento criado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos ou data já cadastrada")
    })
    public ResponseEntity<RespostaPadraoDTO<EventoCalendarioRespostaDTO>> adicionar(
            @Valid @RequestBody EventoCalendarioRequisicaoDTO requisicao) {
        EventoCalendarioRespostaDTO criado = calendarioService.adicionar(requisicao);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(RespostaPadraoDTO.sucesso("Evento adicionado ao calendário com sucesso.", criado));
    }

    // -------------------------------------------------------------------------
    // PUT /api/calendario/{data}  — Atualiza evento existente
    // -------------------------------------------------------------------------
    @PutMapping("/{data}")
    @Operation(
        summary = "Atualizar evento do calendário",
        description = "Atualiza a descrição, tipo e flag de sem-aula de um evento já cadastrado."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Evento atualizado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Nenhum evento encontrado para a data")
    })
    public ResponseEntity<RespostaPadraoDTO<EventoCalendarioRespostaDTO>> atualizar(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
            @Valid @RequestBody EventoCalendarioRequisicaoDTO requisicao) {
        EventoCalendarioRespostaDTO atualizado = calendarioService.atualizar(data, requisicao);
        return ResponseEntity.ok(RespostaPadraoDTO.sucesso("Evento atualizado com sucesso.", atualizado));
    }

    // -------------------------------------------------------------------------
    // DELETE /api/calendario/{data}  — Remove evento
    // -------------------------------------------------------------------------
    @DeleteMapping("/{data}")
    @Operation(
        summary = "Remover evento do calendário",
        description = "Remove um evento da memória pelo seu identificador de data."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Evento removido com sucesso"),
        @ApiResponse(responseCode = "404", description = "Nenhum evento encontrado para a data")
    })
    public ResponseEntity<RespostaPadraoDTO<Void>> excluir(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        calendarioService.excluir(data);
        return ResponseEntity.ok(RespostaPadraoDTO.sucesso("Evento removido do calendário com sucesso.", null));
    }
}
