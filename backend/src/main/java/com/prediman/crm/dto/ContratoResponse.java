package com.prediman.crm.dto;

import com.prediman.crm.model.enums.Periodicidade;
import com.prediman.crm.model.enums.StatusContrato;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContratoResponse {

    private Long id;
    private String descricao;
    private BigDecimal valor;
    private Periodicidade periodicidade;
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private StatusContrato status;
    private String observacoes;
    private Long clienteId;
    private String clienteNome;
    private List<CobrancaResponse> cobrancas;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
