package br.com.fatec.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Dispara a verificação anual do calendário acadêmico:
 * diariamente às 07:00 durante fevereiro e também ao iniciar a aplicação
 * (cobre reinícios do servidor no meio do mês). A deduplicação fica no serviço.
 */
@Component
public class CalendarioLembreteAgendador {

    private static final Logger log = LoggerFactory.getLogger(CalendarioLembreteAgendador.class);

    private final CalendarioAtualizacaoService calendarioAtualizacaoService;

    public CalendarioLembreteAgendador(CalendarioAtualizacaoService calendarioAtualizacaoService) {
        this.calendarioAtualizacaoService = calendarioAtualizacaoService;
    }

    @Scheduled(cron = "${appointer.calendario.lembrete.cron:0 0 7 * 2 *}", zone = "America/Sao_Paulo")
    public void verificarDiariamenteEmFevereiro() {
        executar();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void verificarNaInicializacao() {
        executar();
    }

    private void executar() {
        try {
            int criadas = calendarioAtualizacaoService.verificarLembreteAnual();
            if (criadas > 0) {
                log.info("Lembrete de atualização do calendário acadêmico enviado para {} usuário(s).", criadas);
            }
        } catch (RuntimeException e) {
            log.warn("Falha ao verificar o lembrete anual do calendário: {}", e.getClass().getSimpleName());
        }
    }
}
