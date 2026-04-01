package com.prediman.crm.security;

import com.prediman.crm.model.Usuario;
import com.prediman.crm.model.enums.Perfil;
import com.prediman.crm.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserDetailsServiceImpl")
class UserDetailsServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    private UserDetailsServiceImpl userDetailsService;

    @BeforeEach
    void setUp() {
        userDetailsService = new UserDetailsServiceImpl(usuarioRepository);
    }

    @Test
    @DisplayName("loadUserByUsername com usuario ativo retorna UserDetails correto")
    void loadUserByUsername_usuarioAtivoRetornaUserDetails() {
        Usuario usuario = Usuario.builder()
                .id(1L)
                .nome("João Silva")
                .email("joao@prediman.com.br")
                .senhaHash("$2a$10$hashedpassword")
                .perfil(Perfil.ADMIN)
                .ativo(true)
                .build();

        when(usuarioRepository.findByEmail("joao@prediman.com.br"))
                .thenReturn(Optional.of(usuario));

        UserDetails details = userDetailsService.loadUserByUsername("joao@prediman.com.br");

        assertThat(details).isNotNull();
        assertThat(details.getUsername()).isEqualTo("joao@prediman.com.br");
        assertThat(details.getPassword()).isEqualTo("$2a$10$hashedpassword");
        assertThat(details.getAuthorities()).hasSize(1);
        assertThat(details.getAuthorities().iterator().next().getAuthority())
                .isEqualTo("ROLE_ADMIN");
    }

    @Test
    @DisplayName("loadUserByUsername com usuario de perfil USUARIO retorna role correta")
    void loadUserByUsername_usuarioPerfilUsuarioRetornaRoleCorreta() {
        Usuario usuario = Usuario.builder()
                .id(2L)
                .email("user@prediman.com.br")
                .senhaHash("$2a$10$hash")
                .perfil(Perfil.USUARIO)
                .ativo(true)
                .build();

        when(usuarioRepository.findByEmail("user@prediman.com.br"))
                .thenReturn(Optional.of(usuario));

        UserDetails details = userDetailsService.loadUserByUsername("user@prediman.com.br");

        assertThat(details.getAuthorities().iterator().next().getAuthority())
                .isEqualTo("ROLE_USUARIO");
    }

    @Test
    @DisplayName("loadUserByUsername com email inexistente lanca UsernameNotFoundException")
    void loadUserByUsername_emailNaoEncontradoLancaException() {
        when(usuarioRepository.findByEmail("inexistente@prediman.com.br"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                userDetailsService.loadUserByUsername("inexistente@prediman.com.br"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("inexistente@prediman.com.br");
    }

    @Test
    @DisplayName("loadUserByUsername com usuario inativo lanca UsernameNotFoundException")
    void loadUserByUsername_usuarioInativoLancaException() {
        Usuario usuario = Usuario.builder()
                .id(3L)
                .email("inativo@prediman.com.br")
                .senhaHash("$2a$10$hash")
                .perfil(Perfil.USUARIO)
                .ativo(false)
                .build();

        when(usuarioRepository.findByEmail("inativo@prediman.com.br"))
                .thenReturn(Optional.of(usuario));

        assertThatThrownBy(() ->
                userDetailsService.loadUserByUsername("inativo@prediman.com.br"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("inativo");
    }
}
