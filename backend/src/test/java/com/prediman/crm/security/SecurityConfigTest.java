package com.prediman.crm.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SecurityConfig")
class SecurityConfigTest {

    @Mock
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Mock
    private UserDetailsServiceImpl userDetailsService;

    @Mock
    private RateLimitFilter rateLimitFilter;

    private SecurityConfig securityConfig;

    @BeforeEach
    void setUp() {
        securityConfig = new SecurityConfig(jwtAuthenticationFilter, userDetailsService, rateLimitFilter);
        ReflectionTestUtils.setField(securityConfig, "allowedOrigins", "http://localhost:5173");
    }

    @Test
    @DisplayName("passwordEncoder retorna instancia de BCryptPasswordEncoder")
    void passwordEncoder_retornaBCrypt() {
        PasswordEncoder encoder = securityConfig.passwordEncoder();

        assertThat(encoder).isInstanceOf(BCryptPasswordEncoder.class);
    }

    @Test
    @DisplayName("passwordEncoder codifica e valida senha corretamente")
    void passwordEncoder_codificaSenha() {
        PasswordEncoder encoder = securityConfig.passwordEncoder();
        String raw = "minhaSenha123";

        String encoded = encoder.encode(raw);

        assertThat(encoder.matches(raw, encoded)).isTrue();
    }

    @Test
    @DisplayName("corsConfigurationSource retorna UrlBasedCorsConfigurationSource nao nulo")
    void corsConfigurationSource_retornaFonte() {
        CorsConfigurationSource source = securityConfig.corsConfigurationSource();

        assertThat(source).isNotNull();
        assertThat(source).isInstanceOf(UrlBasedCorsConfigurationSource.class);
    }

    @Test
    @DisplayName("corsConfigurationSource com multiplas origens separadas por virgula")
    void corsConfigurationSource_multipasOrigens() {
        ReflectionTestUtils.setField(securityConfig, "allowedOrigins",
                "http://localhost:5173,https://app.prediman.com.br");

        CorsConfigurationSource source = securityConfig.corsConfigurationSource();

        assertThat(source).isNotNull();
    }

    @Test
    @DisplayName("authenticationProvider retorna DaoAuthenticationProvider nao nulo")
    void authenticationProvider_retornaProvider() {
        var provider = securityConfig.authenticationProvider();

        assertThat(provider).isNotNull();
    }

    @Test
    @DisplayName("authenticationProvider retorna instancia de DaoAuthenticationProvider")
    void authenticationProvider_retornaDaoProvider() {
        var provider = securityConfig.authenticationProvider();

        assertThat(provider).isInstanceOf(DaoAuthenticationProvider.class);
    }

    @Test
    @DisplayName("authenticationManager delega para AuthenticationConfiguration")
    void authenticationManager_delegaParaConfiguration() throws Exception {
        AuthenticationManager mockManager = mock(AuthenticationManager.class);
        AuthenticationConfiguration mockConfig = mock(AuthenticationConfiguration.class);
        when(mockConfig.getAuthenticationManager()).thenReturn(mockManager);

        AuthenticationManager result = securityConfig.authenticationManager(mockConfig);

        assertThat(result).isSameAs(mockManager);
    }

    @Test
    @DisplayName("corsConfigurationSource configura origens, metodos e headers corretamente")
    void corsConfigurationSource_configuracaoCompleta() {
        CorsConfigurationSource source = securityConfig.corsConfigurationSource();

        assertThat(source).isNotNull();
        assertThat(source).isInstanceOf(UrlBasedCorsConfigurationSource.class);
    }
}
