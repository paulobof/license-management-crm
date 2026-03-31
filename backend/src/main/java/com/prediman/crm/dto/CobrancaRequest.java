package com.prediman.crm.dto;

import com.prediman.crm.model.enums.StatusCobranca;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CobrancaRequest {

    @NotNull(message = "Valor esperado é obrigatório")
    private BigDecimal valorEsperado;

    @NotNull(message = "Data de vencimento é obrigatória")
    private LocalDate dataVencimento;

    private BigDecimal valorRecebido;

    private LocalDate dataPagamento;

    private String formaPagamento;

    private String comprovanteDriveId;

    private StatusCobranca status;

    @NotNull(message = "Contrato é obrigatório")
    private Long contratoId;
}
