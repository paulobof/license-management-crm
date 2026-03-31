package com.prediman.crm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfiguracaoAlertaResponse {

    private Long id;
    private String diasAntecedencia;
    private LocalTime horarioExecucao;
    private Boolean emailAtivo;
    private Boolean whatsappAtivo;
    private String templateEmail;
    private String templateWhatsapp;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
