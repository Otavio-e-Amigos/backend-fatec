package br.com.fatec.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List;

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
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuracao = new CorsConfiguration();
        configuracao.setAllowedOriginPatterns(List.of("*"));
        configuracao.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuracao.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuracao.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource fonte = new UrlBasedCorsConfigurationSource();
        fonte.registerCorsConfiguration("/**", configuracao);
        return fonte;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .accessDeniedHandler(accessDeniedHandler) // Retorna 403 customizado
                        .authenticationEntryPoint(authenticationEntryPoint) // Retorna 401 customizado
                )
                .authorizeHttpRequests(auth -> auth
                        // 1. Swagger e Health Check
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers(HttpMethod.GET, "/").permitAll()
                        // 2. Regra ESPECÍFICA de Autenticação (exige Token JWT)
                        .requestMatchers("/api/autenticacao/me").authenticated()
                        // 3. Regra GENÉRICA de Autenticação (Login e outras rotas públicas do módulo)
                        .requestMatchers("/api/autenticacao/**").permitAll()
                        // 4. Gerenciamento de Usuários (EXCLUSIVO do perfil TI)
                        .requestMatchers("/api/usuarios/**").hasRole("TI")
                        // 5. Gerenciamento de Professor (AMBOS para os perfilis TI)
                        .requestMatchers("/api/professores/**").hasAnyRole("TI", "RESPONSAVEL")
                        // 6. Gerenciamento de Cursos (AMBOS para os perfilis TI)
                        .requestMatchers("/api/cursos/**").hasAnyRole("TI", "RESPONSAVEL")
                        // 7. Gerenciamento de Períodos Letivos (AMBOS para os perfilis TI)
                        .requestMatchers("/api/periodos-letivos/**").hasAnyRole("TI", "RESPONSAVEL")

                        // -----------------------------------------------------------------------
                        // [FUTURAS ROTAS OPERACIONAIS - PERMISSAO: TI E RESPONSAVEL]
                        // Exemplo de mapeamento para as próximas Issues do sistema:
                        //
                        // .requestMatchers("/api/grades/**").hasAnyRole("TI", "RESPONSAVEL")
                        // .requestMatchers("/api/folhas-frequencia/**").hasAnyRole("TI", "RESPONSAVEL")
                        // -----------------------------------------------------------------------

                        // 5. Qualquer outro endpoint exige autenticação por padrão
                        .anyRequest().authenticated()
                )
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}