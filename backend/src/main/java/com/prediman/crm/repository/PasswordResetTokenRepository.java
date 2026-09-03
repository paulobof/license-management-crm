package com.prediman.crm.repository;

import com.prediman.crm.model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    /**
     * Invalida (marca como usados) todos os tokens pendentes do usuário informado.
     * Garante que apenas o token mais recente permaneça válido.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE PasswordResetToken t SET t.usedAt = :agora " +
           "WHERE t.usuario.id = :usuarioId AND t.usedAt IS NULL")
    int invalidarTokensPendentes(@Param("usuarioId") Long usuarioId,
                                 @Param("agora") LocalDateTime agora);
}
