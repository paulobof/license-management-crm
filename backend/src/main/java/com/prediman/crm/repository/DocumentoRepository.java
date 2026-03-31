package com.prediman.crm.repository;

import com.prediman.crm.model.Documento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DocumentoRepository extends JpaRepository<Documento, Long>, JpaSpecificationExecutor<Documento> {

    List<Documento> findByClienteIdOrderByDataValidadeAsc(Long clienteId);

    @Query("SELECT COUNT(d) FROM Documento d WHERE d.dataValidade IS NOT NULL AND d.dataValidade >= :start AND d.dataValidade <= :end")
    long countAVencer(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT COUNT(d) FROM Documento d WHERE d.dataValidade IS NOT NULL AND d.dataValidade < :date")
    long countVencidos(@Param("date") LocalDate date);
}
