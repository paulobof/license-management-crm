package com.prediman.crm.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RateLimitFilterTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    // RateLimitFilter has no external dependencies — create a fresh instance per test
    // so the per-IP counter is always reset.
    private RateLimitFilter rateLimitFilter;

    private static final String AUTH_PATH = "/api/v1/auth/login";
    private static final String CLIENT_IP = "192.168.1.1";

    @BeforeEach
    void setUp() {
        rateLimitFilter = new RateLimitFilter();
    }

    // -------------------------------------------------------------------------
    // Requests on non-auth paths must bypass the rate limiter entirely
    // -------------------------------------------------------------------------

    @Test
    void nonAuthPath_alwaysPassesThrough() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/v1/clientes");

        rateLimitFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
    }

    // -------------------------------------------------------------------------
    // Requests within limit must pass through
    // -------------------------------------------------------------------------

    @Test
    void allowsRequestsWithinLimit() throws Exception {
        when(request.getRequestURI()).thenReturn(AUTH_PATH);
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn(CLIENT_IP);

        // MAX_REQUESTS is 10; send exactly 10 — all must be allowed
        for (int i = 0; i < 10; i++) {
            rateLimitFilter.doFilterInternal(request, response, filterChain);
        }

        verify(filterChain, org.mockito.Mockito.times(10)).doFilter(request, response);
        verify(response, never()).setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
    }

    // -------------------------------------------------------------------------
    // The 11th request (exceeds limit) must be blocked with 429
    // -------------------------------------------------------------------------

    @Test
    void blocksRequestsExceedingLimit_returns429() throws Exception {
        StringWriter body = new StringWriter();
        PrintWriter writer = new PrintWriter(body);

        when(request.getRequestURI()).thenReturn(AUTH_PATH);
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn(CLIENT_IP);
        // getWriter() is only called when the request is blocked
        when(response.getWriter()).thenReturn(writer);

        // Send 11 requests; the 11th must be blocked
        for (int i = 0; i < 11; i++) {
            rateLimitFilter.doFilterInternal(request, response, filterChain);
        }

        // Filter chain must only have been called for the first 10 requests
        verify(filterChain, org.mockito.Mockito.times(10)).doFilter(request, response);
        // 429 status must be set at least once (for the blocked request(s))
        verify(response, atLeastOnce()).setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
    }

    // -------------------------------------------------------------------------
    // Different IPs must have independent counters
    // -------------------------------------------------------------------------

    @Test
    void differentIps_haveIndependentCounters() throws Exception {
        String ip1 = "10.0.0.1";
        String ip2 = "10.0.0.2";

        HttpServletRequest request1 = org.mockito.Mockito.mock(HttpServletRequest.class);
        HttpServletRequest request2 = org.mockito.Mockito.mock(HttpServletRequest.class);

        when(request1.getRequestURI()).thenReturn(AUTH_PATH);
        when(request1.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request1.getRemoteAddr()).thenReturn(ip1);

        when(request2.getRequestURI()).thenReturn(AUTH_PATH);
        when(request2.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request2.getRemoteAddr()).thenReturn(ip2);

        // Exhaust ip1's limit
        for (int i = 0; i < 10; i++) {
            rateLimitFilter.doFilterInternal(request1, response, filterChain);
        }

        // ip2 should still be allowed
        rateLimitFilter.doFilterInternal(request2, response, filterChain);

        // ip1 blocked (11th), ip2 passes — filter chain called 10 + 1 = 11 times
        verify(filterChain, org.mockito.Mockito.times(11)).doFilter(org.mockito.Mockito.any(), org.mockito.Mockito.eq(response));
    }

    // -------------------------------------------------------------------------
    // X-Forwarded-For header is used as client IP when present
    // -------------------------------------------------------------------------

    @Test
    void usesXForwardedForAsClientIp() throws Exception {
        when(request.getRequestURI()).thenReturn(AUTH_PATH);
        when(request.getHeader("X-Forwarded-For")).thenReturn("203.0.113.5, 10.0.0.1");

        // Single request must be allowed; getRemoteAddr must not be called
        rateLimitFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(request, never()).getRemoteAddr();
    }
}
