package com.um.springbootprojstructure.auth.web;

import com.um.springbootprojstructure.auth.repository.SessionTokenRepository;
import com.um.springbootprojstructure.common.exception.UnauthorizedException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class SessionAuthenticationFilter extends OncePerRequestFilter {
    private final SessionTokenRepository sessionTokenRepository;

    public SessionAuthenticationFilter(SessionTokenRepository sessionTokenRepository) {
        this.sessionTokenRepository = sessionTokenRepository;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        var uri = request.getRequestURI();
        return uri == null
                || uri.startsWith("/h2-console")
                || uri.startsWith("/api/auth/login")
                || uri.startsWith("/api/auth/register")
                || uri.startsWith("/api/auth/mfa/challenge")
                || uri.startsWith("/api/auth/mfa/verify")
                || uri.startsWith("/api/auth/reset-request")
                || uri.startsWith("/api/auth/reset-confirm");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        var header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            var token = header.substring("Bearer ".length()).trim();
            if (token.isEmpty()) {
                throw new UnauthorizedException("missing bearer token");
            }

            var session = sessionTokenRepository
                    .findValidByToken(token, Instant.now())
                    .orElseThrow(() -> new UnauthorizedException("invalid or expired token"));

            var user = session.getUser();
            request.setAttribute(
                    AuthRequestAttributes.AUTHENTICATED_USER,
                    new AuthenticatedUser(user.getId(), user.getUsername(), user.getRole())
            );
        }

        filterChain.doFilter(request, response);
    }
}

