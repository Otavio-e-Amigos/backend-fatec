package br.com.fatec.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final SecurityFilter securityFilter;
    private final CustomAccessDeniedHandler accessDeniedHandler;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;

    public SecurityConfig(SecurityFilter securityFilter,
                          CustomAccessDeniedHandler accessDeniedHandler,
                          CustomAuthenticationEntryPoint authenticationEntryPoint) {
        this.securityFilter = securityFilter;
        this.accessDeniedHandler = accessDeniedHandler;
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .accessDeniedHandler(accessDeniedHandler) // Retorna 403 customizado
                        .authenticationEntryPoint(authenticationEntryPoint) // Retorna 401 customizado
                )
                .authorizeHttpRequests(auth -> auth
                        // 1. Endpoints Públicos
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/api/autenticacao/**").permitAll()

                        // 2. Consulta de perfil próprio (Acessível por TI e RESPONSAVEL)
                        .requestMatchers("/api/usuarios/me").authenticated()

                        // 3. Gerenciamento de Usuários (EXCLUSIVO do perfil TI)
                        .requestMatchers("/api/usuarios/**").hasRole("TI")

                        // -----------------------------------------------------------------------
                        // [FUTURAS ROTAS OPERACIONAIS - PERMISSAO: TI E RESPONSAVEL]
                        // Exemplo de mapeamento para as próximas Issues do sistema:
                        // .requestMatchers("/api/professores/**").hasAnyRole("TI", "RESPONSAVEL")
                        // .requestMatchers("/api/grades/**").hasAnyRole("TI", "RESPONSAVEL")
                        // .requestMatchers("/api/folhas-frequencia/**").hasAnyRole("TI", "RESPONSAVEL")
                        // -----------------------------------------------------------------------

                        // 4. Qualquer outro endpoint exige autenticação por padrão
                        .anyRequest().authenticated()
                )
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}