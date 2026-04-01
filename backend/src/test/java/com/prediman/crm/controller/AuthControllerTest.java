package com.prediman.crm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prediman.crm.dto.LoginRequest;
import com.prediman.crm.dto.LoginResponse;
import com.prediman.crm.dto.RefreshRequest;
import com.prediman.crm.security.JwtAuthenticationFilter;
import com.prediman.crm.security.JwtTokenProvider;
import com.prediman.crm.security.RateLimitFilter;
import com.prediman.crm.security.UserDetailsServiceImpl;
import com.prediman.crm.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("AuthController — testes de unidade")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @MockBean
    private RateLimitFilter rateLimitFilter;

    // -------------------------------------------------------------------------
    // POST /api/v1/auth/login
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("POST /api/v1/auth/login — sucesso retorna 200 com token")
    void login_success() throws Exception {
        LoginRequest request = new LoginRequest("admin@example.com", "senha123");
        LoginResponse response = LoginResponse.builder()
                .token("access-token")
                .refreshToken("refresh-token")
                .nome("Admin")
                .perfil("ADMIN")
                .build();

        when(authService.authenticate(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.nome").value("Admin"))
                .andExpect(jsonPath("$.perfil").value("ADMIN"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login — email em branco retorna 400")
    void login_blankEmail_returns400() throws Exception {
        LoginRequest request = new LoginRequest("", "senha123");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/auth/login — email inválido retorna 400")
    void login_invalidEmail_returns400() throws Exception {
        LoginRequest request = new LoginRequest("nao-e-email", "senha123");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/auth/login — senha em branco retorna 400")
    void login_blankPassword_returns400() throws Exception {
        LoginRequest request = new LoginRequest("admin@example.com", "");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // -------------------------------------------------------------------------
    // POST /api/v1/auth/refresh
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("POST /api/v1/auth/refresh — sucesso retorna 200 com novo token")
    void refresh_success() throws Exception {
        RefreshRequest request = new RefreshRequest("valid-refresh-token");
        LoginResponse response = LoginResponse.builder()
                .token("new-access-token")
                .refreshToken("new-refresh-token")
                .nome("Admin")
                .perfil("ADMIN")
                .build();

        when(authService.refresh(any(RefreshRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("new-access-token"))
                .andExpect(jsonPath("$.refreshToken").value("new-refresh-token"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/refresh — refreshToken em branco retorna 400")
    void refresh_blankToken_returns400() throws Exception {
        RefreshRequest request = new RefreshRequest("");

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
