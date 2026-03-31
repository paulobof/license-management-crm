package com.prediman.crm.repository;

import com.prediman.crm.model.ConfiguracaoAlerta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConfiguracaoAlertaRepository extends JpaRepository<ConfiguracaoAlerta, Long> {

    /**
     * Returns the single global configuration row.
     * Equivalent to SELECT * FROM configuracao_alerta LIMIT 1.
     */
    Optional<ConfiguracaoAlerta> findFirstByOrderByIdAsc();
}
