package br.com.erp.api.web.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.net.URI;
import java.time.LocalDateTime;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ObjectMapper            objectMapper;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          ObjectMapper objectMapper) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.objectMapper            = objectMapper;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/store/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/store/orders").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/actuator/health").permitAll()

                        // Cancelamento de pedido — service ou admin
                        .requestMatchers(HttpMethod.POST, "/erp/orders/*/cancel")
                        .hasAnyRole("ADMIN", "SERVICE")

                        // Leitura de pedido e timeline — service ou admin (polling do ecommerce)
                        .requestMatchers(HttpMethod.GET, "/erp/orders/*")
                        .hasAnyRole("ADMIN", "SERVICE")
                        .requestMatchers(HttpMethod.GET, "/erp/orders/*/timeline")
                        .hasAnyRole("ADMIN", "SERVICE")

                        // Reserva — service ou admin
                        .requestMatchers(HttpMethod.POST, "/erp/skus/*/stock/reserve")
                        .hasAnyRole("ADMIN", "SERVICE")
                        // Confirmar reserva — service ou admin
                        .requestMatchers(HttpMethod.POST, "/erp/skus/reservations/*/confirm")
                        .hasAnyRole("ADMIN", "SERVICE")
                        // Liberar reserva — service ou admin
                        .requestMatchers(HttpMethod.POST, "/erp/skus/reservations/*/release")
                        .hasAnyRole("ADMIN", "SERVICE")
                        // Consultar estoque — service ou admin
                        .requestMatchers(HttpMethod.GET, "/erp/skus/*/stock")
                        .hasAnyRole("ADMIN", "SERVICE")
                        // Ajustar estoque — somente admin
                        .requestMatchers(HttpMethod.POST, "/erp/skus/*/stock/adjust")
                        .hasRole("ADMIN")

                        // Demais endpoints do ERP — somente admin
                        .requestMatchers("/erp/**").hasRole("ADMIN")

                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, e) ->
                                writeProblemDetail(response, HttpStatus.UNAUTHORIZED, "Não autenticado"))
                        .accessDeniedHandler((request, response, e) ->
                                writeProblemDetail(response, HttpStatus.FORBIDDEN, "Acesso negado"))
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private void writeProblemDetail(HttpServletResponse response,
                                    HttpStatus status, String title) throws IOException {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, title);
        problem.setTitle(title);
        problem.setType(URI.create("https://example.com/probs/" + status.name().toLowerCase()));
        problem.setProperty("timestamp", LocalDateTime.now());

        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(problem));
    }
}