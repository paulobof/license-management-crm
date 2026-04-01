package com.prediman.crm.model;

import com.prediman.crm.model.enums.Perfil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Usuario — cobertura de getters, setters, builder, equals e hashCode")
class UsuarioTest {

    @Test
    @DisplayName("builder cria instancia com todos os campos preenchidos")
    void builder_criaInstanciaCompleta() {
        LocalDateTime now = LocalDateTime.now();

        Usuario usuario = Usuario.builder()
                .id(1L)
                .nome("Maria Silva")
                .email("maria@prediman.com.br")
                .senhaHash("$2a$10$hash")
                .perfil(Perfil.ADMIN)
                .ativo(true)
                .createdAt(now)
                .ultimoLogin(now)
                .build();

        assertThat(usuario.getId()).isEqualTo(1L);
        assertThat(usuario.getNome()).isEqualTo("Maria Silva");
        assertThat(usuario.getEmail()).isEqualTo("maria@prediman.com.br");
        assertThat(usuario.getSenhaHash()).isEqualTo("$2a$10$hash");
        assertThat(usuario.getPerfil()).isEqualTo(Perfil.ADMIN);
        assertThat(usuario.getAtivo()).isTrue();
        assertThat(usuario.getCreatedAt()).isEqualTo(now);
        assertThat(usuario.getUltimoLogin()).isEqualTo(now);
    }

    @Test
    @DisplayName("builder usa ativo=true como default quando nao informado")
    void builder_usaDefaultAtivo() {
        Usuario usuario = Usuario.builder()
                .nome("Teste")
                .email("teste@email.com")
                .senhaHash("hash")
                .build();

        assertThat(usuario.getAtivo()).isTrue();
    }

    @Test
    @DisplayName("setters alteram todos os campos corretamente")
    void setters_alteramValores() {
        Usuario usuario = new Usuario();
        LocalDateTime now = LocalDateTime.now();

        usuario.setId(2L);
        usuario.setNome("Carlos");
        usuario.setEmail("carlos@prediman.com.br");
        usuario.setSenhaHash("$2a$10$hashedPw");
        usuario.setPerfil(Perfil.USUARIO);
        usuario.setAtivo(false);
        usuario.setCreatedAt(now);
        usuario.setUltimoLogin(now);

        assertThat(usuario.getId()).isEqualTo(2L);
        assertThat(usuario.getNome()).isEqualTo("Carlos");
        assertThat(usuario.getEmail()).isEqualTo("carlos@prediman.com.br");
        assertThat(usuario.getSenhaHash()).isEqualTo("$2a$10$hashedPw");
        assertThat(usuario.getPerfil()).isEqualTo(Perfil.USUARIO);
        assertThat(usuario.getAtivo()).isFalse();
        assertThat(usuario.getCreatedAt()).isEqualTo(now);
        assertThat(usuario.getUltimoLogin()).isEqualTo(now);
    }

    @Test
    @DisplayName("equals retorna true para usuarios com mesmo id")
    void equals_mesmosIds_retornaTrue() {
        Usuario u1 = Usuario.builder().id(1L).nome("A").email("a@a.com").senhaHash("h").build();
        Usuario u2 = Usuario.builder().id(1L).nome("B").email("b@b.com").senhaHash("h").build();

        assertThat(u1).isEqualTo(u2);
    }

    @Test
    @DisplayName("equals retorna false para usuarios com ids diferentes")
    void equals_idsDistintos_retornaFalse() {
        Usuario u1 = Usuario.builder().id(1L).email("a@a.com").senhaHash("h").build();
        Usuario u2 = Usuario.builder().id(2L).email("a@a.com").senhaHash("h").build();

        assertThat(u1).isNotEqualTo(u2);
    }

    @Test
    @DisplayName("hashCode e consistente para usuarios com mesmo id")
    void hashCode_consistente() {
        Usuario u1 = Usuario.builder().id(5L).email("a@a.com").senhaHash("h").build();
        Usuario u2 = Usuario.builder().id(5L).email("b@b.com").senhaHash("h").build();

        assertThat(u1.hashCode()).isEqualTo(u2.hashCode());
    }

    @Test
    @DisplayName("noArgsConstructor cria instancia vazia")
    void noArgsConstructor_criaInstanciaVazia() {
        Usuario usuario = new Usuario();

        assertThat(usuario.getId()).isNull();
        assertThat(usuario.getNome()).isNull();
    }

    @Test
    @DisplayName("allArgsConstructor preenche todos os campos")
    void allArgsConstructor_preencheAllFields() {
        LocalDateTime now = LocalDateTime.now();
        Usuario usuario = new Usuario(1L, "Nome", "email@email.com", "hash",
                Perfil.ADMIN, true, now, now);

        assertThat(usuario.getId()).isEqualTo(1L);
        assertThat(usuario.getNome()).isEqualTo("Nome");
        assertThat(usuario.getPerfil()).isEqualTo(Perfil.ADMIN);
    }

    @Test
    @DisplayName("onCreate define createdAt e perfil default quando nulos")
    void onCreate_defineCreatedAtEPerfilDefault() throws Exception {
        Usuario usuario = new Usuario();
        assertThat(usuario.getCreatedAt()).isNull();
        assertThat(usuario.getPerfil()).isNull();

        var method = Usuario.class.getDeclaredMethod("onCreate");
        method.setAccessible(true);
        method.invoke(usuario);

        assertThat(usuario.getCreatedAt()).isNotNull();
        assertThat(usuario.getPerfil()).isEqualTo(Perfil.USUARIO);
    }

    @Test
    @DisplayName("onCreate nao sobrescreve createdAt e perfil quando ja definidos")
    void onCreate_naoSobreescreveValoresExistentes() throws Exception {
        LocalDateTime original = LocalDateTime.of(2022, 6, 15, 9, 0);
        Usuario usuario = new Usuario();
        usuario.setCreatedAt(original);
        usuario.setPerfil(Perfil.ADMIN);

        var method = Usuario.class.getDeclaredMethod("onCreate");
        method.setAccessible(true);
        method.invoke(usuario);

        assertThat(usuario.getCreatedAt()).isEqualTo(original);
        assertThat(usuario.getPerfil()).isEqualTo(Perfil.ADMIN);
    }
}
