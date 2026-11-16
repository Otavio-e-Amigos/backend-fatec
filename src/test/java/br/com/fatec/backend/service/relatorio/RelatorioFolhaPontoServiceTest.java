package br.com.fatec.backend.service.relatorio;

import br.com.fatec.backend.dto.relatorio.RelatorioFolhaPontoDTO;
import br.com.fatec.backend.entity.*;
import br.com.fatec.backend.enums.*;
import br.com.fatec.backend.exception.RecursoNaoEncontradoException;
import br.com.fatec.backend.repository.DisciplinaDaGradeRepository;
import br.com.fatec.backend.repository.GradeRepository;
import br.com.fatec.backend.repository.ProfessorRepository;
import br.com.fatec.backend.repository.RegistroFolhaPontoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RelatorioFolhaPontoServiceTest {

    @Mock
    private RegistroFolhaPontoRepository registroRepository;
    @Mock
    private ProfessorRepository professorRepository;
    @Mock
    private GradeRepository gradeRepository;
    @Mock
    private DisciplinaDaGradeRepository disciplinaDaGradeRepository;

    private RelatorioFolhaPontoService service;
    private Professor katia;
    private Professor joao;
    private Grade grade;
    private Usuario ana;

    private final LocalDateTime inicioNov = LocalDateTime.of(2026, 11, 1, 0, 0);
    private final LocalDateTime fimNov = LocalDateTime.of(2026, 12, 1, 0, 0);

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-11-18T15:00:00Z"), ZoneId.of("America/Sao_Paulo"));
        service = new RelatorioFolhaPontoService(registroRepository, professorRepository, gradeRepository,
                disciplinaDaGradeRepository, clock);

        katia = professor(1L, "Katia", "M1");
        joao = professor(2L, "João", "M2");
        ana = new Usuario("Ana", "ana", "hash", Perfil.RESPONSAVEL);
        ReflectionTestUtils.setField(ana, "id", 9L);

        PeriodoLetivo periodo = new PeriodoLetivo(2, 2026, LocalDate.of(2026, 8, 1), LocalDate.of(2026, 12, 15));
        grade = new Grade(katia, periodo, new BigDecimal("20.00"), new BigDecimal("80.00"), new BigDecimal("400.00"));
        ReflectionTestUtils.setField(grade, "id", 10L);
    }

    private static Professor professor(long id, String nome, String matricula) {
        Professor p = new Professor(nome, null, "1234567890" + id, matricula,
                RegimeContrato.INDETERMINADO, RegimeJuridico.CLT, Titulacao.MESTRE);
        ReflectionTestUtils.setField(p, "id", id);
        return p;
    }

    private RegistroFolhaPonto folha(long id, Professor p, int mes, LocalDateTime quando, boolean outroPeriodo) {
        RegistroFolhaPonto r = new RegistroFolhaPonto(p, grade, ana, mes, 2026, 2026, 2, outroPeriodo, 1, 3);
        ReflectionTestUtils.setField(r, "id", id);
        ReflectionTestUtils.setField(r, "createdAt", quando);
        return r;
    }

    @Test
    @DisplayName("Relatório geral compara professores, conta regerações e ativos sem folha")
    void relatorioGeral() {
        List<RegistroFolhaPonto> registros = List.of(
                folha(1, katia, 10, LocalDateTime.of(2026, 11, 3, 10, 0), false),
                folha(2, katia, 10, LocalDateTime.of(2026, 11, 4, 10, 0), false),
                folha(3, joao, 11, LocalDateTime.of(2026, 11, 5, 10, 0), true)
        );
        when(registroRepository.buscarNoIntervalo(inicioNov, fimNov)).thenReturn(registros);
        when(professorRepository.countByStatus(StatusProfessor.ATIVO)).thenReturn(5L);
        when(registroRepository.totaisHistoricosPorProfessor(anyCollection())).thenReturn(List.of(
                new Object[]{1L, 7L, LocalDateTime.of(2026, 11, 4, 10, 0)},
                new Object[]{2L, 1L, LocalDateTime.of(2026, 11, 5, 10, 0)}));

        RelatorioFolhaPontoDTO r = service.gerar("MENSAL", null, null);

        assertEquals(RelatorioFolhaPontoDTO.ESCOPO_TODOS, r.escopo());
        assertNull(r.professor());
        assertEquals(3, r.resumo().totalFolhas());
        assertEquals(2, r.resumo().professoresAtendidos());
        assertEquals(5L, r.resumo().professoresAtivos());
        assertEquals(3L, r.resumo().professoresAtivosSemFolha());
        assertEquals(1, r.resumo().regeracoes());
        assertEquals(1, r.resumo().folhasComGradeDeOutroPeriodo());
        assertEquals(2, r.resumo().competenciasDistintas());

        var primeiro = r.porProfessor().get(0);
        assertEquals("Katia", primeiro.nome());
        assertEquals(2, primeiro.folhasNoPeriodo());
        assertEquals(7, primeiro.totalHistorico());
        assertEquals(new BigDecimal("66.7"), primeiro.percentualDoPeriodo());
        assertEquals(List.of("10/2026"), primeiro.competencias());

        assertEquals(30, r.evolucaoDiaria().size());
        assertEquals(3, r.historico().size());
        assertEquals("Ana", r.porUsuario().get(0).usuario());
    }

    @Test
    @DisplayName("Relatório individual traz grades, disciplinas principais e histórico do professor")
    void relatorioIndividual() {
        when(professorRepository.findById(1L)).thenReturn(Optional.of(katia));
        when(registroRepository.buscarNoIntervaloPorProfessor(1L, inicioNov, fimNov))
                .thenReturn(List.of(folha(1, katia, 10, LocalDateTime.of(2026, 11, 3, 10, 0), false)));
        when(registroRepository.totaisHistoricosPorProfessor(anyCollection()))
                .thenReturn(List.<Object[]>of(new Object[]{1L, 4L, LocalDateTime.of(2026, 11, 3, 10, 0)}));
        when(registroRepository.resumoHistoricoDoProfessor(1L)).thenReturn(List.<Object[]>of(
                new Object[]{4L, LocalDateTime.of(2026, 3, 2, 8, 0), LocalDateTime.of(2026, 11, 3, 10, 0)}));
        when(registroRepository.totaisPorGradeDoProfessor(1L)).thenReturn(List.<Object[]>of(new Object[]{10L, 4L}));
        when(gradeRepository.findByProfessorIdComPeriodo(1L)).thenReturn(List.of(grade));

        Curso ads = new Curso("Análise e Desenvolvimento de Sistemas", Turno.NOITE, "ADS");
        Disciplina es = new Disciplina("ES3", "ESIII", "Engenharia de Software III");
        ReflectionTestUtils.setField(es, "id", 50L);
        Disciplina bd = new Disciplina("BD1", "BDI", "Banco de Dados");
        ReflectionTestUtils.setField(bd, "id", 51L);
        DisciplinaDaGrade v1 = new DisciplinaDaGrade(grade, es, ads, (short) 4, "NOITE", (short) 4, 1, StatusDisciplinaGrade.MINISTRANDO);
        DisciplinaDaGrade v2 = new DisciplinaDaGrade(grade, bd, ads, (short) 2, "NOITE", (short) 2, 1, StatusDisciplinaGrade.CANCELADA);
        when(disciplinaDaGradeRepository.findByProfessorIdComDetalhes(1L)).thenReturn(List.of(v1, v2));

        RelatorioFolhaPontoDTO r = service.gerar("MENSAL", LocalDate.of(2026, 11, 10), 1L);

        assertEquals(RelatorioFolhaPontoDTO.ESCOPO_INDIVIDUAL, r.escopo());
        assertNull(r.resumo().professoresAtivos());
        var detalhe = r.professor();
        assertEquals("Katia", detalhe.nome());
        assertEquals(4, detalhe.totalFolhasHistorico());
        assertEquals(1, detalhe.grades().size());
        var g = detalhe.grades().get(0);
        assertEquals("2º/2026", g.periodoLetivo());
        assertEquals(2, g.disciplinas());
        assertEquals(1, g.disciplinasMinistrando());
        assertEquals(4, g.aulasSemanais());
        assertEquals(4, g.folhasGeradas());
        assertEquals("ESIII", detalhe.principaisDisciplinas().get(0).sigla());
        assertEquals(List.of("Análise e Desenvolvimento de Sistemas (NOITE)"), detalhe.cursos());
    }

    @Test
    @DisplayName("Professor inexistente responde 404")
    void professorInexistente() {
        when(professorRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RecursoNaoEncontradoException.class, () -> service.gerar("DIARIO", null, 99L));
    }
}
