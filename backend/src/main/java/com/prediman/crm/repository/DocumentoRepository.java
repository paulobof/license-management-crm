package com.prediman.crm.repository;

import com.prediman.crm.model.Documento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DocumentoRepository extends JpaRepository<Documento, Long>, JpaSpecificationExecutor<Documento> {

    List<Documento> findTop500ByClienteIdOrderByDataValidadeAsc(Long clienteId);

    @Query("SELECT COUNT(d) FROM Documento d WHERE d.dataValidade IS NOT NULL AND d.dataValidade >= :start AND d.dataValidade <= :end")
    long countAVencer(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT COUNT(d) FROM Documento d WHERE d.dataValidade IS NOT NULL AND d.dataValidade < :date")
    long countVencidos(@Param("date") LocalDate date);

    @Override
    @EntityGraph(attributePaths = {"cliente"})
    Page<Documento> findAll(Specification<Documento> spec, Pageable pageable);
}
