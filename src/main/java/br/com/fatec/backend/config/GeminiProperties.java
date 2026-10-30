package br.com.fatec.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/** Lê as propriedades gemini.* do application.properties. */
@ConfigurationProperties(prefix = "gemini")
public record GeminiProperties(
        @DefaultValue("") String apiKey,
        @DefaultValue("gemini-3.5-flash") String model,
        @DefaultValue("60") int timeoutSegundos
) { }