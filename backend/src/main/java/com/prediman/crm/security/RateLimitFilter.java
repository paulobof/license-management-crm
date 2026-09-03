package com.prediman.crm.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_REQUESTS = 10;
    private static final long WINDOW_MS = 60_000; // 1 minute

    /**
     * O forgot-password dispara envio de e-mail e é o alvo natural de enumeração de contas
     * e de spam; por isso recebe um limite bem mais restrito que o restante de /auth.
     */
    static final String FORGOT_PASSWORD_PATH = "/api/v1/auth/forgot-password";
    private static final int FORGOT_PASSWORD_MAX_REQUESTS = 3;
    private static final long FORGOT_PASSWORD_WINDOW_MS = 900_000; // 15 minutes

    private static final String MSG_PADRAO = "Muitas tentativas. Aguarde 1 minuto.";
    private static final String MSG_FORGOT_PASSWORD =
            "Muitas solicitações de recuperação de senha. Aguarde 15 minutos antes de tentar novamente.";

    private final Map<String, RateLimitEntry> clients = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();

        if (!path.startsWith("/api/v1/auth/")) {
            filterChain.doFilter(request, response);
            return;
        }

        boolean forgotPassword = FORGOT_PASSWORD_PATH.equals(path);
        int maxRequests = forgotPassword ? FORGOT_PASSWORD_MAX_REQUESTS : MAX_REQUESTS;
        long windowMs = forgotPassword ? FORGOT_PASSWORD_WINDOW_MS : WINDOW_MS;

        String clientIp = getClientIp(request);
        String bucketKey = (forgotPassword ? "forgot-password|" : "auth|") + clientIp;

        RateLimitEntry entry = clients.compute(bucketKey, (key, existing) -> {
            long now = System.currentTimeMillis();
            if (existing == null || now - existing.windowStart > windowMs) {
                return new RateLimitEntry(now, new AtomicInteger(1));
            }
            existing.count.incrementAndGet();
            return existing;
        });

        if (entry.count.get() > maxRequests) {
            String mensagem = forgotPassword ? MSG_FORGOT_PASSWORD : MSG_PADRAO;
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"message\":\"" + mensagem + "\",\"status\":429}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private static class RateLimitEntry {
        final long windowStart;
        final AtomicInteger count;

        RateLimitEntry(long windowStart, AtomicInteger count) {
            this.windowStart = windowStart;
            this.count = count;
        }
    }
}
