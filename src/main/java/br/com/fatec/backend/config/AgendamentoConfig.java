package br.com.fatec.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.Clock;
import java.time.ZoneId;

/**
 * Habilita tarefas agendadas (lembrete anual do calendário) e disponibiliza um relógio
 * único da aplicação, no fuso de São Paulo, que pode ser substituído nos testes.
 */
@Configuration
@EnableScheduling
public class AgendamentoConfig {

    public static final ZoneId ZONA_PADRAO = ZoneId.of("America/Sao_Paulo");

    @Bean
    public Clock relogioAplicacao() {
        return Clock.system(ZONA_PADRAO);
    }
}
