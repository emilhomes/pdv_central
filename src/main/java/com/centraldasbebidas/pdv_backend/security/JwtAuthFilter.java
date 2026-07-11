package com.centraldasbebidas.pdv_backend.security;

import com.centraldasbebidas.pdv_backend.model.Operador;
import com.centraldasbebidas.pdv_backend.repository.OperadorRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Lê o header "Authorization: Bearer <token>" em cada requisição, valida
 * o JWT e, se válido, coloca o Operador autenticado no SecurityContext
 * (assim os controllers/services conseguem saber "quem está logado" via
 * SecurityContextHolder.getContext().getAuthentication().getPrincipal()).
 *
 * Se o token estiver ausente/inválido/expirado, simplesmente não autentica
 * — quem decide se isso é um problema é o SecurityConfig (rotas
 * protegidas vão retornar 401/403 automaticamente).
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private OperadorRepository operadorRepository;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                     @NonNull HttpServletResponse response,
                                     @NonNull FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                Claims claims = jwtService.validarEExtrairClaims(token);
                String login = claims.getSubject();

                if (SecurityContextHolder.getContext().getAuthentication() == null) {
                    Optional<Operador> operadorOpt = operadorRepository.findByLogin(login);
                    if (operadorOpt.isPresent() && operadorOpt.get().isAtivo()) {
                        Operador operador = operadorOpt.get();
                        var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + operador.getPapel()));
                        var authToken = new UsernamePasswordAuthenticationToken(operador, null, authorities);
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
                }
            } catch (Exception e) {
                // Token inválido/expirado: não autentica. Se a rota exigir
                // login, o SecurityConfig vai barrar com 401 mais adiante.
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}
