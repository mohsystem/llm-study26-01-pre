package com.um.springbootprojstructure.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

@Component
public class UserPromptLoggingFilter extends OncePerRequestFilter {
    private static final Logger USER_PROMPT_LOG = LoggerFactory.getLogger("USER_PROMPT");
    private static final int MAX_PAYLOAD_BYTES = 64 * 1024;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        var uri = request.getRequestURI();
        return uri == null
                || uri.startsWith("/h2-console")
                || uri.startsWith("/actuator")
                || uri.startsWith("/favicon.ico");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        var wrapped = new ContentCachingRequestWrapper(request, MAX_PAYLOAD_BYTES);
        try {
            filterChain.doFilter(wrapped, response);
        } finally {
            logRequest(wrapped, response);
        }
    }

    private static void logRequest(ContentCachingRequestWrapper request, HttpServletResponse response) {
        try {
            var method = request.getMethod();
            var uri = request.getRequestURI();
            var query = request.getQueryString();
            var status = response.getStatus();
            var remote = request.getRemoteAddr();
            var contentType = request.getContentType();

            var body = extractBody(request);
            body = redactSecrets(body);

            var fullPath = StringUtils.hasText(query) ? (uri + "?" + query) : uri;

            USER_PROMPT_LOG.info(
                    "remote={} method={} path={} status={} contentType={} body={}",
                    remote,
                    method,
                    fullPath,
                    status,
                    contentType,
                    body
            );
        } catch (Exception ignored) {
            // Never break the request flow due to logging.
        }
    }

    private static String extractBody(ContentCachingRequestWrapper request) {
        var bytes = request.getContentAsByteArray();
        if (bytes == null || bytes.length == 0) return "";

        var len = Math.min(bytes.length, MAX_PAYLOAD_BYTES);
        return new String(bytes, 0, len, StandardCharsets.UTF_8);
    }

    private static String redactSecrets(String body) {
        if (!StringUtils.hasText(body)) return body;
        // Basic redaction for common sensitive fields. Keep it simple and format-agnostic.
        return body
                .replaceAll("(?i)(\"password\"\\s*:\\s*\")[^\"]*(\")", "$1***$2")
                .replaceAll("(?i)(\"currentPassword\"\\s*:\\s*\")[^\"]*(\")", "$1***$2")
                .replaceAll("(?i)(\"newPassword\"\\s*:\\s*\")[^\"]*(\")", "$1***$2")
                .replaceAll("(?i)(\"passcode\"\\s*:\\s*\")[^\"]*(\")", "$1***$2")
                .replaceAll("(?i)(\"apiKey\"\\s*:\\s*\")[^\"]*(\")", "$1***$2")
                .replaceAll("(?i)(\"passwordHash\"\\s*:\\s*\")[^\"]*(\")", "$1***$2");
    }
}

