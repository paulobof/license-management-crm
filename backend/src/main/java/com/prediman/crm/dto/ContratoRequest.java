package com.prediman.crm.dto;

import com.prediman.crm.model.enums.Periodicidade;
import com.prediman.crm.model.enums.StatusContrato;
import jakarta.validation.constraints.NotBlank;
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
public class ContratoRequest {

    @NotBlank(message = "Descrição é obrigatória")
    private String descricao;

    @NotNull(message = "Valor é obrigatório")
    private BigDecimal valor;

    private Periodicidade periodicidade;

    @NotNull(message = "Data de início é obrigatória")
    private LocalDate dataInicio;

    private LocalDate dataFim;

    private StatusContrato status;

    private String observacoes;

    @NotNull(message = "Cliente é obrigatório")
    private Long clienteId;
}
