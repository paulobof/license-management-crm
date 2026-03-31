package com.prediman.crm.dto;

import com.prediman.crm.model.enums.TipoAlerta;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertaPendenteResponse {

    private Long id;
    private TipoAlerta tipo;
    private String nome;
    private String clienteNome;
    private LocalDate dataVencimento;
    private int diasRestantes;

    /**
     * A_VENCER when dataVencimento >= today, VENCIDO when dataVencimento < today.
     */
    private String status;
}
