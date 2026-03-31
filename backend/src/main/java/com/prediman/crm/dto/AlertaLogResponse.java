package com.prediman.crm.dto;

import com.prediman.crm.model.enums.CanalAlerta;
import com.prediman.crm.model.enums.StatusEnvio;
import com.prediman.crm.model.enums.TipoAlerta;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertaLogResponse {

    private Long id;
    private Long documentoId;
    private String documentoNome;
    private String clienteNome;
    private Long cobrancaId;
    private TipoAlerta tipo;
    private CanalAlerta canal;
    private String destinatario;
    private String mensagem;
    private StatusEnvio statusEnvio;
    private LocalDateTime dataEnvio;
    private LocalDate snoozedAte;
    private LocalDateTime createdAt;
}
