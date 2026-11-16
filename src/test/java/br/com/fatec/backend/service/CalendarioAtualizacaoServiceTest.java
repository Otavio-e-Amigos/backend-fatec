package br.com.fatec.backend.service;

import br.com.fatec.backend.entity.CalendarioAtualizacao;
import br.com.fatec.backend.entity.Usuario;
import br.com.fatec.backend.enums.Perfil;
import br.com.fatec.backend.enums.TipoNotificacao;
import br.com.fatec.backend.exception.RegraNegocioException;
import br.com.fatec.backend.repository.CalendarioAtualizacaoRepository;
import br.com.fatec.backend.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CalendarioAtualizacaoServiceTest {

    private static final ZoneId SP = ZoneId.of("America/Sao_Paulo");

    @Mock
    private CalendarioAtualizacaoRepository repository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private NotificacaoService notificacaoService;

    private CalendarioAtualizacaoService servico(String instanteUtc) {
        Clock clock = Clock.fixed(Instant.parse(instanteUtc), SP);
        return new CalendarioAtualizacaoService(repository, usuarioRepository, notificacaoService,
                new CalendarioAcademicoService(), clock, true);
    }

    @Test
    @DisplayName("Fora de fevereiro nenhum lembrete é gerado")
    void foraDeFevereiro() {
        int criadas = servico("2027-03-01T12:00:00Z").verificarLembreteAnual();
        assertEquals(0, criadas);
        verifyNoInteractions(repository, notificacaoService);
    }

    @Test
    @DisplayName("Em fevereiro, sem confirmação do ano, notifica TI e RESPONSAVEL com expiração em 1º de março")
    void fevereiroSemConfirmacao() {
        when(repository.existsById(2027)).thenReturn(false);
        when(notificacaoService.notificarPendenciaParaPerfis(any(), any(), anyString(), anyString(), anyString(), any(), anyString(), any()))
                .thenReturn(2);

        int criadas = servico("2027-02-01T12:00:00Z").verificarLembreteAnual();

        assertEquals(2, criadas);
        verify(notificacaoService).notificarPendenciaParaPerfis(
                eq(CalendarioAtualizacaoService.PERFIS_RESPONSAVEIS),
                eq(TipoNotificacao.CALENDARIO_ATUALIZACAO_PENDENTE),
                contains("2027"), anyString(),
                eq("CALENDARIO_ATUALIZACAO_2027"),
                eq(LocalDateTime.of(2027, 3, 1, 0, 0)),
                eq(CalendarioAtualizacaoService.REFERENCIA_TIPO), eq(2027L));
    }

    @Test
    @DisplayName("Calendário já confirmado (antes ou durante fevereiro): não notifica e encerra pendências")
    void fevereiroJaConfirmado() {
        when(repository.existsById(2027)).thenReturn(true);

        int criadas = servico("2027-02-15T12:00:00Z").verificarLembreteAnual();

        assertEquals(0, criadas);
        verify(notificacaoService).resolverPendencias("CALENDARIO_ATUALIZACAO_2027");
        verify(notificacaoService, never()).notificarPendenciaParaPerfis(any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Lembrete desabilitado por configuração não gera notificações")
    void lembreteDesabilitado() {
        Clock clock = Clock.fixed(Instant.parse("2027-02-10T12:00:00Z"), SP);
        var service = new CalendarioAtualizacaoService(repository, usuarioRepository, notificacaoService,
                new CalendarioAcademicoService(), clock, false);
        assertEquals(0, service.verificarLembreteAnual());
        verifyNoInteractions(repository, notificacaoService);
    }

    @Test
    @DisplayName("Confirmar atualização grava o registro e resolve as pendências do ano")
    void confirmar() {
        Usuario usuario = new Usuario("Coordenador", "coord", "hash", Perfil.RESPONSAVEL);
        ReflectionTestUtils.setField(usuario, "id", 3L);
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(usuario));
        when(repository.findById(2027)).thenReturn(Optional.empty());

        var status = servico("2027-02-10T12:00:00Z").confirmarAtualizacao(2027, "  Publicado pela CPS  ", 3L);

        assertTrue(status.atualizado());
        assertFalse(status.lembreteAtivo());
        assertEquals("Coordenador", status.atualizadoPor());
        assertEquals("Publicado pela CPS", status.observacao());
        verify(repository).save(any(CalendarioAtualizacao.class));
        verify(notificacaoService).resolverPendencias("CALENDARIO_ATUALIZACAO_2027");
    }

    @Test
    @DisplayName("Status indica lembrete ativo em fevereiro enquanto não houver confirmação")
    void statusLembreteAtivo() {
        when(repository.findById(2027)).thenReturn(Optional.empty());
        var status = servico("2027-02-10T12:00:00Z").consultarStatus(null);
        assertEquals(2027, status.ano());
        assertFalse(status.atualizado());
        assertTrue(status.lembreteAtivo());
    }

    @Test
    @DisplayName("Rejeita anos fora do intervalo ou muito à frente")
    void anoInvalido() {
        var service = servico("2027-02-10T12:00:00Z");
        assertThrows(RegraNegocioException.class, () -> service.confirmarAtualizacao(1999, null, 1L));
        assertThrows(RegraNegocioException.class, () -> service.confirmarAtualizacao(2029, null, 1L));
    }
}
