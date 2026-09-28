package br.com.fatec.backend.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;


/**
 * Configuração central de segurança do Spring Security.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Permite usar a anotação @PreAuthorize nos Controllers
public class SecurityConfig {
    /**
     * Disponibiliza o PasswordEncoder como um Bean para o Spring injetar no UsuarioService.
     * O BCrypt é o padrão de mercado para hash seguro de senhas.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configura as regras de acesso HTTP (Filtros).
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // LIBERA O SWAGGER PARA QUALQUER UM ACESSAR
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                        // Rota de login (faremos na próxima etapa)
                        .requestMatchers("/api/autenticacao/**").permitAll()

                        .requestMatchers("/api/usuarios/**").hasRole("TI")
                        .anyRequest().authenticated()
                );

        // Nota: Na próxima etapa configuraremos a política Stateless e o filtro JWT aqui.

        return http.build();
    }
}
