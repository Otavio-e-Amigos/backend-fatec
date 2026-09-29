package br.com.fatec.backend.service;

import org.springframework.beans.factory.annotation.Value;

public class TokenService {
    @Value("${api.security.token.secret:fatec-segredo-desenvolvimento-123}")
}
