package br.com.fatec.backend.controller;

// Importa a anotação que transforma esta classe
// em um Controller responsável por responder requisições HTTP.
import org.springframework.web.bind.annotation.GetMapping;

// Importa a anotação que define o caminho principal
// dos endpoints desta classe.
import org.springframework.web.bind.annotation.RequestMapping;

// Importa a anotação que identifica esta classe
// como um Controller REST.
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller utilizado para realizar testes iniciais da API.
 *
 * Neste momento ele possui apenas um endpoint simples
 * para verificar se o backend está funcionando.
 */
@RestController

// Define o caminho principal deste Controller.
// Todos os endpoints desta classe começarão com /api/teste.
@RequestMapping("/api/teste")
public class TestController {

    /**
     * Endpoint utilizado para verificar se a API está funcionando.
     *
     * Quando fizermos uma requisição GET para:
     *
     * http://localhost:8080/api/teste
     *
     * o método abaixo será executado.
     */
    @GetMapping
    public String testarApi() {

        // Retorna uma mensagem simples para confirmar
        // que o backend recebeu e processou a requisição.
        return "API do backend funcionando!";
    }
}