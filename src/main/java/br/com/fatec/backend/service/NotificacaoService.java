package br.com.fatec.backend.service;

import br.com.fatec.backend.dto.notificacao.NotificacaoContagemDTO;
import br.com.fatec.backend.dto.notificacao.NotificacaoRespostaDTO;
import br.com.fatec.backend.entity.Notificacao;
import br.com.fatec.backend.entity.Usuario;
import br.com.fatec.backend.enums.CategoriaNotificacao;
import br.com.fatec.backend.enums.Perfil;
import br.com.fatec.backend.enums.TipoNotificacao;
import br.com.fatec.backend.exception.RecursoNaoEncontradoException;
import br.com.fatec.backend.repository.NotificacaoRepository;
import br.com.fatec.backend.repository.UsuarioRepository;
import br.com.fatec.backend.specification.NotificacaoSpecs;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Gerencia as notificações internas: criação (com deduplicação), consulta e controle de leitura.
 * Nenhuma notificação é enviada por canal externo (e-mail, WhatsApp etc.).
 */
@Service
public class NotificacaoService {

    static final int TAMANHO_MAX_TITULO = 150;
    static final int TAMANHO_MAX_MENSAGEM = 1000;

    private final NotificacaoRepository notificacaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final Clock clock;

    public NotificacaoService(NotificacaoRepository notificacaoRepository,
                              UsuarioRepository usuarioRepository,
                              Clock clock) {
        this.notificacaoRepository = notificacaoRepository;
        this.usuarioRepository = usuarioRepository;
        this.clock = clock;
    }

    // -------------------------------------------------------------------------
    // Criação
    // -------------------------------------------------------------------------

    /**
     * Cria uma notificação para um usuário específico (normalmente quem executou a ação).
     * Retorna vazio quando não há usuário identificado (ex: execução sem autenticação).
     */
    @Transactional
    public Optional<Notificacao> notificarUsuario(Long usuarioId, TipoNotificacao tipo, String titulo,
                                                  String mensagem, String referenciaTipo, Long referenciaId) {
        if (usuarioId == null) {
            return Optional.empty();
        }
        Usuario usuario = usuarioRepository.getReferenceById(usuarioId);
        Notificacao notificacao = new Notificacao(usuario, tipo, limitar(titulo, TAMANHO_MAX_TITULO),
                limitar(mensagem, TAMANHO_MAX_MENSAGEM));
        notificacao.definirReferencia(referenciaTipo, referenciaId);
        return Optional.of(notificacaoRepository.save(notificacao));
    }

    /**
     * Cria uma pendência para todos os usuários ATIVOS dos perfis informados.
     * A chave de deduplicação garante no máximo uma notificação por usuário e chave
     * (também protegida pela constraint uk_notificacao_usuario_chave).
     *
     * @return quantidade de notificações efetivamente criadas
     */
    @Transactional
    public int notificarPendenciaParaPerfis(Collection<Perfil> perfis, TipoNotificacao tipo, String titulo,
                                            String mensagem, String chaveDeduplicacao, LocalDateTime expiraEm,
                                            String referenciaTipo, Long referenciaId) {
        List<Usuario> destinatarios = usuarioRepository.findByAtivoTrueAndPerfilIn(perfis);
        int criadas = 0;
        for (Usuario usuario : destinatarios) {
            if (notificacaoRepository.existsByUsuarioIdAndChaveDeduplicacao(usuario.getId(), chaveDeduplicacao)) {
                continue;
            }
            Notificacao notificacao = new Notificacao(usuario, tipo, limitar(titulo, TAMANHO_MAX_TITULO),
                    limitar(mensagem, TAMANHO_MAX_MENSAGEM));
            notificacao.definirPendencia(chaveDeduplicacao, expiraEm);
            notificacao.definirReferencia(referenciaTipo, referenciaId);
            notificacaoRepository.save(notificacao);
            criadas++;
        }
        return criadas;
    }

    /** Encerra as pendências associadas à chave (ex: calendário do ano confirmado). */
    @Transactional
    public int resolverPendencias(String chaveDeduplicacao) {
        return notificacaoRepository.resolverPendenciasPorChave(chaveDeduplicacao, LocalDateTime.now(clock));
    }

    // -------------------------------------------------------------------------
    // Consulta e leitura (sempre restritas ao próprio usuário)
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public Page<NotificacaoRespostaDTO> listar(Long usuarioId, Boolean lida, CategoriaNotificacao categoria,
                                               boolean apenasPendentes, boolean incluirExpiradas, Pageable pageable) {
        Pageable ordenado = pageable.getSort().isSorted()
                ? pageable
                : PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "createdAt", "id"));

        return notificacaoRepository.findAll(
                NotificacaoSpecs.filtrar(usuarioId, lida, categoria, apenasPendentes, incluirExpiradas,
                        LocalDateTime.now(clock)),
                ordenado
        ).map(NotificacaoRespostaDTO::de);
    }

    @Transactional(readOnly = true)
    public NotificacaoContagemDTO contar(Long usuarioId) {
        LocalDateTime agora = LocalDateTime.now(clock);
        return new NotificacaoContagemDTO(
                notificacaoRepository.contarNaoLidasVisiveis(usuarioId, agora),
                notificacaoRepository.contarPendentesVisiveis(usuarioId, agora)
        );
    }

    @Transactional(readOnly = true)
    public NotificacaoRespostaDTO buscar(Long id, Long usuarioId) {
        return NotificacaoRespostaDTO.de(buscarDoUsuario(id, usuarioId));
    }

    @Transactional
    public NotificacaoRespostaDTO marcarComoLida(Long id, Long usuarioId) {
        Notificacao notificacao = buscarDoUsuario(id, usuarioId);
        notificacao.marcarComoLida(LocalDateTime.now(clock));
        return NotificacaoRespostaDTO.de(notificacao);
    }

    @Transactional
    public NotificacaoRespostaDTO marcarComoNaoLida(Long id, Long usuarioId) {
        Notificacao notificacao = buscarDoUsuario(id, usuarioId);
        notificacao.marcarComoNaoLida();
        return NotificacaoRespostaDTO.de(notificacao);
    }

    @Transactional
    public int marcarTodasComoLidas(Long usuarioId) {
        return notificacaoRepository.marcarTodasComoLidas(usuarioId, LocalDateTime.now(clock));
    }

    /**
     * Notificações de outro usuário respondem 404 (e não 403) para não revelar a existência do registro.
     */
    private Notificacao buscarDoUsuario(Long id, Long usuarioId) {
        return notificacaoRepository.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Notificação não encontrada com ID: " + id));
    }

    static String limitar(String texto, int maximo) {
        if (texto == null) {
            return "";
        }
        String limpo = texto.strip();
        return limpo.length() <= maximo ? limpo : limpo.substring(0, maximo - 1) + "…";
    }
}
