package com.prediman.crm.dto;

import com.prediman.crm.model.enums.StatusCobranca;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CobrancaResponse {

    private Long id;
    private BigDecimal valorEsperado;
    private BigDecimal valorRecebido;
    private LocalDate dataVencimento;
    private LocalDate dataPagamento;
    private String formaPagamento;
    private String comprovanteDriveId;
    private StatusCobranca status;
    private StatusCobranca statusCalculado;
    private Long contratoId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
