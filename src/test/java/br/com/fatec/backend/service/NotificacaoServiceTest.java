package br.com.fatec.backend.service;

import br.com.fatec.backend.entity.Notificacao;
import br.com.fatec.backend.entity.Usuario;
import br.com.fatec.backend.enums.CategoriaNotificacao;
import br.com.fatec.backend.enums.Perfil;
import br.com.fatec.backend.enums.TipoNotificacao;
import br.com.fatec.backend.exception.RecursoNaoEncontradoException;
import br.com.fatec.backend.repository.NotificacaoRepository;
import br.com.fatec.backend.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificacaoServiceTest {

    @Mock
    private NotificacaoRepository notificacaoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    private NotificacaoService service;
    private final Clock clock = Clock.fixed(Instant.parse("2027-02-10T13:00:00Z"), ZoneId.of("America/Sao_Paulo"));

    @BeforeEach
    void setUp() {
        service = new NotificacaoService(notificacaoRepository, usuarioRepository, clock);
    }

    private Usuario usuario(long id, Perfil perfil) {
        Usuario u = new Usuario("Usuário " + id, "login" + id, "hash", perfil);
        ReflectionTestUtils.setField(u, "id", id);
        return u;
    }

    @Test
    @DisplayName("Sem usuário identificado, nenhuma notificação é criada")
    void semUsuarioNaoNotifica() {
        Optional<Notificacao> resultado = service.notificarUsuario(null, TipoNotificacao.FOLHA_PONTO_GERADA,
                "Título", "Mensagem", null, null);
        assertTrue(resultado.isEmpty());
        verifyNoInteractions(notificacaoRepository);
    }

    @Test
    @DisplayName("Notificação individual recebe categoria a partir do tipo e referência")
    void notificaUsuario() {
        when(usuarioRepository.getReferenceById(1L)).thenReturn(usuario(1L, Perfil.TI));
        when(notificacaoRepository.save(any(Notificacao.class))).thenAnswer(inv -> inv.getArgument(0));

        Notificacao n = service.notificarUsuario(1L, TipoNotificacao.EXTRACAO_GRADE_FALHA,
                "Falha", "Motivo", "REGISTRO_EXTRACAO_GRADE", 7L).orElseThrow();

        assertEquals(CategoriaNotificacao.EXTRACAO_GRADE, n.getCategoria());
        assertEquals(7L, n.getReferenciaId());
        assertFalse(n.isLida());
        assertFalse(n.isPendente());
    }

    @Test
    @DisplayName("Pendência por perfil não duplica para quem já recebeu a mesma chave")
    void pendenciaSemDuplicidade() {
        Usuario jaNotificado = usuario(1L, Perfil.TI);
        Usuario novo = usuario(2L, Perfil.RESPONSAVEL);
        when(usuarioRepository.findByAtivoTrueAndPerfilIn(any())).thenReturn(List.of(jaNotificado, novo));
        when(notificacaoRepository.existsByUsuarioIdAndChaveDeduplicacao(1L, "CHAVE")).thenReturn(true);
        when(notificacaoRepository.existsByUsuarioIdAndChaveDeduplicacao(2L, "CHAVE")).thenReturn(false);

        LocalDateTime expira = LocalDateTime.of(2027, 3, 1, 0, 0);
        int criadas = service.notificarPendenciaParaPerfis(EnumSet.of(Perfil.TI, Perfil.RESPONSAVEL),
                TipoNotificacao.CALENDARIO_ATUALIZACAO_PENDENTE, "Título", "Mensagem", "CHAVE", expira, null, null);

        assertEquals(1, criadas);
        ArgumentCaptor<Notificacao> captor = ArgumentCaptor.forClass(Notificacao.class);
        verify(notificacaoRepository).save(captor.capture());
        Notificacao salva = captor.getValue();
        assertSame(novo, salva.getUsuario());
        assertTrue(salva.isPendente());
        assertEquals("CHAVE", salva.getChaveDeduplicacao());
        assertEquals(expira, salva.getExpiraEm());
    }

    @Test
    @DisplayName("Marcar como lida registra o momento; notificação de outro usuário responde 404")
    void marcarComoLida() {
        Notificacao n = new Notificacao(usuario(1L, Perfil.TI), TipoNotificacao.RELATORIO_GERADO, "T", "M");
        when(notificacaoRepository.findByIdAndUsuarioId(5L, 1L)).thenReturn(Optional.of(n));
        when(notificacaoRepository.findByIdAndUsuarioId(5L, 2L)).thenReturn(Optional.empty());

        var dto = service.marcarComoLida(5L, 1L);
        assertTrue(dto.lida());
        assertEquals(LocalDateTime.of(2027, 2, 10, 10, 0), dto.lidaEm());

        assertThrows(RecursoNaoEncontradoException.class, () -> service.marcarComoLida(5L, 2L));
    }

    @Test
    @DisplayName("Textos longos são limitados ao tamanho das colunas")
    void limitarTexto() {
        String longo = "x".repeat(2000);
        assertEquals(NotificacaoService.TAMANHO_MAX_MENSAGEM,
                NotificacaoService.limitar(longo, NotificacaoService.TAMANHO_MAX_MENSAGEM).length());
        assertEquals("", NotificacaoService.limitar(null, 10));
    }
}
