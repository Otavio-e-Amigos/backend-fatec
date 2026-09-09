package br.com.fatec.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Classe principal responsável por iniciar
 * a aplicação Spring Boot.
 */
@SpringBootApplication
public class BackendApplication {

    public static void main(String[] args) {

        // Inicia o servidor Spring Boot.
        SpringApplication.run(BackendApplication.class, args);
    }
}