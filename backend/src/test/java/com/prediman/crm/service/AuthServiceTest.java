package com.prediman.crm.service;

import com.prediman.crm.dto.ForgotPasswordRequest;
import com.prediman.crm.dto.LoginRequest;
import com.prediman.crm.dto.LoginResponse;
import com.prediman.crm.dto.RefreshRequest;
import com.prediman.crm.dto.ResetPasswordRequest;
import com.prediman.crm.exception.BusinessException;
import com.prediman.crm.exception.ResourceNotFoundException;
import com.prediman.crm.model.PasswordResetToken;
import com.prediman.crm.model.Usuario;
import com.prediman.crm.model.enums.Perfil;
import com.prediman.crm.repository.PasswordResetTokenRepository;
import com.prediman.crm.repository.UsuarioRepository;
import com.prediman.crm.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService — testes unitários")
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthService authService;

    private static final String BASE_URL = "https://crm.prediman.com.br";

    @BeforeEach
    void configurarBaseUrl() {
        ReflectionTestUtils.setField(authService, "baseUrl", BASE_URL);
    }

    // ---------------------------------------------------------------------------
    // authenticate
    // ---------------------------------------------------------------------------

    @Test
    @DisplayName("authenticate com credenciais válidas retorna LoginResponse com tokens")
    void authenticate_credenciaisValidas_retornaLoginResponse() {
        LoginRequest request = new LoginRequest("admin@prediman.com", "senha123");

        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("admin@prediman.com");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(auth);

        Usuario usuario = Usuario.builder()
                .id(1L)
                .nome("Admin")
                .email("admin@prediman.com")
                .senhaHash("hash")
                .perfil(Perfil.ADMIN)
                .ativo(true)
                .build();
        when(usuarioRepository.findByEmail("admin@prediman.com")).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        when(jwtTokenProvider.generateToken("admin@prediman.com", "ADMIN")).thenReturn("access-token");
        when(jwtTokenProvider.generateRefreshToken("admin@prediman.com", "ADMIN")).thenReturn("refresh-token");

        LoginResponse response = authService.authenticate(request);

        assertThat(response.getToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(response.getNome()).isEqualTo("Admin");
        assertThat(response.getPerfil()).isEqualTo("ADMIN");
        verify(usuarioRepository).save(usuario);
    }

    @Test
    @DisplayName("authenticate com e-mail inválido lança exceção do AuthenticationManager")
    void authenticate_emailInvalido_lancaExcecao() {
        LoginRequest request = new LoginRequest("naoexiste@prediman.com", "senha123");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Credenciais inválidas"));

        assertThatThrownBy(() -> authService.authenticate(request))
                .isInstanceOf(BadCredentialsException.class);

        verify(usuarioRepository, never()).findByEmail(anyString());
    }

    @Test
    @DisplayName("authenticate com senha incorreta lança BadCredentialsException")
    void authenticate_senhaErrada_lancaExcecao() {
        LoginRequest request = new LoginRequest("admin@prediman.com", "senhaErrada");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.authenticate(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Bad credentials");
    }

    @Test
    @DisplayName("authenticate quando usuário não é encontrado no banco lança ResourceNotFoundException")
    void authenticate_usuarioNaoEncontrado_lancaResourceNotFoundException() {
        LoginRequest request = new LoginRequest("ghost@prediman.com", "senha123");

        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("ghost@prediman.com");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(auth);
        when(usuarioRepository.findByEmail("ghost@prediman.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.authenticate(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Usuário não encontrado");
    }

    @Test
    @DisplayName("authenticate registra ultimoLogin ao autenticar com sucesso")
    void authenticate_atualizaUltimoLogin() {
        LoginRequest request = new LoginRequest("user@prediman.com", "senha123");

        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("user@prediman.com");
        when(authenticationManager.authenticate(any())).thenReturn(auth);

        Usuario usuario = Usuario.builder()
                .id(2L)
                .nome("Usuário")
                .email("user@prediman.com")
                .senhaHash("hash")
                .perfil(Perfil.USUARIO)
                .ativo(true)
                .build();
        when(usuarioRepository.findByEmail("user@prediman.com")).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
        when(jwtTokenProvider.generateToken(anyString(), anyString())).thenReturn("t");
        when(jwtTokenProvider.generateRefreshToken(anyString(), anyString())).thenReturn("rt");

        authService.authenticate(request);

        assertThat(usuario.getUltimoLogin()).isNotNull();
        verify(usuarioRepository).save(usuario);
    }

    // ---------------------------------------------------------------------------
    // refresh
    // ---------------------------------------------------------------------------

    @Test
    @DisplayName("refresh com refresh token válido retorna novos tokens")
    void refresh_tokenValido_retornaNovoLoginResponse() {
        RefreshRequest request = new RefreshRequest("valid-refresh-token");

        when(jwtTokenProvider.validateToken("valid-refresh-token")).thenReturn(true);
        when(jwtTokenProvider.isRefreshToken("valid-refresh-token")).thenReturn(true);
        when(jwtTokenProvider.getEmailFromToken("valid-refresh-token")).thenReturn("user@prediman.com");

        Usuario usuario = Usuario.builder()
                .id(2L)
                .nome("Usuário")
                .email("user@prediman.com")
                .senhaHash("hash")
                .perfil(Perfil.USUARIO)
                .ativo(true)
                .build();
        when(usuarioRepository.findByEmail("user@prediman.com")).thenReturn(Optional.of(usuario));
        when(jwtTokenProvider.generateToken("user@prediman.com", "USUARIO")).thenReturn("new-access");
        when(jwtTokenProvider.generateRefreshToken("user@prediman.com", "USUARIO")).thenReturn("new-refresh");

        LoginResponse response = authService.refresh(request);

        assertThat(response.getToken()).isEqualTo("new-access");
        assertThat(response.getRefreshToken()).isEqualTo("new-refresh");
        assertThat(response.getNome()).isEqualTo("Usuário");
        assertThat(response.getPerfil()).isEqualTo("USUARIO");
    }

    @Test
    @DisplayName("refresh com token inválido (expirado/malformado) lança IllegalArgumentException")
    void refresh_tokenInvalido_lancaIllegalArgumentException() {
        RefreshRequest request = new RefreshRequest("token-expirado");
        when(jwtTokenProvider.validateToken("token-expirado")).thenReturn(false);

        assertThatThrownBy(() -> authService.refresh(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Refresh token inválido ou expirado");
    }

    @Test
    @DisplayName("refresh com access token (não refresh) lança IllegalArgumentException")
    void refresh_accessTokenFornecido_lancaIllegalArgumentException() {
        RefreshRequest request = new RefreshRequest("access-token-errado");
        when(jwtTokenProvider.validateToken("access-token-errado")).thenReturn(true);
        when(jwtTokenProvider.isRefreshToken("access-token-errado")).thenReturn(false);

        assertThatThrownBy(() -> authService.refresh(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nao e um refresh token");
    }

    @Test
    @DisplayName("refresh com usuário inativo lança IllegalArgumentException")
    void refresh_usuarioInativo_lancaIllegalArgumentException() {
        RefreshRequest request = new RefreshRequest("valid-refresh-token");

        when(jwtTokenProvider.validateToken("valid-refresh-token")).thenReturn(true);
        when(jwtTokenProvider.isRefreshToken("valid-refresh-token")).thenReturn(true);
        when(jwtTokenProvider.getEmailFromToken("valid-refresh-token")).thenReturn("inativo@prediman.com");

        Usuario usuario = Usuario.builder()
                .id(3L)
                .nome("Inativo")
                .email("inativo@prediman.com")
                .senhaHash("hash")
                .perfil(Perfil.USUARIO)
                .ativo(false)
                .build();
        when(usuarioRepository.findByEmail("inativo@prediman.com")).thenReturn(Optional.of(usuario));

        assertThatThrownBy(() -> authService.refresh(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Usuário inativo");

        verify(jwtTokenProvider, never()).generateToken(anyString(), anyString());
    }

    // ---------------------------------------------------------------------------
    // Helpers de recuperação de senha
    // ---------------------------------------------------------------------------

    private Usuario buildUsuario(Long id, String email, boolean ativo) {
        return Usuario.builder()
                .id(id)
                .nome("Fulano de Tal")
                .email(email)
                .senhaHash("hash-antigo")
                .perfil(Perfil.USUARIO)
                .ativo(ativo)
                .build();
    }

    private String sha256Hex(String valor) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(valor.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private String extrairTokenDoLink(String mensagem) {
        int inicio = mensagem.indexOf("?token=") + "?token=".length();
        int fim = mensagem.indexOf('\n', inicio);
        return mensagem.substring(inicio, fim < 0 ? mensagem.length() : fim).trim();
    }

    // ---------------------------------------------------------------------------
    // forgotPassword
    // ---------------------------------------------------------------------------

    @Test
    @DisplayName("forgotPassword com e-mail não cadastrado não gera token nem envia e-mail")
    void forgotPassword_emailNaoCadastrado_naoFazNada() {
        when(usuarioRepository.findByEmail("ninguem@prediman.com")).thenReturn(Optional.empty());

        authService.forgotPassword(new ForgotPasswordRequest("ninguem@prediman.com"));

        verify(passwordResetTokenRepository, never()).save(any());
        verify(passwordResetTokenRepository, never()).invalidarTokensPendentes(anyLong(), any());
        verify(emailService, never()).enviar(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("forgotPassword com e-mail nulo não quebra e não gera token")
    void forgotPassword_emailNulo_naoFazNada() {
        when(usuarioRepository.findByEmail("")).thenReturn(Optional.empty());

        authService.forgotPassword(new ForgotPasswordRequest(null));

        verify(passwordResetTokenRepository, never()).save(any());
        verify(emailService, never()).enviar(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("forgotPassword ignora espaços em volta do e-mail informado")
    void forgotPassword_emailComEspacos_normalizaAntesDeBuscar() {
        when(usuarioRepository.findByEmail("user@prediman.com")).thenReturn(Optional.empty());

        authService.forgotPassword(new ForgotPasswordRequest("  user@prediman.com  "));

        verify(usuarioRepository).findByEmail("user@prediman.com");
    }

    @Test
    @DisplayName("forgotPassword com usuário inativo não gera token nem envia e-mail")
    void forgotPassword_usuarioInativo_naoFazNada() {
        Usuario inativo = buildUsuario(9L, "inativo@prediman.com", false);
        when(usuarioRepository.findByEmail("inativo@prediman.com")).thenReturn(Optional.of(inativo));

        authService.forgotPassword(new ForgotPasswordRequest("inativo@prediman.com"));

        verify(passwordResetTokenRepository, never()).save(any());
        verify(emailService, never()).enviar(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("forgotPassword com usuário ativo invalida tokens anteriores, persiste hash e envia link por e-mail")
    void forgotPassword_usuarioAtivo_geraTokenEEnviaEmail() {
        Usuario usuario = buildUsuario(4L, "user@prediman.com", true);
        when(usuarioRepository.findByEmail("user@prediman.com")).thenReturn(Optional.of(usuario));
        when(emailService.enviar(anyString(), anyString(), anyString())).thenReturn(true);

        LocalDateTime antes = LocalDateTime.now();
        authService.forgotPassword(new ForgotPasswordRequest("user@prediman.com"));
        LocalDateTime depois = LocalDateTime.now();

        verify(passwordResetTokenRepository).invalidarTokensPendentes(eq(4L), any(LocalDateTime.class));

        ArgumentCaptor<PasswordResetToken> tokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(passwordResetTokenRepository).save(tokenCaptor.capture());
        PasswordResetToken salvo = tokenCaptor.getValue();

        assertThat(salvo.getUsuario()).isSameAs(usuario);
        assertThat(salvo.getUsedAt()).isNull();
        assertThat(salvo.getTokenHash()).matches("[0-9a-f]{64}");
        assertThat(salvo.getExpiresAt()).isAfter(antes.plusHours(2).minusMinutes(1));
        assertThat(salvo.getExpiresAt()).isBefore(depois.plusHours(2).plusMinutes(1));

        ArgumentCaptor<String> mensagemCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService).enviar(eq("user@prediman.com"), anyString(), mensagemCaptor.capture());
        String mensagem = mensagemCaptor.getValue();

        assertThat(mensagem).contains(BASE_URL + "/redefinir-senha?token=");
        String tokenPuro = extrairTokenDoLink(mensagem);
        // o valor em claro nunca é persistido — apenas o SHA-256 dele
        assertThat(tokenPuro).isNotEqualTo(salvo.getTokenHash());
        assertThat(sha256Hex(tokenPuro)).isEqualTo(salvo.getTokenHash());
        assertThat(tokenPuro).hasSizeGreaterThanOrEqualTo(32);
    }

    @Test
    @DisplayName("forgotPassword gera tokens diferentes a cada solicitação")
    void forgotPassword_tokensSaoAleatorios() {
        Usuario usuario = buildUsuario(4L, "user@prediman.com", true);
        when(usuarioRepository.findByEmail("user@prediman.com")).thenReturn(Optional.of(usuario));

        authService.forgotPassword(new ForgotPasswordRequest("user@prediman.com"));
        authService.forgotPassword(new ForgotPasswordRequest("user@prediman.com"));

        ArgumentCaptor<PasswordResetToken> captor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(passwordResetTokenRepository, times(2)).save(captor.capture());

        assertThat(captor.getAllValues().get(0).getTokenHash())
                .isNotEqualTo(captor.getAllValues().get(1).getTokenHash());
    }

    @Test
    @DisplayName("forgotPassword com base-url terminada em barra não duplica a barra do link")
    void forgotPassword_baseUrlComBarraFinal_naoDuplicaBarra() {
        ReflectionTestUtils.setField(authService, "baseUrl", "https://crm.prediman.com.br/");
        Usuario usuario = buildUsuario(4L, "user@prediman.com", true);
        when(usuarioRepository.findByEmail("user@prediman.com")).thenReturn(Optional.of(usuario));

        authService.forgotPassword(new ForgotPasswordRequest("user@prediman.com"));

        ArgumentCaptor<String> mensagemCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService).enviar(anyString(), anyString(), mensagemCaptor.capture());

        assertThat(mensagemCaptor.getValue()).contains("https://crm.prediman.com.br/redefinir-senha?token=");
        assertThat(mensagemCaptor.getValue()).doesNotContain("br//redefinir-senha");
    }

    @Test
    @DisplayName("forgotPassword com base-url nula usa caminho relativo sem quebrar")
    void forgotPassword_baseUrlNula_naoQuebra() {
        ReflectionTestUtils.setField(authService, "baseUrl", null);
        Usuario usuario = buildUsuario(4L, "user@prediman.com", true);
        when(usuarioRepository.findByEmail("user@prediman.com")).thenReturn(Optional.of(usuario));

        authService.forgotPassword(new ForgotPasswordRequest("user@prediman.com"));

        ArgumentCaptor<String> mensagemCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService).enviar(anyString(), anyString(), mensagemCaptor.capture());

        assertThat(mensagemCaptor.getValue()).contains("/redefinir-senha?token=");
    }

    @Test
    @DisplayName("forgotPassword não propaga falha de envio de e-mail")
    void forgotPassword_falhaNoEnvio_naoLancaExcecao() {
        Usuario usuario = buildUsuario(4L, "user@prediman.com", true);
        when(usuarioRepository.findByEmail("user@prediman.com")).thenReturn(Optional.of(usuario));
        when(emailService.enviar(anyString(), anyString(), anyString())).thenReturn(false);

        authService.forgotPassword(new ForgotPasswordRequest("user@prediman.com"));

        verify(passwordResetTokenRepository).save(any(PasswordResetToken.class));
    }

    // ---------------------------------------------------------------------------
    // resetPassword
    // ---------------------------------------------------------------------------

    @Test
    @DisplayName("resetPassword com token inexistente lança BusinessException")
    void resetPassword_tokenInexistente_lancaBusinessException() {
        when(passwordResetTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.resetPassword(new ResetPasswordRequest("qualquer", "novaSenha123")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Link de redefinição inválido");

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("resetPassword busca o token pelo hash e nunca pelo valor em claro")
    void resetPassword_buscaPeloHashDoToken() {
        when(passwordResetTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.resetPassword(new ResetPasswordRequest("token-puro", "novaSenha123")))
                .isInstanceOf(BusinessException.class);

        verify(passwordResetTokenRepository).findByTokenHash(sha256Hex("token-puro"));
        verify(passwordResetTokenRepository, never()).findByTokenHash("token-puro");
    }

    @Test
    @DisplayName("resetPassword com token já utilizado lança BusinessException")
    void resetPassword_tokenJaUtilizado_lancaBusinessException() {
        PasswordResetToken token = PasswordResetToken.builder()
                .id(1L)
                .usuario(buildUsuario(4L, "user@prediman.com", true))
                .tokenHash(sha256Hex("token-puro"))
                .expiresAt(LocalDateTime.now().plusHours(1))
                .usedAt(LocalDateTime.now().minusMinutes(5))
                .build();
        when(passwordResetTokenRepository.findByTokenHash(sha256Hex("token-puro")))
                .thenReturn(Optional.of(token));

        assertThatThrownBy(() -> authService.resetPassword(new ResetPasswordRequest("token-puro", "novaSenha123")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("já foi utilizado");

        verify(usuarioRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    @DisplayName("resetPassword com token expirado lança BusinessException")
    void resetPassword_tokenExpirado_lancaBusinessException() {
        PasswordResetToken token = PasswordResetToken.builder()
                .id(1L)
                .usuario(buildUsuario(4L, "user@prediman.com", true))
                .tokenHash(sha256Hex("token-puro"))
                .expiresAt(LocalDateTime.now().minusMinutes(1))
                .build();
        when(passwordResetTokenRepository.findByTokenHash(sha256Hex("token-puro")))
                .thenReturn(Optional.of(token));

        assertThatThrownBy(() -> authService.resetPassword(new ResetPasswordRequest("token-puro", "novaSenha123")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("expirado");

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("resetPassword com expiresAt nulo lança BusinessException")
    void resetPassword_expiresAtNulo_lancaBusinessException() {
        PasswordResetToken token = PasswordResetToken.builder()
                .id(1L)
                .usuario(buildUsuario(4L, "user@prediman.com", true))
                .tokenHash(sha256Hex("token-puro"))
                .build();
        when(passwordResetTokenRepository.findByTokenHash(sha256Hex("token-puro")))
                .thenReturn(Optional.of(token));

        assertThatThrownBy(() -> authService.resetPassword(new ResetPasswordRequest("token-puro", "novaSenha123")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("expirado");
    }

    @Test
    @DisplayName("resetPassword de usuário inativo lança BusinessException")
    void resetPassword_usuarioInativo_lancaBusinessException() {
        PasswordResetToken token = PasswordResetToken.builder()
                .id(1L)
                .usuario(buildUsuario(4L, "user@prediman.com", false))
                .tokenHash(sha256Hex("token-puro"))
                .expiresAt(LocalDateTime.now().plusHours(1))
                .build();
        when(passwordResetTokenRepository.findByTokenHash(sha256Hex("token-puro")))
                .thenReturn(Optional.of(token));

        assertThatThrownBy(() -> authService.resetPassword(new ResetPasswordRequest("token-puro", "novaSenha123")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Usuário inativo");
    }

    @Test
    @DisplayName("resetPassword com token órfão (sem usuário) lança BusinessException")
    void resetPassword_tokenSemUsuario_lancaBusinessException() {
        PasswordResetToken token = PasswordResetToken.builder()
                .id(1L)
                .tokenHash(sha256Hex("token-puro"))
                .expiresAt(LocalDateTime.now().plusHours(1))
                .build();
        when(passwordResetTokenRepository.findByTokenHash(sha256Hex("token-puro")))
                .thenReturn(Optional.of(token));

        assertThatThrownBy(() -> authService.resetPassword(new ResetPasswordRequest("token-puro", "novaSenha123")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Usuário inativo");
    }

    @Test
    @DisplayName("resetPassword com token válido grava senha codificada e marca o token como usado")
    void resetPassword_tokenValido_atualizaSenhaEConsomeToken() {
        Usuario usuario = buildUsuario(4L, "user@prediman.com", true);
        PasswordResetToken token = PasswordResetToken.builder()
                .id(1L)
                .usuario(usuario)
                .tokenHash(sha256Hex("token-puro"))
                .expiresAt(LocalDateTime.now().plusHours(1))
                .build();
        when(passwordResetTokenRepository.findByTokenHash(sha256Hex("token-puro")))
                .thenReturn(Optional.of(token));
        when(passwordEncoder.encode("novaSenha123")).thenReturn("novo-hash-bcrypt");

        authService.resetPassword(new ResetPasswordRequest("token-puro", "novaSenha123"));

        assertThat(usuario.getSenhaHash()).isEqualTo("novo-hash-bcrypt");
        assertThat(token.getUsedAt()).isNotNull();
        verify(usuarioRepository).save(usuario);
        verify(passwordResetTokenRepository).save(token);
    }

    @Test
    @DisplayName("PasswordResetToken.isValido reflete uso e expiração")
    void passwordResetToken_isValido() {
        LocalDateTime agora = LocalDateTime.now();

        PasswordResetToken valido = PasswordResetToken.builder()
                .expiresAt(agora.plusHours(1))
                .build();
        PasswordResetToken usado = PasswordResetToken.builder()
                .expiresAt(agora.plusHours(1))
                .usedAt(agora)
                .build();
        PasswordResetToken expirado = PasswordResetToken.builder()
                .expiresAt(agora.minusMinutes(1))
                .build();
        PasswordResetToken semExpiracao = PasswordResetToken.builder().build();

        assertThat(valido.isValido(agora)).isTrue();
        assertThat(usado.isValido(agora)).isFalse();
        assertThat(expirado.isValido(agora)).isFalse();
        assertThat(semExpiracao.isValido(agora)).isFalse();
    }
}
