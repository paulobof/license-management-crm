package com.prediman.crm.config;

import com.prediman.crm.model.Usuario;
import com.prediman.crm.model.enums.Perfil;
import com.prediman.crm.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DataSeeder")
class DataSeederTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private DataSeeder dataSeeder;

    @BeforeEach
    void setUp() {
        dataSeeder = new DataSeeder(usuarioRepository, passwordEncoder);
    }

    @Test
    @DisplayName("run nao cria admin quando ja existe pelo menos um admin ativo")
    void run_naoFazNadaQuandoAdminExiste() throws Exception {
        when(usuarioRepository.countByPerfilAndAtivoTrue(Perfil.ADMIN)).thenReturn(1L);

        dataSeeder.run();

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("run nao cria admin quando senha inicial nao esta configurada")
    void run_naoFazNadaQuandoSenhaVazia() throws Exception {
        when(usuarioRepository.countByPerfilAndAtivoTrue(Perfil.ADMIN)).thenReturn(0L);
        ReflectionTestUtils.setField(dataSeeder, "adminInitialPassword", "");

        dataSeeder.run();

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("run nao cria admin quando senha inicial e apenas espacos")
    void run_naoFazNadaQuandoSenhaApenasEspacos() throws Exception {
        when(usuarioRepository.countByPerfilAndAtivoTrue(Perfil.ADMIN)).thenReturn(0L);
        ReflectionTestUtils.setField(dataSeeder, "adminInitialPassword", "   ");

        dataSeeder.run();

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("run cria novo admin quando nenhum admin existe e senha esta configurada")
    void run_criaNovoAdminQuandoNenhumAdminExiste() throws Exception {
        when(usuarioRepository.countByPerfilAndAtivoTrue(Perfil.ADMIN)).thenReturn(0L);
        ReflectionTestUtils.setField(dataSeeder, "adminInitialPassword", "senhaSegura123");
        when(usuarioRepository.findByEmail("admin@prediman.com.br")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("senhaSegura123")).thenReturn("$2a$10$hashedSenha");

        dataSeeder.run();

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());

        Usuario savedAdmin = captor.getValue();
        assertThat(savedAdmin.getEmail()).isEqualTo("admin@prediman.com.br");
        assertThat(savedAdmin.getNome()).isEqualTo("Administrador");
        assertThat(savedAdmin.getPerfil()).isEqualTo(Perfil.ADMIN);
        assertThat(savedAdmin.getAtivo()).isTrue();
        assertThat(savedAdmin.getSenhaHash()).isEqualTo("$2a$10$hashedSenha");
    }

    @Test
    @DisplayName("run reativa admin existente quando admin esta inativo e senha esta configurada")
    void run_reativaAdminExistenteInativo() throws Exception {
        when(usuarioRepository.countByPerfilAndAtivoTrue(Perfil.ADMIN)).thenReturn(0L);
        ReflectionTestUtils.setField(dataSeeder, "adminInitialPassword", "novaSenha456");

        Usuario adminInativo = Usuario.builder()
                .id(99L)
                .email("admin@prediman.com.br")
                .nome("Admin Antigo")
                .senhaHash("$2a$10$oldHash")
                .perfil(Perfil.USUARIO)
                .ativo(false)
                .build();

        when(usuarioRepository.findByEmail("admin@prediman.com.br"))
                .thenReturn(Optional.of(adminInativo));
        when(passwordEncoder.encode("novaSenha456")).thenReturn("$2a$10$newHash");

        dataSeeder.run();

        verify(usuarioRepository).save(adminInativo);
        assertThat(adminInativo.getAtivo()).isTrue();
        assertThat(adminInativo.getPerfil()).isEqualTo(Perfil.ADMIN);
        assertThat(adminInativo.getSenhaHash()).isEqualTo("$2a$10$newHash");
    }
}
