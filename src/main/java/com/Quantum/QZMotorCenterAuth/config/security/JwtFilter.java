package com.Quantum.QZMotorCenterAuth.config.security;

import com.Quantum.QZMotorCenterAuth.persistnecia.repositorio.TokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private TokenRepository tokenRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        // 🔹 Si no hay token, sigue normal
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        String email = null;

        try {
            email = jwtService.extractUsername(token);
        } catch (Exception e) {

            // 🔥 AQUÍ está la mejora (opción 2)
            tokenRepository.findByToken(token).ifPresent(t -> {
                t.setExpired(true);
                tokenRepository.save(t);
            });

            filterChain.doFilter(request, response);
            return;
        }

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            var tokenDb = tokenRepository.findByToken(token);

            boolean isValidToken = tokenDb.isPresent()
                    && !tokenDb.get().isExpired()
                    && !tokenDb.get().isRevoked()
                    && tokenDb.get().getTokenType() == com.Quantum.QZMotorCenterAuth.persistnecia.entidad.TokenType.BEARER;

            if (isValidToken && jwtService.isTokenValid(token)) {

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                email,
                                null,
                                Collections.emptyList()
                        );

                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                SecurityContextHolder.getContext().setAuthentication(authToken);

            } else {
                // 🔥 También lo marcas como expirado si no es válido
                tokenDb.ifPresent(t -> {
                    t.setExpired(true);
                    tokenRepository.save(t);
                });
            }
        }

        filterChain.doFilter(request, response);
    }
}