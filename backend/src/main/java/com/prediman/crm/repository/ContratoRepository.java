package com.prediman.crm.repository;

import com.prediman.crm.model.Contrato;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.prediman.crm.model.enums.StatusContrato;

import java.util.List;

@Repository
public interface ContratoRepository extends JpaRepository<Contrato, Long>, JpaSpecificationExecutor<Contrato> {

    List<Contrato> findTop500ByClienteId(Long clienteId);

    boolean existsByClienteIdAndStatus(Long clienteId, StatusContrato status);

    @Override
    @EntityGraph(attributePaths = {"cliente", "cobrancas"})
    Page<Contrato> findAll(Specification<Contrato> spec, Pageable pageable);
}
