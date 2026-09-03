package com.prediman.crm.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Token de recuperação de senha.
 *
 * <p>O valor em claro do token nunca é persistido: a coluna {@code token_hash} guarda
 * o SHA-256 do token opaco gerado com {@link java.security.SecureRandom}. O valor puro
 * é enviado apenas no e-mail de redefinição.</p>
 */
@Entity
@Table(name = "password_reset_tokens")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"usuario", "tokenHash"})
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    /**
     * @return true se o token ainda não foi utilizado e não expirou na data informada.
     */
    public boolean isValido(LocalDateTime referencia) {
        return usedAt == null && expiresAt != null && expiresAt.isAfter(referencia);
    }
}
