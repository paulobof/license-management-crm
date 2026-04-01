package com.prediman.crm.service;

import com.prediman.crm.dto.LoginRequest;
import com.prediman.crm.dto.LoginResponse;
import com.prediman.crm.dto.RefreshRequest;
import com.prediman.crm.exception.ResourceNotFoundException;
import com.prediman.crm.model.Usuario;
import com.prediman.crm.model.enums.Perfil;
import com.prediman.crm.repository.UsuarioRepository;
import com.prediman.crm.security.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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

    @InjectMocks
    private AuthService authService;

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
}
