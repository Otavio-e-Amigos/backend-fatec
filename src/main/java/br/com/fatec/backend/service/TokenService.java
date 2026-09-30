package br.com.fatec.backend.service;

import br.com.fatec.backend.entity.Usuario;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

@Service
public class TokenService {
    @Value("${api.security.token.secret}")
    private String secret;

    private static final String ISSUER = "API-FATEC";

    private Instant gerarDataExpiracao() {
        return Instant.now().plus(2, ChronoUnit.HOURS);
    }

    public String gerarToken(Usuario usuario) {
        try {
            Algorithm algoritmo = Algorithm.HMAC256(secret);
            return JWT.create()
                    .withIssuer(ISSUER)
                    .withSubject(usuario.getLogin()) // O "Subject" é quem é o dono do token
                    .withClaim("id", usuario.getId()) // Guardamos o ID para extrair no Controller depois
                    .withExpiresAt(gerarDataExpiracao())
                    .sign(algoritmo);
        } catch (JWTCreationException exception) {
            throw new RuntimeException("Erro ao gerar token JWT", exception);
        }
    }

    public String validarTokenEObterLogin(String token) {
        try {
            Algorithm algoritmo = Algorithm.HMAC256(secret);
            return JWT.require(algoritmo)
                    .withIssuer(ISSUER)
                    .build()
                    .verify(token)
                    .getSubject();
        } catch (JWTVerificationException exception) {
            return ""; // Retorna vazio se for inválido/expirado, negando o acesso
        }
    }
}
