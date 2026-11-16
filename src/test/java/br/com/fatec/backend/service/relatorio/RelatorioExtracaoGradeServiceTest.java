package br.com.fatec.backend.service.relatorio;

import br.com.fatec.backend.dto.relatorio.RelatorioExtracaoGradeDTO;
import br.com.fatec.backend.entity.Professor;
import br.com.fatec.backend.entity.RegistroExtracaoGrade;
import br.com.fatec.backend.entity.Usuario;
import br.com.fatec.backend.enums.*;
import br.com.fatec.backend.repository.RegistroExtracaoGradeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RelatorioExtracaoGradeServiceTest {

    @Mock
    private RegistroExtracaoGradeRepository repository;

    private final Clock clock = Clock.fixed(Instant.parse("2026-11-18T15:00:00Z"), ZoneId.of("America/Sao_Paulo"));

    static Usuario usuario(long id, String nome) {
        Usuario u = new Usuario(nome, "login" + id, "hash", Perfil.RESPONSAVEL);
        ReflectionTestUtils.setField(u, "id", id);
        return u;
    }

    static RegistroExtracaoGrade registro(long id, Usuario usuario, StatusExtracaoGrade status, LocalDateTime quando,
                                          Professor cadastrado, String matricula, int disciplinas, int aulas, int avisos, long ms) {
        RegistroExtracaoGrade r = new RegistroExtracaoGrade(usuario, "grade" + id + ".pdf", 204_800L, status, ms);
        if (status == StatusExtracaoGrade.FALHA) {
            r.registrarErro("Serviço do Gemini indisponível.");
        } else {
            r.preencherResultado(cadastrado, "Professor " + matricula, matricula, (short) 2, 2026, disciplinas, aulas, avisos);
        }
        ReflectionTestUtils.setField(r, "id", id);
        ReflectionTestUtils.setField(r, "createdAt", quando);
        return r;
    }

    @Test
    @DisplayName("Consolida quantidades, taxa de sucesso, professores e distribuição por dia/usuário")
    void consolidaIndicadores() {
        Usuario ana = usuario(1L, "Ana");
        Usuario bruno = usuario(2L, "Bruno");
        Professor katia = new Professor("Katia", "100", "12345678901", "M1",
                RegimeContrato.INDETERMINADO, RegimeJuridico.CLT, Titulacao.MESTRE);
        ReflectionTestUtils.setField(katia, "id", 10L);

        List<RegistroExtracaoGrade> registros = List.of(
                registro(1, ana, StatusExtracaoGrade.SUCESSO, LocalDateTime.of(2026, 11, 16, 9, 0), katia, "M1", 3, 6, 0, 4000),
                registro(2, ana, StatusExtracaoGrade.SUCESSO_COM_AVISOS, LocalDateTime.of(2026, 11, 17, 9, 0), null, "M2", 2, 4, 2, 6000),
                registro(3, bruno, StatusExtracaoGrade.FALHA, LocalDateTime.of(2026, 11, 17, 10, 0), null, null, 0, 0, 0, 2000)
        );
        when(repository.buscarNoIntervalo(LocalDateTime.of(2026, 11, 16, 0, 0), LocalDateTime.of(2026, 11, 23, 0, 0)))
                .thenReturn(registros);

        RelatorioExtracaoGradeDTO r = new RelatorioExtracaoGradeService(repository, clock)
                .gerar("SEMANAL", LocalDate.of(2026, 11, 18));

        var resumo = r.resumo();
        assertEquals(3, resumo.totalExtracoes());
        assertEquals(2, resumo.gradesExtraidas());
        assertEquals(1, resumo.falhas());
        assertEquals(new BigDecimal("66.7"), resumo.taxaSucessoPercentual());
        assertEquals(5, resumo.totalDisciplinas());
        assertEquals(10, resumo.totalAulas());
        assertEquals(2, resumo.totalAvisos());
        assertEquals(new BigDecimal("2.50"), resumo.mediaDisciplinasPorGrade());
        assertEquals(4000L, resumo.tempoMedioMs());
        assertEquals(6000L, resumo.tempoMaximoMs());
        assertEquals(2, resumo.professoresDistintos());
        assertEquals(1, resumo.professoresCadastrados());
        assertEquals(1, resumo.professoresNaoCadastrados());

        assertEquals(7, r.evolucaoDiaria().size());
        var dia17 = r.evolucaoDiaria().get(1);
        assertEquals(2, dia17.total());
        assertEquals(1, dia17.falhas());

        assertEquals("Ana", r.porUsuario().get(0).usuario());
        assertEquals(2, r.porUsuario().get(0).total());
        assertEquals(1, r.porPeriodoLetivo().size());
        assertEquals(2, r.porPeriodoLetivo().get(0).quantidade());
        assertEquals(1, r.principaisFalhas().size());
        assertEquals(3, r.extracoes().size());
        assertTrue(r.periodo().emAndamento());
    }

    @Test
    @DisplayName("Período sem registros retorna zeros sem erros de divisão")
    void periodoVazio() {
        when(repository.buscarNoIntervalo(LocalDateTime.of(2026, 10, 1, 0, 0), LocalDateTime.of(2026, 11, 1, 0, 0)))
                .thenReturn(List.of());

        RelatorioExtracaoGradeDTO r = new RelatorioExtracaoGradeService(repository, clock)
                .gerar("MENSAL", LocalDate.of(2026, 10, 5));

        assertEquals(0, r.resumo().totalExtracoes());
        assertEquals(new BigDecimal("0.0"), r.resumo().taxaSucessoPercentual());
        assertNull(r.resumo().tempoMedioMs());
        assertEquals(31, r.evolucaoDiaria().size());
        assertFalse(r.periodo().emAndamento());
    }
}
