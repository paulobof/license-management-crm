package com.prediman.crm.service;

import com.prediman.crm.dto.UsuarioRequest;
import com.prediman.crm.dto.UsuarioResponse;
import com.prediman.crm.dto.UsuarioUpdateRequest;
import com.prediman.crm.exception.BusinessException;
import com.prediman.crm.exception.ResourceNotFoundException;
import com.prediman.crm.model.Usuario;
import com.prediman.crm.model.enums.Perfil;
import com.prediman.crm.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService — testes unitários")
class UserServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UsuarioMapper usuarioMapper;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private UserService userService;

    // ---------------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------------

    private Usuario buildUsuario(Long id, String email, Perfil perfil, boolean ativo) {
        return Usuario.builder()
                .id(id)
                .nome("Nome Teste")
                .email(email)
                .senhaHash("hashed")
                .perfil(perfil)
                .ativo(ativo)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private UsuarioResponse buildResponse(Long id, String email, Perfil perfil, boolean ativo) {
        return UsuarioResponse.builder()
                .id(id)
                .nome("Nome Teste")
                .email(email)
                .perfil(perfil)
                .ativo(ativo)
                .build();
    }

    // ---------------------------------------------------------------------------
    // create
    // ---------------------------------------------------------------------------

    @Test
    @DisplayName("create com e-mail novo persiste e retorna UsuarioResponse")
    void create_emailNovo_persisteERetornaResponse() {
        UsuarioRequest request = UsuarioRequest.builder()
                .nome("Novo Usuário")
                .email("novo@prediman.com")
                .senha("senha12345")
                .perfil(Perfil.USUARIO)
                .build();

        when(usuarioRepository.existsByEmail("novo@prediman.com")).thenReturn(false);
        when(passwordEncoder.encode("senha12345")).thenReturn("hashed-senha");

        Usuario savedEntity = buildUsuario(10L, "novo@prediman.com", Perfil.USUARIO, true);
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(savedEntity);

        UsuarioResponse response = buildResponse(10L, "novo@prediman.com", Perfil.USUARIO, true);
        when(usuarioMapper.toResponse(savedEntity)).thenReturn(response);

        UsuarioResponse result = userService.create(request);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getEmail()).isEqualTo("novo@prediman.com");
        assertThat(result.getAtivo()).isTrue();
        verify(passwordEncoder).encode("senha12345");
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    @DisplayName("create com e-mail duplicado lança BusinessException")
    void create_emailDuplicado_lancaBusinessException() {
        UsuarioRequest request = UsuarioRequest.builder()
                .nome("Admin")
                .email("admin@prediman.com")
                .senha("senha12345")
                .perfil(Perfil.ADMIN)
                .build();

        when(usuarioRepository.existsByEmail("admin@prediman.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("E-mail já cadastrado");

        verify(usuarioRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(anyString());
        verify(emailService, never()).enviar(anyString(), anyString(), anyString());
    }

    // ---------------------------------------------------------------------------
    // create — e-mail de boas-vindas
    // ---------------------------------------------------------------------------

    @Test
    @DisplayName("create envia e-mail de boas-vindas com nome, e-mail de acesso e senha inicial")
    void create_enviaEmailDeBoasVindasComCredenciais() {
        UsuarioRequest request = UsuarioRequest.builder()
                .nome("Novo Usuário")
                .email("novo@prediman.com")
                .senha("senha12345")
                .perfil(Perfil.USUARIO)
                .build();

        Usuario savedEntity = buildUsuario(10L, "novo@prediman.com", Perfil.USUARIO, true);
        when(usuarioRepository.existsByEmail("novo@prediman.com")).thenReturn(false);
        when(passwordEncoder.encode("senha12345")).thenReturn("hashed-senha");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(savedEntity);
        when(usuarioMapper.toResponse(savedEntity))
                .thenReturn(buildResponse(10L, "novo@prediman.com", Perfil.USUARIO, true));
        when(emailService.enviar(anyString(), anyString(), anyString())).thenReturn(true);

        userService.create(request);

        ArgumentCaptor<String> assuntoCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> mensagemCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService).enviar(eq("novo@prediman.com"), assuntoCaptor.capture(), mensagemCaptor.capture());

        assertThat(assuntoCaptor.getValue()).contains("Bem-vindo");
        String mensagem = mensagemCaptor.getValue();
        assertThat(mensagem).contains("Nome Teste");
        assertThat(mensagem).contains("novo@prediman.com");
        assertThat(mensagem).contains("senha12345");
        assertThat(mensagem).contains("Esqueci minha senha");
    }

    @Test
    @DisplayName("create conclui mesmo quando o envio do e-mail de boas-vindas retorna false")
    void create_envioDeEmailRetornaFalse_naoAbortaCriacao() {
        UsuarioRequest request = UsuarioRequest.builder()
                .nome("Novo Usuário")
                .email("novo@prediman.com")
                .senha("senha12345")
                .perfil(Perfil.USUARIO)
                .build();

        Usuario savedEntity = buildUsuario(11L, "novo@prediman.com", Perfil.USUARIO, true);
        UsuarioResponse response = buildResponse(11L, "novo@prediman.com", Perfil.USUARIO, true);
        when(usuarioRepository.existsByEmail("novo@prediman.com")).thenReturn(false);
        when(passwordEncoder.encode("senha12345")).thenReturn("hashed-senha");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(savedEntity);
        when(usuarioMapper.toResponse(savedEntity)).thenReturn(response);
        when(emailService.enviar(anyString(), anyString(), anyString())).thenReturn(false);

        UsuarioResponse result = userService.create(request);

        assertThat(result).isEqualTo(response);
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    @DisplayName("create conclui mesmo quando o envio do e-mail de boas-vindas lança exceção")
    void create_envioDeEmailLancaExcecao_naoAbortaCriacao() {
        UsuarioRequest request = UsuarioRequest.builder()
                .nome("Novo Usuário")
                .email("novo@prediman.com")
                .senha("senha12345")
                .perfil(Perfil.USUARIO)
                .build();

        Usuario savedEntity = buildUsuario(12L, "novo@prediman.com", Perfil.USUARIO, true);
        UsuarioResponse response = buildResponse(12L, "novo@prediman.com", Perfil.USUARIO, true);
        when(usuarioRepository.existsByEmail("novo@prediman.com")).thenReturn(false);
        when(passwordEncoder.encode("senha12345")).thenReturn("hashed-senha");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(savedEntity);
        when(usuarioMapper.toResponse(savedEntity)).thenReturn(response);
        when(emailService.enviar(anyString(), anyString(), anyString()))
                .thenThrow(new IllegalStateException("SMTP indisponível"));

        UsuarioResponse result = userService.create(request);

        assertThat(result).isEqualTo(response);
        verify(usuarioRepository).save(any(Usuario.class));
    }

    // ---------------------------------------------------------------------------
    // update
    // ---------------------------------------------------------------------------

    @Test
    @DisplayName("update com dados válidos e mesmo e-mail atualiza usuário")
    void update_dadosValidosMesmoEmail_atualizaUsuario() {
        Long id = 5L;
        Usuario entity = buildUsuario(id, "user@prediman.com", Perfil.USUARIO, true);
        UsuarioUpdateRequest request = UsuarioUpdateRequest.builder()
                .nome("Nome Atualizado")
                .email("user@prediman.com")  // mesmo e-mail — sem verificação de duplicidade
                .perfil(Perfil.USUARIO)
                .build();

        when(usuarioRepository.findById(id)).thenReturn(Optional.of(entity));
        when(usuarioRepository.save(entity)).thenReturn(entity);
        UsuarioResponse response = buildResponse(id, "user@prediman.com", Perfil.USUARIO, true);
        when(usuarioMapper.toResponse(entity)).thenReturn(response);

        UsuarioResponse result = userService.update(id, request);

        assertThat(result).isNotNull();
        assertThat(entity.getNome()).isEqualTo("Nome Atualizado");
        verify(usuarioRepository, never()).existsByEmail(anyString());
        verify(usuarioRepository).save(entity);
    }

    @Test
    @DisplayName("update com novo e-mail já usado por outro usuário lança BusinessException")
    void update_novoEmailDuplicado_lancaBusinessException() {
        Long id = 5L;
        Usuario entity = buildUsuario(id, "user@prediman.com", Perfil.USUARIO, true);
        UsuarioUpdateRequest request = UsuarioUpdateRequest.builder()
                .nome("Nome")
                .email("outro@prediman.com")  // e-mail diferente já cadastrado
                .perfil(Perfil.USUARIO)
                .build();

        when(usuarioRepository.findById(id)).thenReturn(Optional.of(entity));
        when(usuarioRepository.existsByEmail("outro@prediman.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.update(id, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("E-mail já cadastrado");

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("update com id inexistente lança ResourceNotFoundException")
    void update_idInexistente_lancaResourceNotFoundException() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());
        UsuarioUpdateRequest request = UsuarioUpdateRequest.builder()
                .nome("X")
                .email("x@prediman.com")
                .perfil(Perfil.USUARIO)
                .build();

        assertThatThrownBy(() -> userService.update(99L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    // ---------------------------------------------------------------------------
    // toggleStatus
    // ---------------------------------------------------------------------------

    @Test
    @DisplayName("toggleStatus desativa usuário ativo (não-admin)")
    void toggleStatus_usuarioAtivo_desativa() {
        Long id = 6L;
        Usuario entity = buildUsuario(id, "user@prediman.com", Perfil.USUARIO, true);
        Usuario saved = buildUsuario(id, "user@prediman.com", Perfil.USUARIO, false);
        UsuarioResponse response = buildResponse(id, "user@prediman.com", Perfil.USUARIO, false);

        when(usuarioRepository.findById(id)).thenReturn(Optional.of(entity));
        when(usuarioRepository.save(entity)).thenReturn(saved);
        when(usuarioMapper.toResponse(saved)).thenReturn(response);

        UsuarioResponse result = userService.toggleStatus(id);

        assertThat(entity.getAtivo()).isFalse();
        assertThat(result.getAtivo()).isFalse();
        verify(usuarioRepository).save(entity);
    }

    @Test
    @DisplayName("toggleStatus ativa usuário inativo")
    void toggleStatus_usuarioInativo_ativa() {
        Long id = 7L;
        Usuario entity = buildUsuario(id, "user@prediman.com", Perfil.USUARIO, false);
        Usuario saved = buildUsuario(id, "user@prediman.com", Perfil.USUARIO, true);
        UsuarioResponse response = buildResponse(id, "user@prediman.com", Perfil.USUARIO, true);

        when(usuarioRepository.findById(id)).thenReturn(Optional.of(entity));
        when(usuarioRepository.save(entity)).thenReturn(saved);
        when(usuarioMapper.toResponse(saved)).thenReturn(response);

        UsuarioResponse result = userService.toggleStatus(id);

        assertThat(entity.getAtivo()).isTrue();
        assertThat(result.getAtivo()).isTrue();
    }

    @Test
    @DisplayName("toggleStatus no único admin ativo lança BusinessException")
    void toggleStatus_unicoAdminAtivo_lancaBusinessException() {
        Long id = 1L;
        Usuario adminAtivo = buildUsuario(id, "admin@prediman.com", Perfil.ADMIN, true);

        when(usuarioRepository.findById(id)).thenReturn(Optional.of(adminAtivo));
        // há apenas 1 admin ativo — o próprio usuário sendo desativado
        when(usuarioRepository.countByPerfilAndAtivoTrue(Perfil.ADMIN)).thenReturn(1L);

        assertThatThrownBy(() -> userService.toggleStatus(id))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("único administrador ativo");

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("toggleStatus permite desativar admin quando há outros admins ativos")
    void toggleStatus_adminComOutrosAdmins_desativa() {
        Long id = 1L;
        Usuario admin = buildUsuario(id, "admin@prediman.com", Perfil.ADMIN, true);
        Usuario saved = buildUsuario(id, "admin@prediman.com", Perfil.ADMIN, false);
        UsuarioResponse response = buildResponse(id, "admin@prediman.com", Perfil.ADMIN, false);

        when(usuarioRepository.findById(id)).thenReturn(Optional.of(admin));
        // há 2 admins ativos — pode desativar
        when(usuarioRepository.countByPerfilAndAtivoTrue(Perfil.ADMIN)).thenReturn(2L);
        when(usuarioRepository.save(admin)).thenReturn(saved);
        when(usuarioMapper.toResponse(saved)).thenReturn(response);

        UsuarioResponse result = userService.toggleStatus(id);

        assertThat(admin.getAtivo()).isFalse();
        assertThat(result.getAtivo()).isFalse();
    }

    // ---------------------------------------------------------------------------
    // findAll (paginado)
    // ---------------------------------------------------------------------------

    @Test
    @DisplayName("findAll paginado retorna página de UsuarioResponse")
    void findAll_paginado_retornaPaginaDeRespostas() {
        Pageable pageable = PageRequest.of(0, 10);
        Usuario u1 = buildUsuario(1L, "a@prediman.com", Perfil.ADMIN, true);
        Usuario u2 = buildUsuario(2L, "b@prediman.com", Perfil.USUARIO, true);
        Page<Usuario> page = new PageImpl<>(List.of(u1, u2), pageable, 2);

        UsuarioResponse r1 = buildResponse(1L, "a@prediman.com", Perfil.ADMIN, true);
        UsuarioResponse r2 = buildResponse(2L, "b@prediman.com", Perfil.USUARIO, true);

        when(usuarioRepository.findAll(pageable)).thenReturn(page);
        when(usuarioMapper.toResponse(u1)).thenReturn(r1);
        when(usuarioMapper.toResponse(u2)).thenReturn(r2);

        Page<UsuarioResponse> result = userService.findAll(pageable);

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).containsExactly(r1, r2);
    }

    @Test
    @DisplayName("findAll lista completa retorna todos os usuários mapeados")
    void findAll_listaCompleta_retornaTodosUsuarios() {
        Usuario u1 = buildUsuario(1L, "a@prediman.com", Perfil.ADMIN, true);
        Usuario u2 = buildUsuario(2L, "b@prediman.com", Perfil.USUARIO, false);

        UsuarioResponse r1 = buildResponse(1L, "a@prediman.com", Perfil.ADMIN, true);
        UsuarioResponse r2 = buildResponse(2L, "b@prediman.com", Perfil.USUARIO, false);

        when(usuarioRepository.findAll()).thenReturn(List.of(u1, u2));
        when(usuarioMapper.toResponse(u1)).thenReturn(r1);
        when(usuarioMapper.toResponse(u2)).thenReturn(r2);

        List<UsuarioResponse> result = userService.findAll();

        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(r1, r2);
    }

    // ---------------------------------------------------------------------------
    // update — rebaixar admin lança exceção se for o último
    // ---------------------------------------------------------------------------

    @Test
    @DisplayName("update rebaixar único admin ativo para USUARIO lança BusinessException")
    void update_rebaixarUnicoAdmin_lancaBusinessException() {
        Long id = 1L;
        Usuario admin = buildUsuario(id, "admin@prediman.com", Perfil.ADMIN, true);
        UsuarioUpdateRequest request = UsuarioUpdateRequest.builder()
                .nome("Admin")
                .email("admin@prediman.com")
                .perfil(Perfil.USUARIO)  // tentativa de rebaixar o perfil
                .build();

        when(usuarioRepository.findById(id)).thenReturn(Optional.of(admin));
        when(usuarioRepository.countByPerfilAndAtivoTrue(Perfil.ADMIN)).thenReturn(1L);

        assertThatThrownBy(() -> userService.update(id, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("único administrador ativo");

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("update rebaixa admin com multiplos admins — nao lanca excecao e persiste")
    void update_rebaixarAdminComMultiplosAdmins_atualizaComSucesso() {
        Long id = 2L;
        Usuario admin = buildUsuario(id, "admin2@prediman.com", Perfil.ADMIN, true);
        UsuarioUpdateRequest request = UsuarioUpdateRequest.builder()
                .nome("Usuario Rebaixado")
                .email("admin2@prediman.com")
                .perfil(Perfil.USUARIO)  // rebaixar perfil mas ha outros admins
                .build();

        when(usuarioRepository.findById(id)).thenReturn(Optional.of(admin));
        // ha 2 admins ativos — pode rebaixar
        when(usuarioRepository.countByPerfilAndAtivoTrue(Perfil.ADMIN)).thenReturn(2L);
        when(usuarioRepository.save(admin)).thenReturn(admin);
        UsuarioResponse response = buildResponse(id, "admin2@prediman.com", Perfil.USUARIO, true);
        when(usuarioMapper.toResponse(admin)).thenReturn(response);

        UsuarioResponse result = userService.update(id, request);

        assertThat(result).isNotNull();
        verify(usuarioRepository).countByPerfilAndAtivoTrue(Perfil.ADMIN);
        verify(usuarioRepository).save(admin);
    }

    @Test
    @DisplayName("update admin que permanece admin nao verifica ultimo admin")
    void update_adminParaAdmin_naoVerificaUltimoAdmin() {
        Long id = 1L;
        Usuario admin = buildUsuario(id, "admin@prediman.com", Perfil.ADMIN, true);
        UsuarioUpdateRequest request = UsuarioUpdateRequest.builder()
                .nome("Admin Atualizado")
                .email("admin@prediman.com")
                .perfil(Perfil.ADMIN)  // mantém perfil ADMIN — não deve verificar último admin
                .build();

        when(usuarioRepository.findById(id)).thenReturn(Optional.of(admin));
        when(usuarioRepository.save(admin)).thenReturn(admin);
        UsuarioResponse response = buildResponse(id, "admin@prediman.com", Perfil.ADMIN, true);
        when(usuarioMapper.toResponse(admin)).thenReturn(response);

        UsuarioResponse result = userService.update(id, request);

        assertThat(result).isNotNull();
        verify(usuarioRepository, never()).countByPerfilAndAtivoTrue(any());
        verify(usuarioRepository).save(admin);
    }

    @Test
    @DisplayName("update com novo e-mail ainda disponível atualiza o usuário")
    void update_novoEmailDisponivel_atualizaUsuario() {
        Long id = 6L;
        Usuario entity = buildUsuario(id, "user@prediman.com", Perfil.USUARIO, true);
        UsuarioUpdateRequest request = UsuarioUpdateRequest.builder()
                .nome("Nome Novo")
                .email("novo@prediman.com")
                .perfil(Perfil.USUARIO)
                .build();

        when(usuarioRepository.findById(id)).thenReturn(Optional.of(entity));
        when(usuarioRepository.existsByEmail("novo@prediman.com")).thenReturn(false);
        when(usuarioRepository.save(entity)).thenReturn(entity);
        UsuarioResponse response = buildResponse(id, "novo@prediman.com", Perfil.USUARIO, true);
        when(usuarioMapper.toResponse(entity)).thenReturn(response);

        UsuarioResponse result = userService.update(id, request);

        assertThat(result.getEmail()).isEqualTo("novo@prediman.com");
        assertThat(entity.getEmail()).isEqualTo("novo@prediman.com");
        assertThat(entity.getNome()).isEqualTo("Nome Novo");
        verify(usuarioRepository).save(entity);
    }
}
