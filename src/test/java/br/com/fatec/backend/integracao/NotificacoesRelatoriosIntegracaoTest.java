package br.com.fatec.backend.integracao;

import br.com.fatec.backend.entity.*;
import br.com.fatec.backend.enums.*;
import br.com.fatec.backend.repository.*;
import br.com.fatec.backend.service.NotificacaoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Executa as consultas novas (JPQL, Specifications e updates em lote) contra o H2 em modo PostgreSQL.
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:integracao_notificacoes;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "api.security.token.secret=test-secret-key-123456789012345678901234567890",
        "appointer.calendario.lembrete.habilitado=false"
})
@Transactional
class NotificacoesRelatoriosIntegracaoTest {

    @Autowired private NotificacaoService notificacaoService;
    @Autowired private NotificacaoRepository notificacaoRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private ProfessorRepository professorRepository;
    @Autowired private PeriodoLetivoRepository periodoLetivoRepository;
    @Autowired private GradeRepository gradeRepository;
    @Autowired private RegistroFolhaPontoRepository registroFolhaPontoRepository;
    @Autowired private RegistroExtracaoGradeRepository registroExtracaoGradeRepository;
    @Autowired private DisciplinaDaGradeRepository disciplinaDaGradeRepository;

    private Usuario novoUsuario(String login, Perfil perfil) {
        return usuarioRepository.save(new Usuario("Usuário " + login, login, "hash", perfil));
    }

    @Test
    @DisplayName("Notificações: deduplicação por chave, listagem filtrada, contagem, leitura em lote e resolução")
    void fluxoNotificacoes() {
        Usuario ti = novoUsuario("int.ti", Perfil.TI);
        Usuario resp = novoUsuario("int.resp", Perfil.RESPONSAVEL);
        Usuario inativo = novoUsuario("int.inativo", Perfil.TI);
        inativo.desativar();

        var perfis = EnumSet.of(Perfil.TI, Perfil.RESPONSAVEL);
        LocalDateTime expira = LocalDateTime.now().plusDays(10);
        int primeira = notificacaoService.notificarPendenciaParaPerfis(perfis, TipoNotificacao.CALENDARIO_ATUALIZACAO_PENDENTE,
                "Atualize", "Mensagem", "CHAVE_INT", expira, "CALENDARIO_ATUALIZACAO", 2030L);
        int segunda = notificacaoService.notificarPendenciaParaPerfis(perfis, TipoNotificacao.CALENDARIO_ATUALIZACAO_PENDENTE,
                "Atualize", "Mensagem", "CHAVE_INT", expira, "CALENDARIO_ATUALIZACAO", 2030L);
        assertTrue(primeira >= 2, "usuários ativos de TI e RESPONSAVEL devem receber");
        assertEquals(0, segunda, "a mesma chave não pode gerar duplicatas");
        assertFalse(notificacaoRepository.existsByUsuarioIdAndChaveDeduplicacao(inativo.getId(), "CHAVE_INT"));

        notificacaoService.notificarUsuario(ti.getId(), TipoNotificacao.RELATORIO_GERADO, "Relatório", "Gerado", "RELATORIO", null);
        Notificacao expirada = new Notificacao(ti, TipoNotificacao.CALENDARIO_ATUALIZACAO_PENDENTE, "Antiga", "Expirada");
        expirada.definirPendencia("CHAVE_ANTIGA", LocalDateTime.now().minusDays(1));
        notificacaoRepository.save(expirada);

        var pagina = notificacaoService.listar(ti.getId(), null, null, false, false, PageRequest.of(0, 20));
        assertEquals(2, pagina.getTotalElements(), "expiradas ficam fora por padrão");
        assertEquals(3, notificacaoService.listar(ti.getId(), null, null, false, true, PageRequest.of(0, 20)).getTotalElements());
        assertEquals(1, notificacaoService.listar(ti.getId(), null, CategoriaNotificacao.RELATORIO, false, false, PageRequest.of(0, 20)).getTotalElements());
        assertEquals(1, notificacaoService.listar(ti.getId(), null, null, true, false, PageRequest.of(0, 20)).getTotalElements());

        var contagem = notificacaoService.contar(ti.getId());
        assertEquals(2, contagem.naoLidas());
        assertEquals(1, contagem.pendentes());

        assertEquals(3, notificacaoService.marcarTodasComoLidas(ti.getId()));
        assertEquals(0, notificacaoService.contar(ti.getId()).naoLidas());

        assertTrue(notificacaoService.resolverPendencias("CHAVE_INT") >= 2);
        assertEquals(0, notificacaoService.contar(resp.getId()).pendentes());
    }

    @Test
    @DisplayName("Consultas dos relatórios: intervalos, agregações por professor/grade e vínculos com detalhes")
    void consultasRelatorios() {
        Usuario usuario = novoUsuario("int.rel", Perfil.RESPONSAVEL);
        Professor professor = professorRepository.save(new Professor("Prof Integração", "1", "11122233344", "MINT1",
                RegimeContrato.INDETERMINADO, RegimeJuridico.CLT, Titulacao.DOUTOR));
        PeriodoLetivo periodo = periodoLetivoRepository.save(new PeriodoLetivo(2, 2031, LocalDate.of(2031, 8, 1), LocalDate.of(2031, 12, 15)));
        Grade grade = gradeRepository.save(new Grade(professor, periodo, BigDecimal.TEN, null, null));

        registroFolhaPontoRepository.save(new RegistroFolhaPonto(professor, grade, usuario, 9, 2031, 2031, 2, false, 1, 2));
        registroFolhaPontoRepository.save(new RegistroFolhaPonto(professor, grade, usuario, 10, 2031, 2031, 2, false, 0, 2));
        RegistroExtracaoGrade extracao = new RegistroExtracaoGrade(usuario, "a.pdf", 100L, StatusExtracaoGrade.SUCESSO, 1500L);
        extracao.preencherResultado(professor, "Prof Integração", "MINT1", (short) 2, 2031, 2, 4, 0);
        registroExtracaoGradeRepository.save(extracao);
        registroFolhaPontoRepository.flush();

        LocalDateTime inicio = LocalDate.now().atStartOfDay();
        LocalDateTime fim = inicio.plusDays(1);

        assertEquals(2, registroFolhaPontoRepository.buscarNoIntervalo(inicio, fim).stream()
                .filter(r -> r.getProfessor().getId().equals(professor.getId())).count());
        assertEquals(2, registroFolhaPontoRepository.buscarNoIntervaloPorProfessor(professor.getId(), inicio, fim).size());
        assertTrue(registroExtracaoGradeRepository.buscarNoIntervalo(inicio, fim).stream()
                .anyMatch(r -> "a.pdf".equals(r.getNomeArquivo())));

        List<Object[]> totais = registroFolhaPontoRepository.totaisHistoricosPorProfessor(List.of(professor.getId()));
        assertEquals(1, totais.size());
        assertEquals(2L, ((Number) totais.get(0)[1]).longValue());

        Object[] resumo = registroFolhaPontoRepository.resumoHistoricoDoProfessor(professor.getId()).get(0);
        assertEquals(2L, ((Number) resumo[0]).longValue());
        assertNotNull(resumo[2]);

        List<Object[]> porGrade = registroFolhaPontoRepository.totaisPorGradeDoProfessor(professor.getId());
        assertEquals(grade.getId(), ((Number) porGrade.get(0)[0]).longValue());

        assertEquals(1, gradeRepository.findByProfessorIdComPeriodo(professor.getId()).size());
        assertTrue(disciplinaDaGradeRepository.findByProfessorIdComDetalhes(professor.getId()).isEmpty());
        assertTrue(professorRepository.countByStatus(StatusProfessor.ATIVO) >= 1);
    }
}
