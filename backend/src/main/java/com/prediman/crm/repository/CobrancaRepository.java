package com.prediman.crm.repository;

import com.prediman.crm.model.Cobranca;
import com.prediman.crm.model.enums.StatusCobranca;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CobrancaRepository extends JpaRepository<Cobranca, Long>, JpaSpecificationExecutor<Cobranca> {

    List<Cobranca> findTop500ByContratoId(Long contratoId);

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Cobranca c " +
           "WHERE c.contrato.cliente.id = :clienteId AND c.status = :status")
    boolean existsByClienteIdAndStatus(@Param("clienteId") Long clienteId, @Param("status") StatusCobranca status);

    boolean existsByContratoIdAndDataVencimentoBetween(Long contratoId, LocalDate start, LocalDate end);

    @Query("SELECT COALESCE(SUM(c.valorEsperado), 0) FROM Cobranca c " +
           "WHERE c.status = :status AND c.dataVencimento >= :start AND c.dataVencimento <= :end")
    java.math.BigDecimal sumValorEsperadoByStatusAndVencimentoBetween(
            @Param("status") StatusCobranca status,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end);

    @Query("SELECT COALESCE(SUM(c.valorRecebido), 0) FROM Cobranca c " +
           "WHERE c.status = :status AND c.dataPagamento >= :start AND c.dataPagamento <= :end")
    java.math.BigDecimal sumValorRecebidoByStatusAndPagamentoBetween(
            @Param("status") StatusCobranca status,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end);

    @Query("SELECT COALESCE(SUM(c.valorEsperado), 0) FROM Cobranca c " +
           "WHERE c.status = :status AND c.dataVencimento < :date")
    java.math.BigDecimal sumValorEsperadoByStatusAndVencimentoBefore(
            @Param("status") StatusCobranca status,
            @Param("date") LocalDate date);

}
