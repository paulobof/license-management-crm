package com.prediman.crm.repository;

import com.prediman.crm.model.AlertaLog;
import com.prediman.crm.model.enums.StatusEnvio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertaLogRepository extends JpaRepository<AlertaLog, Long>, JpaSpecificationExecutor<AlertaLog> {

    long countByStatusEnvio(StatusEnvio statusEnvio);

    List<AlertaLog> findByDocumentoIdOrderByCreatedAtDesc(Long documentoId);
}
