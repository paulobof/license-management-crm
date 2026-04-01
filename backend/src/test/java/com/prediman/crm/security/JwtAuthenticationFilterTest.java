package com.prediman.crm.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    // -------------------------------------------------------------------------
    // doFilterInternal — valid token
    // -------------------------------------------------------------------------

    @Test
    void doFilterInternal_withValidToken_setsAuthentication() throws Exception {
        String token = "valid.jwt.token";
        String email = "user@test.com";
        UserDetails userDetails = new User(email, "", Collections.emptyList());

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtTokenProvider.validateToken(token)).thenReturn(true);
        when(jwtTokenProvider.isAccessToken(token)).thenReturn(true);
        when(jwtTokenProvider.getEmailFromToken(token)).thenReturn(email);
        when(userDetailsService.loadUserByUsername(email)).thenReturn(userDetails);

        SecurityContextHolder.clearContext();

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.getPrincipal()).isEqualTo(userDetails);

        verify(filterChain).doFilter(request, response);

        SecurityContextHolder.clearContext();
    }

    // -------------------------------------------------------------------------
    // doFilterInternal — no token
    // -------------------------------------------------------------------------

    @Test
    void doFilterInternal_withoutToken_continuesFilterChain() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        SecurityContextHolder.clearContext();

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNull();

        verify(filterChain).doFilter(request, response);
        verify(jwtTokenProvider, never()).validateToken(any());
    }

    // -------------------------------------------------------------------------
    // doFilterInternal — invalid token
    // -------------------------------------------------------------------------

    @Test
    void doFilterInternal_withInvalidToken_continuesWithoutAuthentication() throws Exception {
        String token = "invalid.jwt.token";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtTokenProvider.validateToken(token)).thenReturn(false);

        SecurityContextHolder.clearContext();

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNull();

        verify(filterChain).doFilter(request, response);
        verify(userDetailsService, never()).loadUserByUsername(any());
    }

    // -------------------------------------------------------------------------
    // doFilterInternal — valid token but not an access token (e.g. refresh)
    // -------------------------------------------------------------------------

    @Test
    void doFilterInternal_withRefreshToken_continuesWithoutAuthentication() throws Exception {
        String token = "refresh.jwt.token";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtTokenProvider.validateToken(token)).thenReturn(true);
        when(jwtTokenProvider.isAccessToken(token)).thenReturn(false);

        SecurityContextHolder.clearContext();

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNull();

        verify(filterChain).doFilter(request, response);
        verify(userDetailsService, never()).loadUserByUsername(any());
    }

    // -------------------------------------------------------------------------
    // extractTokenFromRequest — correct Bearer extraction
    // -------------------------------------------------------------------------

    @Test
    void extractTokenFromRequest_extractsBearerTokenCorrectly() throws Exception {
        String token = "some.valid.token";
        String email = "admin@test.com";
        UserDetails userDetails = new User(email, "", Collections.emptyList());

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtTokenProvider.validateToken(token)).thenReturn(true);
        when(jwtTokenProvider.isAccessToken(token)).thenReturn(true);
        when(jwtTokenProvider.getEmailFromToken(token)).thenReturn(email);
        when(userDetailsService.loadUserByUsername(email)).thenReturn(userDetails);

        SecurityContextHolder.clearContext();

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // If extraction worked, authentication should be set with the correct user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        UserDetails principal = (UserDetails) authentication.getPrincipal();
        assertThat(principal.getUsername()).isEqualTo(email);

        SecurityContextHolder.clearContext();
    }

    // -------------------------------------------------------------------------
    // extractTokenFromRequest — non-Bearer header returns null (no auth set)
    // -------------------------------------------------------------------------

    @Test
    void extractTokenFromRequest_returnsNullForNonBearerHeader() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Basic dXNlcjpwYXNz");

        SecurityContextHolder.clearContext();

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Non-Bearer scheme must not set authentication
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNull();

        verify(filterChain).doFilter(request, response);
        verify(jwtTokenProvider, never()).validateToken(any());
    }

    // -------------------------------------------------------------------------
    // Helper — avoid repeating the unchecked Mockito any() import
    // -------------------------------------------------------------------------

    private static String any() {
        return org.mockito.Mockito.any();
    }
}
