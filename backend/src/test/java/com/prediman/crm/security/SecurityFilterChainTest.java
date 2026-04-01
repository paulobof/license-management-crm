package com.prediman.crm.security;

import com.prediman.crm.controller.AuthController;
import com.prediman.crm.service.AuthService;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testa o filterChain do SecurityConfig instanciando a cadeia de filtros real via WebMvcTest.
 * Importa SecurityConfig explicitamente para garantir cobertura de filterChain().
 */
@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = {
        "jwt.secret=test-only-prediman-crm-jwt-secret-key-must-be-at-least-256-bits-long-for-hs256",
        "jwt.expiration=900000",
        "jwt.refresh-expiration=604800000",
        "cors.allowed-origins=http://localhost:5173"
})
@DisplayName("SecurityConfig — filterChain integrado")
class SecurityFilterChainTest {

    @Autowired
    private MockMvc mockMvc;

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

    @BeforeEach
    void setup() throws Exception {
        // Faz os filtros mockados delegar para a chain, permitindo que
        // a configuracao de segurança (authorizeHttpRequests) seja avaliada
        doAnswer(inv -> {
            FilterChain chain = inv.getArgument(2);
            chain.doFilter(inv.getArgument(0), inv.getArgument(1));
            return null;
        }).when(jwtAuthenticationFilter).doFilter(any(), any(), any());

        doAnswer(inv -> {
            FilterChain chain = inv.getArgument(2);
            chain.doFilter(inv.getArgument(0), inv.getArgument(1));
            return null;
        }).when(rateLimitFilter).doFilter(any(), any(), any());
    }

    @Test
    @DisplayName("endpoint protegido sem token retorna 401 ou 403")
    void endpointProtegido_semToken_retornaForbiddenOuUnauthorized() throws Exception {
        int status = mockMvc.perform(get("/api/v1/clientes"))
                .andReturn().getResponse().getStatus();
        // Sem autenticacao, Spring Security retorna 401 ou 403 dependendo da config
        org.junit.jupiter.api.Assertions.assertTrue(
                status == 401 || status == 403,
                "Esperado 401 ou 403, mas recebeu " + status);
    }

    @Test
    @DisplayName("endpoint publico /api/v1/auth aceita requisicao sem autenticacao")
    void endpointPublico_auth_naoExigeToken() throws Exception {
        // /api/v1/auth/** e permitAll() — nao deve retornar 401/403 (nao precisa de token)
        int status = mockMvc.perform(get("/api/v1/auth/qualquer-coisa"))
                .andReturn().getResponse().getStatus();
        org.junit.jupiter.api.Assertions.assertTrue(
                status != 401 && status != 403,
                "Endpoint publico nao deveria exigir autenticacao, mas retornou " + status);
    }
}
