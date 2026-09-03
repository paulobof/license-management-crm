package com.prediman.crm.model;

import com.prediman.crm.model.enums.Perfil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PasswordResetToken — cobertura de getters, setters, builder, equals e hashCode")
class PasswordResetTokenTest {

    private Usuario usuario() {
        return Usuario.builder()
                .id(1L)
                .nome("Fulano")
                .email("fulano@prediman.com")
                .senhaHash("hash")
                .perfil(Perfil.USUARIO)
                .ativo(true)
                .build();
    }

    @Test
    @DisplayName("builder preenche todos os campos")
    void builder_preencheCampos() {
        LocalDateTime agora = LocalDateTime.now();
        Usuario usuario = usuario();

        PasswordResetToken token = PasswordResetToken.builder()
                .id(7L)
                .usuario(usuario)
                .tokenHash("abc123")
                .expiresAt(agora.plusHours(2))
                .usedAt(null)
                .createdAt(agora)
                .build();

        assertThat(token.getId()).isEqualTo(7L);
        assertThat(token.getUsuario()).isSameAs(usuario);
        assertThat(token.getTokenHash()).isEqualTo("abc123");
        assertThat(token.getExpiresAt()).isEqualTo(agora.plusHours(2));
        assertThat(token.getUsedAt()).isNull();
        assertThat(token.getCreatedAt()).isEqualTo(agora);
    }

    @Test
    @DisplayName("construtor vazio e setters funcionam")
    void construtorVazioESetters() {
        LocalDateTime agora = LocalDateTime.now();
        PasswordResetToken token = new PasswordResetToken();

        token.setId(3L);
        token.setUsuario(usuario());
        token.setTokenHash("hash-do-token");
        token.setExpiresAt(agora.plusHours(1));
        token.setUsedAt(agora);
        token.setCreatedAt(agora.minusHours(1));

        assertThat(token.getId()).isEqualTo(3L);
        assertThat(token.getUsuario()).isNotNull();
        assertThat(token.getTokenHash()).isEqualTo("hash-do-token");
        assertThat(token.getExpiresAt()).isEqualTo(agora.plusHours(1));
        assertThat(token.getUsedAt()).isEqualTo(agora);
        assertThat(token.getCreatedAt()).isEqualTo(agora.minusHours(1));
    }

    @Test
    @DisplayName("construtor completo preenche todos os campos")
    void construtorCompleto() {
        LocalDateTime agora = LocalDateTime.now();
        PasswordResetToken token = new PasswordResetToken(
                9L, usuario(), "hash", agora.plusHours(2), null, agora);

        assertThat(token.getId()).isEqualTo(9L);
        assertThat(token.getTokenHash()).isEqualTo("hash");
    }

    @Test
    @DisplayName("equals e hashCode consideram apenas o id")
    void equalsEHashCode_apenasId() {
        PasswordResetToken a = PasswordResetToken.builder().id(1L).tokenHash("x").build();
        PasswordResetToken b = PasswordResetToken.builder().id(1L).tokenHash("y").build();
        PasswordResetToken c = PasswordResetToken.builder().id(2L).tokenHash("x").build();

        assertThat(a).isEqualTo(b);
        assertThat(a).hasSameHashCodeAs(b);
        assertThat(a).isNotEqualTo(c);
        assertThat(a).isNotEqualTo(null);
        assertThat(a).isEqualTo(a);
        assertThat(a).isNotEqualTo("outra coisa");
    }

    @Test
    @DisplayName("toString não expõe o hash do token nem o usuário")
    void toString_naoExpoeDadosSensiveis() {
        PasswordResetToken token = PasswordResetToken.builder()
                .id(1L)
                .usuario(usuario())
                .tokenHash("segredo-hash")
                .build();

        assertThat(token.toString()).doesNotContain("segredo-hash");
        assertThat(token.toString()).doesNotContain("fulano@prediman.com");
    }

    @Test
    @DisplayName("onCreate define createdAt quando ausente e preserva quando presente")
    void onCreate_defineCreatedAt() {
        PasswordResetToken semData = PasswordResetToken.builder().build();
        semData.onCreate();
        assertThat(semData.getCreatedAt()).isNotNull();

        LocalDateTime original = LocalDateTime.now().minusDays(1);
        PasswordResetToken comData = PasswordResetToken.builder().createdAt(original).build();
        comData.onCreate();
        assertThat(comData.getCreatedAt()).isEqualTo(original);
    }

    @Test
    @DisplayName("isValido é verdadeiro apenas para token não usado e dentro da validade")
    void isValido_regras() {
        LocalDateTime agora = LocalDateTime.now();

        assertThat(PasswordResetToken.builder().expiresAt(agora.plusHours(1)).build().isValido(agora)).isTrue();
        assertThat(PasswordResetToken.builder().expiresAt(agora.plusHours(1)).usedAt(agora).build()
                .isValido(agora)).isFalse();
        assertThat(PasswordResetToken.builder().expiresAt(agora.minusSeconds(1)).build().isValido(agora)).isFalse();
        assertThat(PasswordResetToken.builder().build().isValido(agora)).isFalse();
    }
}
