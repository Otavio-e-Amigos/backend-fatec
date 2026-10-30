package br.com.fatec.backend.service;

import br.com.fatec.backend.dto.calendario.EventoCalendarioRequisicaoDTO;
import br.com.fatec.backend.dto.calendario.EventoCalendarioRespostaDTO;
import br.com.fatec.backend.exception.RegraNegocioException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.MonthDay;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Serviço responsável por gerenciar o Calendário Acadêmico da FATEC.
 * Os eventos são mantidos exclusivamente em memória (sem banco de dados).
 * Suporta CRUD completo: listar, buscar, adicionar, atualizar e excluir eventos.
 */
@Service
public class CalendarioAcademicoService {

    /**
     * Feriados anuais recorrentes (mês e dia) — pré-carregados e imutáveis.
     */
    private final Map<MonthDay, String> feriadosFixos = new java.util.HashMap<>();

    /**
     * Eventos gerenciáveis em memória (data completa → EventoCalendario).
     * ConcurrentSkipListMap mantém ordenação por data e é thread-safe.
     */
    private final ConcurrentSkipListMap<LocalDate, EventoCalendario> eventosEmMemoria = new ConcurrentSkipListMap<>();

    // -------------------------------------------------------------------------
    // Estrutura interna para armazenar um evento
    // -------------------------------------------------------------------------
    private record EventoCalendario(String descricao, String tipo, boolean semAula) {}

    // -------------------------------------------------------------------------
    // Inicialização
    // -------------------------------------------------------------------------
    public CalendarioAcademicoService() {
        inicializarFeriadosFixos();
        inicializarEventosIniciais();
    }

    private void inicializarFeriadosFixos() {
        feriadosFixos.put(MonthDay.of(1, 1), "Confraternização Universal");
        feriadosFixos.put(MonthDay.of(4, 21), "Tiradentes");
        feriadosFixos.put(MonthDay.of(5, 1), "Dia do Trabalho");
        feriadosFixos.put(MonthDay.of(7, 9), "Revolução Constitucionalista (SP)");
        feriadosFixos.put(MonthDay.of(9, 7), "Feriado - Independência do Brasil");
        feriadosFixos.put(MonthDay.of(10, 12), "Feriado - Nossa Senhora Aparecida");
        feriadosFixos.put(MonthDay.of(10, 15), "Dia do Professor");
        feriadosFixos.put(MonthDay.of(10, 28), "Dia do Servidor Público");
        feriadosFixos.put(MonthDay.of(11, 2), "Feriado - Dia de Finados");
        feriadosFixos.put(MonthDay.of(11, 15), "Feriado - Proclamação da República");
        feriadosFixos.put(MonthDay.of(11, 20), "Feriado - Dia da Consciência Negra");
        feriadosFixos.put(MonthDay.of(12, 25), "Natal");
    }

    private void inicializarEventosIniciais() {
        // Eventos específicos do calendário acadêmico FATEC 2026
        adicionarInterno(LocalDate.of(2026, 9, 7), "Feriado - Independência do Brasil", "FERIADO", true);
        adicionarInterno(LocalDate.of(2026, 10, 12), "Feriado - Nossa Senhora Aparecida", "FERIADO", true);
        adicionarInterno(LocalDate.of(2026, 10, 15), "Dia do Professor", "SEM_AULA", true);
        adicionarInterno(LocalDate.of(2026, 10, 28), "Dia do Servidor Público", "SEM_AULA", true);
        adicionarInterno(LocalDate.of(2026, 11, 2), "Feriado - Dia de Finados", "FERIADO", true);
        adicionarInterno(LocalDate.of(2026, 11, 15), "Feriado - Proclamação da República", "FERIADO", true);
        adicionarInterno(LocalDate.of(2026, 11, 20), "Feriado - Dia da Consciência Negra", "FERIADO", true);
        adicionarInterno(LocalDate.of(2026, 11, 21), "Emenda - Dia da Consciência Negra", "EMENDA", true);
        adicionarInterno(LocalDate.of(2026, 12, 25), "Natal", "FERIADO", true);
    }

    private void adicionarInterno(LocalDate data, String descricao, String tipo, boolean semAula) {
        eventosEmMemoria.put(data, new EventoCalendario(descricao, tipo, semAula));
    }

    // -------------------------------------------------------------------------
    // CRUD em memória
    // -------------------------------------------------------------------------

    /**
     * Lista todos os eventos do calendário (feriados em memória), ordenados por data.
     */
    public List<EventoCalendarioRespostaDTO> listarTodos() {
        List<EventoCalendarioRespostaDTO> resultado = new ArrayList<>();
        for (Map.Entry<LocalDate, EventoCalendario> entry : eventosEmMemoria.entrySet()) {
            resultado.add(toDTO(entry.getKey(), entry.getValue()));
        }
        return resultado;
    }

    /**
     * Busca um evento por data específica.
     *
     * @throws RegraNegocioException se a data não possuir evento cadastrado
     */
    public EventoCalendarioRespostaDTO buscarPorData(LocalDate data) {
        EventoCalendario evento = eventosEmMemoria.get(data);
        if (evento == null) {
            throw new RegraNegocioException("Nenhum evento encontrado para a data: " + data);
        }
        return toDTO(data, evento);
    }

    /**
     * Adiciona um novo evento ao calendário em memória.
     *
     * @throws RegraNegocioException se já existir um evento cadastrado nessa data
     */
    public EventoCalendarioRespostaDTO adicionar(EventoCalendarioRequisicaoDTO req) {
        if (eventosEmMemoria.containsKey(req.data())) {
            throw new RegraNegocioException("Já existe um evento cadastrado para a data: " + req.data() +
                    ". Use PUT para atualizar.");
        }
        boolean semAula = req.semAula() == null || req.semAula();
        String tipo = req.tipo() != null ? req.tipo() : "SEM_AULA";
        EventoCalendario evento = new EventoCalendario(req.descricao(), tipo, semAula);
        eventosEmMemoria.put(req.data(), evento);
        return toDTO(req.data(), evento);
    }

    /**
     * Atualiza um evento existente na data informada.
     *
     * @throws RegraNegocioException se não existir evento na data
     */
    public EventoCalendarioRespostaDTO atualizar(LocalDate data, EventoCalendarioRequisicaoDTO req) {
        if (!eventosEmMemoria.containsKey(data)) {
            throw new RegraNegocioException("Nenhum evento encontrado para a data: " + data);
        }
        boolean semAula = req.semAula() == null || req.semAula();
        String tipo = req.tipo() != null ? req.tipo() : "SEM_AULA";
        EventoCalendario eventoAtualizado = new EventoCalendario(req.descricao(), tipo, semAula);
        eventosEmMemoria.put(data, eventoAtualizado);
        return toDTO(data, eventoAtualizado);
    }

    /**
     * Remove um evento do calendário em memória.
     *
     * @throws RegraNegocioException se não existir evento na data
     */
    public void excluir(LocalDate data) {
        if (!eventosEmMemoria.containsKey(data)) {
            throw new RegraNegocioException("Nenhum evento encontrado para a data: " + data);
        }
        eventosEmMemoria.remove(data);
    }

    // -------------------------------------------------------------------------
    // Consultas usadas internamente (ex: geração da Folha de Ponto)
    // -------------------------------------------------------------------------

    /**
     * Verifica se uma data é dia sem aula (feriado fixo anual ou evento em memória com semAula=true).
     */
    public boolean isDiaSemAula(LocalDate data) {
        if (data == null) {
            return false;
        }
        EventoCalendario evento = eventosEmMemoria.get(data);
        if (evento != null) {
            return evento.semAula();
        }
        MonthDay md = MonthDay.of(data.getMonthValue(), data.getDayOfMonth());
        return feriadosFixos.containsKey(md);
    }

    /**
     * Retorna o motivo de um dia sem aula, ou null se não houver.
     */
    public String getMotivoDiaSemAula(LocalDate data) {
        if (data == null) {
            return null;
        }
        EventoCalendario evento = eventosEmMemoria.get(data);
        if (evento != null) {
            return evento.descricao();
        }
        MonthDay md = MonthDay.of(data.getMonthValue(), data.getDayOfMonth());
        return feriadosFixos.get(md);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------
    private EventoCalendarioRespostaDTO toDTO(LocalDate data, EventoCalendario evento) {
        return new EventoCalendarioRespostaDTO(data, evento.descricao(), evento.tipo(), evento.semAula());
    }
}
