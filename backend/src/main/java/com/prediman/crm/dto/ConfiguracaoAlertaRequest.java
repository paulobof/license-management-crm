package com.prediman.crm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfiguracaoAlertaRequest {

    /** Comma-separated days ahead, e.g. "30,15,7,1". */
    private String diasAntecedencia;

    /** Time string in HH:mm format, e.g. "08:00". */
    private String horarioExecucao;

    private Boolean emailAtivo;

    private Boolean whatsappAtivo;

    private String templateEmail;

    private String templateWhatsapp;
}
