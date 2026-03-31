package com.prediman.crm.service;

import com.prediman.crm.dto.*;
import com.prediman.crm.exception.ResourceNotFoundException;
import com.prediman.crm.model.AlertaLog;
import com.prediman.crm.model.ConfiguracaoAlerta;
import com.prediman.crm.model.Documento;
import com.prediman.crm.model.enums.CanalAlerta;
import com.prediman.crm.model.enums.StatusEnvio;
import com.prediman.crm.model.enums.TipoAlerta;
import com.prediman.crm.repository.AlertaLogRepository;
import com.prediman.crm.repository.ConfiguracaoAlertaRepository;
import com.prediman.crm.repository.DocumentoRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlertaService {

    private final ConfiguracaoAlertaRepository configuracaoAlertaRepository;
    private final AlertaLogRepository alertaLogRepository;
    private final DocumentoRepository documentoRepository;

    // -------------------------------------------------------------------------
    // Configuration
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public ConfiguracaoAlertaResponse getConfig() {
        ConfiguracaoAlerta config = loadConfig();
        return toConfigResponse(config);
    }

    @Transactional
    public ConfiguracaoAlertaResponse updateConfig(ConfiguracaoAlertaRequest request) {
        ConfiguracaoAlerta config = loadConfig();

        if (request.getDiasAntecedencia() != null) {
            config.setDiasAntecedencia(request.getDiasAntecedencia());
        }
        if (request.getHorarioExecucao() != null) {
            config.setHorarioExecucao(LocalTime.parse(request.getHorarioExecucao()));
        }
        if (request.getEmailAtivo() != null) {
            config.setEmailAtivo(request.getEmailAtivo());
        }
        if (request.getWhatsappAtivo() != null) {
            config.setWhatsappAtivo(request.getWhatsappAtivo());
        }
        if (request.getTemplateEmail() != null) {
            config.setTemplateEmail(request.getTemplateEmail());
        }
        if (request.getTemplateWhatsapp() != null) {
            config.setTemplateWhatsapp(request.getTemplateWhatsapp());
        }

        ConfiguracaoAlerta saved = configuracaoAlertaRepository.save(config);
        log.info("Configuração de alerta atualizada: id={}", saved.getId());
        return toConfigResponse(saved);
    }

    // -------------------------------------------------------------------------
    // Pending alerts
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<AlertaPendenteResponse> getAlertasPendentes() {
        ConfiguracaoAlerta config = loadConfig();
        List<Integer> dias = config.getDiasAntecedenciaList();
        int maxDias = dias.stream().max(Integer::compareTo).orElse(30);

        LocalDate today = LocalDate.now();
        LocalDate limite = today.plusDays(maxDias);

        // Fetch documents with dataValidade within the configured window (a_vencer) or already vencidos
        List<Documento> documentos = documentoRepository.findAll(
                buildDocumentosVencendoSpec(today, limite)
        );

        return documentos.stream()
                .map(d -> toAlertaPendenteResponse(d, today))
                .sorted(Comparator.comparing(AlertaPendenteResponse::getDataVencimento,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());
    }

    // -------------------------------------------------------------------------
    // Summary for notification badge
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public NotificacaoSummaryResponse getNotificacaoSummary() {
        LocalDate today = LocalDate.now();
        ConfiguracaoAlerta config = loadConfig();
        int maxDias = config.getDiasAntecedenciaList().stream()
                .max(Integer::compareTo).orElse(30);

        long documentosAVencer = documentoRepository.countAVencer(today, today.plusDays(maxDias));
        long documentosVencidos = documentoRepository.countVencidos(today);
        // cobrancasVencidas will be populated once Cobranca entity is available
        long cobrancasVencidas = 0L;
        long totalPendentes = documentosAVencer + documentosVencidos + cobrancasVencidas;

        return NotificacaoSummaryResponse.builder()
                .totalPendentes(totalPendentes)
                .documentosAVencer(documentosAVencer)
                .documentosVencidos(documentosVencidos)
                .cobrancasVencidas(cobrancasVencidas)
                .build();
    }

    // -------------------------------------------------------------------------
    // Snooze
    // -------------------------------------------------------------------------

    @Transactional
    public AlertaLogResponse snoozeAlerta(Long documentoId, int dias) {
        Documento documento = documentoRepository.findById(documentoId)
                .orElseThrow(() -> new ResourceNotFoundException("Documento", documentoId));

        LocalDate snoozedAte = LocalDate.now().plusDays(dias);

        AlertaLog log = AlertaLog.builder()
                .documento(documento)
                .tipo(TipoAlerta.DOCUMENTO)
                .canal(CanalAlerta.EMAIL)
                .statusEnvio(StatusEnvio.SNOOZED)
                .snoozedAte(snoozedAte)
                .mensagem("Alerta adiado por " + dias + " dia(s). Próximo alerta em: " + snoozedAte)
                .build();

        AlertaLog saved = alertaLogRepository.save(log);
        this.log.info("Alerta snoozed para documento id={} até {}", documentoId, snoozedAte);
        return toAlertaLogResponse(saved);
    }

    // -------------------------------------------------------------------------
    // History
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<AlertaLogResponse> getHistorico(Long documentoId) {
        documentoRepository.findById(documentoId)
                .orElseThrow(() -> new ResourceNotFoundException("Documento", documentoId));
        return alertaLogRepository.findByDocumentoIdOrderByCreatedAtDesc(documentoId)
                .stream()
                .map(this::toAlertaLogResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<AlertaLogResponse> getLogs(Pageable pageable, StatusEnvio statusEnvio,
                                           TipoAlerta tipo, Long documentoId) {
        Specification<AlertaLog> spec = buildLogsSpec(statusEnvio, tipo, documentoId);
        return alertaLogRepository.findAll(spec, pageable).map(this::toAlertaLogResponse);
    }

    // -------------------------------------------------------------------------
    // Manual trigger
    // -------------------------------------------------------------------------

    @Transactional
    public AlertaLogResponse enviarManual(Long documentoId) {
        Documento documento = documentoRepository.findById(documentoId)
                .orElseThrow(() -> new ResourceNotFoundException("Documento", documentoId));

        ConfiguracaoAlerta config = loadConfig();

        String mensagem = buildMensagem(config.getTemplateEmail(), documento);
        String destinatario = resolveDestinatario(documento);

        AlertaLog alertaLog = AlertaLog.builder()
                .documento(documento)
                .tipo(TipoAlerta.DOCUMENTO)
                .canal(CanalAlerta.EMAIL)
                .destinatario(destinatario)
                .mensagem(mensagem)
                .statusEnvio(StatusEnvio.PENDENTE)
                .build();

        AlertaLog saved = alertaLogRepository.save(alertaLog);
        log.info("Alerta manual criado para documento id={}", documentoId);
        return toAlertaLogResponse(saved);
    }

    // -------------------------------------------------------------------------
    // Scheduler entry point — called by AlertaScheduler
    // -------------------------------------------------------------------------

    @Transactional
    public void processarAlertasDiarios() {
        ConfiguracaoAlerta config = loadConfig();
        List<Integer> diasAntecedencia = config.getDiasAntecedenciaList();
        int maxDias = diasAntecedencia.stream().max(Integer::compareTo).orElse(30);

        LocalDate today = LocalDate.now();
        LocalDate limite = today.plusDays(maxDias);

        List<Documento> documentos = documentoRepository.findAll(
                buildDocumentosVencendoSpec(today, limite)
        );

        int criados = 0;
        for (Documento documento : documentos) {
            if (documento.getDataValidade() == null) {
                continue;
            }
            long diasRestantes = ChronoUnit.DAYS.between(today, documento.getDataValidade());

            boolean deveCriarAlerta = diasAntecedencia.stream()
                    .anyMatch(d -> d == diasRestantes);

            if (deveCriarAlerta) {
                String mensagem = buildMensagem(config.getTemplateEmail(), documento);
                String destinatario = resolveDestinatario(documento);

                AlertaLog alertaLog = AlertaLog.builder()
                        .documento(documento)
                        .tipo(TipoAlerta.DOCUMENTO)
                        .canal(CanalAlerta.EMAIL)
                        .destinatario(destinatario)
                        .mensagem(mensagem)
                        .statusEnvio(StatusEnvio.PENDENTE)
                        .build();

                alertaLogRepository.save(alertaLog);
                criados++;
                log.info("AlertaLog PENDENTE criado: documentoId={}, diasRestantes={}", documento.getId(), diasRestantes);
            }
        }
        log.info("Processamento diário de alertas concluído: {} alerta(s) criado(s)", criados);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private ConfiguracaoAlerta loadConfig() {
        return configuracaoAlertaRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Configuração de alerta não encontrada. Verifique a migração V4."));
    }

    private Specification<Documento> buildDocumentosVencendoSpec(LocalDate today, LocalDate limite) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isNotNull(root.get("dataValidade")));
            predicates.add(cb.lessThanOrEqualTo(root.get("dataValidade"), limite));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Specification<AlertaLog> buildLogsSpec(StatusEnvio statusEnvio,
                                                    TipoAlerta tipo,
                                                    Long documentoId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (statusEnvio != null) {
                predicates.add(cb.equal(root.get("statusEnvio"), statusEnvio));
            }
            if (tipo != null) {
                predicates.add(cb.equal(root.get("tipo"), tipo));
            }
            if (documentoId != null) {
                predicates.add(cb.equal(root.get("documento").get("id"), documentoId));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private String buildMensagem(String template, Documento documento) {
        if (template == null) {
            return "";
        }
        LocalDate today = LocalDate.now();
        long diasRestantes = documento.getDataValidade() != null
                ? ChronoUnit.DAYS.between(today, documento.getDataValidade())
                : 0;
        String clienteNome = documento.getCliente() != null
                ? documento.getCliente().getRazaoSocial()
                : "";
        return template
                .replace("{{documento}}", documento.getNome())
                .replace("{{cliente}}", clienteNome)
                .replace("{{dias}}", String.valueOf(diasRestantes));
    }

    private String resolveDestinatario(Documento documento) {
        if (documento.getCliente() == null || documento.getCliente().getContatos() == null) {
            return null;
        }
        return documento.getCliente().getContatos().stream()
                .filter(c -> c.getEmail() != null && !c.getEmail().isBlank())
                .map(c -> c.getEmail())
                .findFirst()
                .orElse(null);
    }

    private AlertaPendenteResponse toAlertaPendenteResponse(Documento documento, LocalDate today) {
        LocalDate validade = documento.getDataValidade();
        long diasRestantes = validade != null ? ChronoUnit.DAYS.between(today, validade) : 0;
        String status = (validade != null && validade.isBefore(today)) ? "VENCIDO" : "A_VENCER";

        return AlertaPendenteResponse.builder()
                .id(documento.getId())
                .tipo(TipoAlerta.DOCUMENTO)
                .nome(documento.getNome())
                .clienteNome(documento.getCliente() != null
                        ? documento.getCliente().getRazaoSocial()
                        : null)
                .dataVencimento(validade)
                .diasRestantes((int) diasRestantes)
                .status(status)
                .build();
    }

    private AlertaLogResponse toAlertaLogResponse(AlertaLog alertaLog) {
        String documentoNome = null;
        String clienteNome = null;
        Long documentoId = null;

        if (alertaLog.getDocumento() != null) {
            documentoId = alertaLog.getDocumento().getId();
            documentoNome = alertaLog.getDocumento().getNome();
            if (alertaLog.getDocumento().getCliente() != null) {
                clienteNome = alertaLog.getDocumento().getCliente().getRazaoSocial();
            }
        }

        return AlertaLogResponse.builder()
                .id(alertaLog.getId())
                .documentoId(documentoId)
                .documentoNome(documentoNome)
                .clienteNome(clienteNome)
                .cobrancaId(alertaLog.getCobrancaId())
                .tipo(alertaLog.getTipo())
                .canal(alertaLog.getCanal())
                .destinatario(alertaLog.getDestinatario())
                .mensagem(alertaLog.getMensagem())
                .statusEnvio(alertaLog.getStatusEnvio())
                .dataEnvio(alertaLog.getDataEnvio())
                .snoozedAte(alertaLog.getSnoozedAte())
                .createdAt(alertaLog.getCreatedAt())
                .build();
    }

    private ConfiguracaoAlertaResponse toConfigResponse(ConfiguracaoAlerta config) {
        return ConfiguracaoAlertaResponse.builder()
                .id(config.getId())
                .diasAntecedencia(config.getDiasAntecedencia())
                .horarioExecucao(config.getHorarioExecucao())
                .emailAtivo(config.getEmailAtivo())
                .whatsappAtivo(config.getWhatsappAtivo())
                .templateEmail(config.getTemplateEmail())
                .templateWhatsapp(config.getTemplateWhatsapp())
                .createdAt(config.getCreatedAt())
                .updatedAt(config.getUpdatedAt())
                .build();
    }
}
