package com.prediman.crm.repository;

import com.prediman.crm.model.AlertaLog;
import com.prediman.crm.model.enums.StatusEnvio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AlertaLogRepository extends JpaRepository<AlertaLog, Long>, JpaSpecificationExecutor<AlertaLog> {

    long countByStatusEnvio(StatusEnvio statusEnvio);

    List<AlertaLog> findByDocumentoIdOrderByCreatedAtDesc(Long documentoId);

    /**
     * Verifica se já existe um AlertaLog para o documento na data informada (idempotência).
     */
    @Query("SELECT COUNT(a) > 0 FROM AlertaLog a WHERE a.documento.id = :documentoId " +
           "AND a.createdAt >= :startOfDay AND a.createdAt < :startOfNextDay")
    boolean existsByDocumentoIdAndCreatedAtDate(
            @Param("documentoId") Long documentoId,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("startOfNextDay") LocalDateTime startOfNextDay);

    /**
     * Retorna os IDs de documentos que possuem snooze ativo (snoozedAte >= hoje).
     */
    @Query("SELECT DISTINCT a.documento.id FROM AlertaLog a " +
           "WHERE a.statusEnvio = :status AND a.snoozedAte >= :today")
    List<Long> findSnoozedDocumentoIds(
            @Param("status") StatusEnvio status,
            @Param("today") LocalDate today);
}
