package br.com.fatec.backend.service;

import br.com.fatec.backend.dto.calendario.StatusAtualizacaoCalendarioDTO;
import br.com.fatec.backend.entity.CalendarioAtualizacao;
import br.com.fatec.backend.entity.Usuario;
import br.com.fatec.backend.enums.Perfil;
import br.com.fatec.backend.enums.TipoNotificacao;
import br.com.fatec.backend.exception.RegraNegocioException;
import br.com.fatec.backend.repository.CalendarioAtualizacaoRepository;
import br.com.fatec.backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

/**
 * Controla a confirmação anual de atualização do calendário acadêmico e o lembrete de fevereiro.
 * <p>
 * Regras:
 * <ul>
 *   <li>O lembrete só é gerado em fevereiro e expira em 1º de março (deixa de ser exibido).</li>
 *   <li>Se o calendário do ano já foi confirmado (antes ou durante fevereiro), nenhum lembrete é criado.</li>
 *   <li>A confirmação encerra as pendências já criadas para o ano.</li>
 *   <li>Deduplicação por usuário + chave CALENDARIO_ATUALIZACAO_{ano}.</li>
 * </ul>
 */
@Service
public class CalendarioAtualizacaoService {

    public static final String REFERENCIA_TIPO = "CALENDARIO_ATUALIZACAO";
    static final String PREFIXO_CHAVE = "CALENDARIO_ATUALIZACAO_";
    static final Set<Perfil> PERFIS_RESPONSAVEIS = EnumSet.of(Perfil.TI, Perfil.RESPONSAVEL);

    private final CalendarioAtualizacaoRepository repository;
    private final UsuarioRepository usuarioRepository;
    private final NotificacaoService notificacaoService;
    private final CalendarioAcademicoService calendarioAcademicoService;
    private final Clock clock;
    private final boolean lembreteHabilitado;

    public CalendarioAtualizacaoService(CalendarioAtualizacaoRepository repository,
                                        UsuarioRepository usuarioRepository,
                                        NotificacaoService notificacaoService,
                                        CalendarioAcademicoService calendarioAcademicoService,
                                        Clock clock,
                                        @Value("${appointer.calendario.lembrete.habilitado:true}") boolean lembreteHabilitado) {
        this.repository = repository;
        this.usuarioRepository = usuarioRepository;
        this.notificacaoService = notificacaoService;
        this.calendarioAcademicoService = calendarioAcademicoService;
        this.clock = clock;
        this.lembreteHabilitado = lembreteHabilitado;
    }

    @Transactional(readOnly = true)
    public StatusAtualizacaoCalendarioDTO consultarStatus(Integer ano) {
        int anoConsultado = ano != null ? ano : LocalDate.now(clock).getYear();
        validarAno(anoConsultado);
        return montarStatus(anoConsultado, repository.findById(anoConsultado));
    }

    @Transactional
    public StatusAtualizacaoCalendarioDTO confirmarAtualizacao(int ano, String observacao, Long usuarioId) {
        validarAno(ano);
        LocalDateTime agora = LocalDateTime.now(clock);
        Usuario usuario = usuarioId != null ? usuarioRepository.findById(usuarioId).orElse(null) : null;
        String observacaoLimpa = observacao != null && !observacao.isBlank() ? observacao.strip() : null;

        CalendarioAtualizacao registro = repository.findById(ano)
                .map(existente -> {
                    existente.reconfirmar(usuario, observacaoLimpa, agora);
                    return existente;
                })
                .orElseGet(() -> new CalendarioAtualizacao(ano, usuario, observacaoLimpa, agora));
        repository.save(registro);

        notificacaoService.resolverPendencias(chave(ano));
        return montarStatus(ano, Optional.of(registro));
    }

    /**
     * Executada diariamente em fevereiro e na inicialização da aplicação.
     *
     * @return quantidade de notificações criadas
     */
    @Transactional
    public int verificarLembreteAnual() {
        if (!lembreteHabilitado) {
            return 0;
        }
        LocalDate hoje = LocalDate.now(clock);
        if (hoje.getMonth() != Month.FEBRUARY) {
            return 0;
        }
        int ano = hoje.getYear();
        if (repository.existsById(ano)) {
            // Calendário já atualizado: garante que nenhuma pendência antiga continue aberta.
            notificacaoService.resolverPendencias(chave(ano));
            return 0;
        }

        LocalDateTime expiraEm = LocalDate.of(ano, Month.MARCH, 1).atStartOfDay();
        String mensagem = String.format(
                "O calendário acadêmico de %d ainda não foi confirmado como atualizado. Revise feriados, emendas, "
                        + "recessos e demais dias sem aula do ano e confirme a atualização para encerrar esta pendência. "
                        + "Esses dados são usados na geração das folhas de ponto.", ano);

        return notificacaoService.notificarPendenciaParaPerfis(PERFIS_RESPONSAVEIS,
                TipoNotificacao.CALENDARIO_ATUALIZACAO_PENDENTE,
                "Atualize o calendário acadêmico de " + ano,
                mensagem, chave(ano), expiraEm, REFERENCIA_TIPO, (long) ano);
    }

    static String chave(int ano) {
        return PREFIXO_CHAVE + ano;
    }

    private StatusAtualizacaoCalendarioDTO montarStatus(int ano, Optional<CalendarioAtualizacao> registro) {
        LocalDate hoje = LocalDate.now(clock);
        boolean atualizado = registro.isPresent();
        boolean lembreteAtivo = lembreteHabilitado && !atualizado
                && hoje.getYear() == ano && hoje.getMonth() == Month.FEBRUARY;
        int eventosNoAno = (int) calendarioAcademicoService.listarTodos().stream()
                .filter(e -> e.data() != null && e.data().getYear() == ano)
                .count();

        return new StatusAtualizacaoCalendarioDTO(
                ano,
                atualizado,
                registro.map(CalendarioAtualizacao::getAtualizadoEm).orElse(null),
                registro.map(CalendarioAtualizacao::getUsuario).map(Usuario::getNome).orElse(null),
                registro.map(CalendarioAtualizacao::getObservacao).orElse(null),
                eventosNoAno,
                lembreteAtivo
        );
    }

    private void validarAno(int ano) {
        int anoAtual = LocalDate.now(clock).getYear();
        if (ano < 2000 || ano > 2050) {
            throw new RegraNegocioException("O ano informado deve estar entre 2000 e 2050.");
        }
        if (ano > anoAtual + 1) {
            throw new RegraNegocioException("Só é possível consultar ou confirmar o calendário até o próximo ano.");
        }
    }
}
