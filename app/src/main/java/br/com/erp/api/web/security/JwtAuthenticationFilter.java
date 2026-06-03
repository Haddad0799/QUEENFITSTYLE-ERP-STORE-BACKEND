package br.com.erp.api.web.security;

import br.com.erp.api.auth.infrastructure.security.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);

        if (jwtService.isValid(token)) {
            Claims claims  = jwtService.extractClaims(token);
            String subject = claims.getSubject();

            List<GrantedAuthority> authorities = extractAuthorities(claims);

            var auth = new UsernamePasswordAuthenticationToken(
                    subject,
                    null,
                    authorities
            );
            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        chain.doFilter(request, response);
    }

    /**
     * Constrói as authorities a partir das claims do JWT, suportando dois formatos:
     * - tokens de usuário: claim "role" (String) → prefixada com "ROLE_"
     * - tokens de serviço: claim "roles" (List) → valores já prefixados (ex.: "ROLE_SERVICE")
     */
    private List<GrantedAuthority> extractAuthorities(Claims claims) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        Object roles = claims.get("roles");
        if (roles instanceof List<?> roleList) {
            for (Object role : roleList) {
                authorities.add(new SimpleGrantedAuthority(String.valueOf(role)));
            }
        }

        String role = claims.get("role", String.class);
        if (role != null && !role.isBlank()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
        }

        return authorities;
    }
}
