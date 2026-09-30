package br.com.fatec.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class StatusController {

    /** Rota pública para confirmar que o backend está no ar. */
    @GetMapping("/")
    public Map<String, String> verificarStatus() {
        return Map.of(
                "status", "online",
                "mensagem", "Backend FATEC em execução."
        );
    }
}