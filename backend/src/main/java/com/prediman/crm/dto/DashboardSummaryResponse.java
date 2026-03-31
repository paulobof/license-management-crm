package com.prediman.crm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryResponse {

    private long totalClientes;
    private long clientesAtivos;
    private long documentosAVencer;
    private long documentosVencidos;
}
