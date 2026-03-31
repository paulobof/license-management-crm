package com.prediman.crm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificacaoSummaryResponse {

    private long totalPendentes;
    private long documentosAVencer;
    private long documentosVencidos;
    private long cobrancasVencidas;
}
