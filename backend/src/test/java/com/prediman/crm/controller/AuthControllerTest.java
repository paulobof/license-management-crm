package com.prediman.crm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prediman.crm.dto.ForgotPasswordRequest;
import com.prediman.crm.dto.LoginRequest;
import com.prediman.crm.dto.LoginResponse;
import com.prediman.crm.dto.RefreshRequest;
import com.prediman.crm.dto.ResetPasswordRequest;
import com.prediman.crm.exception.BusinessException;
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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
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

    // -------------------------------------------------------------------------
    // POST /api/v1/auth/forgot-password
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("POST /api/v1/auth/forgot-password — e-mail cadastrado retorna 200 com mensagem genérica")
    void forgotPassword_emailCadastrado_returns200() throws Exception {
        ForgotPasswordRequest request = new ForgotPasswordRequest("user@prediman.com");

        mockMvc.perform(post("/api/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensagem").value(AuthController.MENSAGEM_FORGOT_PASSWORD));

        verify(authService).forgotPassword(any(ForgotPasswordRequest.class));
    }

    @Test
    @DisplayName("POST /api/v1/auth/forgot-password — e-mail inexistente também retorna 200 com a mesma mensagem")
    void forgotPassword_emailInexistente_returns200MesmaMensagem() throws Exception {
        // o serviço não sinaliza nada quando o e-mail não existe (evita enumeração de contas)
        doNothing().when(authService).forgotPassword(any(ForgotPasswordRequest.class));
        ForgotPasswordRequest request = new ForgotPasswordRequest("ninguem@prediman.com");

        mockMvc.perform(post("/api/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensagem").value(AuthController.MENSAGEM_FORGOT_PASSWORD));
    }

    @Test
    @DisplayName("POST /api/v1/auth/forgot-password — e-mail em branco retorna 400")
    void forgotPassword_blankEmail_returns400() throws Exception {
        ForgotPasswordRequest request = new ForgotPasswordRequest("");

        mockMvc.perform(post("/api/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/auth/forgot-password — e-mail inválido retorna 400")
    void forgotPassword_invalidEmail_returns400() throws Exception {
        ForgotPasswordRequest request = new ForgotPasswordRequest("nao-e-email");

        mockMvc.perform(post("/api/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // -------------------------------------------------------------------------
    // POST /api/v1/auth/reset-password
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("POST /api/v1/auth/reset-password — token válido retorna 200")
    void resetPassword_tokenValido_returns200() throws Exception {
        ResetPasswordRequest request = new ResetPasswordRequest("token-opaco", "novaSenha123");

        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensagem").value(AuthController.MENSAGEM_RESET_PASSWORD));

        verify(authService).resetPassword(any(ResetPasswordRequest.class));
    }

    @Test
    @DisplayName("POST /api/v1/auth/reset-password — token inválido/expirado retorna 422")
    void resetPassword_tokenInvalido_returns422() throws Exception {
        doThrow(new BusinessException("Link de redefinição expirado. Solicite uma nova recuperação de senha."))
                .when(authService).resetPassword(any(ResetPasswordRequest.class));

        ResetPasswordRequest request = new ResetPasswordRequest("token-expirado", "novaSenha123");

        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value(
                        "Link de redefinição expirado. Solicite uma nova recuperação de senha."));
    }

    @Test
    @DisplayName("POST /api/v1/auth/reset-password — token em branco retorna 400")
    void resetPassword_blankToken_returns400() throws Exception {
        ResetPasswordRequest request = new ResetPasswordRequest("", "novaSenha123");

        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/auth/reset-password — senha curta retorna 400")
    void resetPassword_senhaCurta_returns400() throws Exception {
        ResetPasswordRequest request = new ResetPasswordRequest("token-opaco", "1234567");

        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/auth/reset-password — senha em branco retorna 400")
    void resetPassword_senhaEmBranco_returns400() throws Exception {
        ResetPasswordRequest request = new ResetPasswordRequest("token-opaco", "");

        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
