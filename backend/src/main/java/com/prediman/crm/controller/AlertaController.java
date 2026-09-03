package com.prediman.crm.controller;

import com.prediman.crm.dto.*;
import com.prediman.crm.model.enums.StatusEnvio;
import com.prediman.crm.model.enums.TipoAlerta;
import com.prediman.crm.service.AlertaService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/alertas")
@RequiredArgsConstructor
@Validated
public class AlertaController {

    private final AlertaService alertaService;

    /**
     * GET /api/alertas/config
     * Retorna a configuração global de alertas.
     */
    @GetMapping("/config")
    public ResponseEntity<ConfiguracaoAlertaResponse> getConfig() {
        return ResponseEntity.ok(alertaService.getConfig());
    }

    /**
     * PUT /api/alertas/config
     * Atualiza a configuração global de alertas. Requer perfil ADMIN.
     */
    @PutMapping("/config")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ConfiguracaoAlertaResponse> updateConfig(
            @RequestBody ConfiguracaoAlertaRequest request) {
        return ResponseEntity.ok(alertaService.updateConfig(request));
    }

    /**
     * GET /api/alertas/pendentes
     * Lista documentos e cobranças próximos do vencimento ou já vencidos.
     */
    @GetMapping("/pendentes")
    public ResponseEntity<List<AlertaPendenteResponse>> getPendentes() {
        return ResponseEntity.ok(alertaService.getAlertasPendentes());
    }

    /**
     * GET /api/alertas/summary
     * Retorna contagens para o badge de notificações no frontend.
     */
    @GetMapping("/summary")
    public ResponseEntity<NotificacaoSummaryResponse> getSummary() {
        return ResponseEntity.ok(alertaService.getNotificacaoSummary());
    }

    /**
     * GET /api/alertas/historico
     * Histórico paginado de logs de alerta com filtros opcionais.
     */
    @GetMapping("/historico")
    public ResponseEntity<Page<AlertaLogResponse>> getHistorico(
            @RequestParam(required = false) StatusEnvio status,
            @RequestParam(required = false) TipoAlerta tipo,
            @RequestParam(required = false) Long documentoId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        size = Math.min(size, 100);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(alertaService.getLogs(pageable, status, tipo, documentoId));
    }

    /**
     * POST /api/alertas/{id}/snooze
     * Adia o alerta pelo número de dias informado (padrão 7).
     * O parâmetro {@code tipo} define se o id refere-se a um documento (padrão) ou a uma cobrança.
     */
    @PostMapping("/{id}/snooze")
    @PreAuthorize("hasAnyRole('ADMIN','USUARIO')")
    public ResponseEntity<AlertaLogResponse> snooze(
            @PathVariable Long id,
            @RequestParam(defaultValue = "7") @Min(1) @Max(90) int dias,
            @RequestParam(defaultValue = "DOCUMENTO") TipoAlerta tipo) {
        if (tipo == TipoAlerta.COBRANCA) {
            return ResponseEntity.ok(alertaService.snoozeAlertaCobranca(id, dias));
        }
        return ResponseEntity.ok(alertaService.snoozeAlerta(id, dias));
    }

    /**
     * POST /api/alertas/enviar-manual/{id}
     * Dispara manualmente um alerta, criando uma entrada de log PENDENTE.
     * O parâmetro {@code tipo} define se o id refere-se a um documento (padrão) ou a uma cobrança.
     */
    @PostMapping("/enviar-manual/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','USUARIO')")
    public ResponseEntity<AlertaLogResponse> enviarManual(
            @PathVariable Long id,
            @RequestParam(defaultValue = "DOCUMENTO") TipoAlerta tipo) {
        if (tipo == TipoAlerta.COBRANCA) {
            return ResponseEntity.ok(alertaService.enviarManualCobranca(id));
        }
        return ResponseEntity.ok(alertaService.enviarManual(id));
    }
}
