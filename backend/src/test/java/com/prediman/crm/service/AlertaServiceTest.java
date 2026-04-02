package com.prediman.crm.service;

import com.prediman.crm.dto.AlertaLogResponse;
import com.prediman.crm.dto.AlertaPendenteResponse;
import com.prediman.crm.dto.ConfiguracaoAlertaRequest;
import com.prediman.crm.dto.ConfiguracaoAlertaResponse;
import com.prediman.crm.dto.NotificacaoSummaryResponse;
import com.prediman.crm.exception.ResourceNotFoundException;
import com.prediman.crm.model.AlertaLog;
import com.prediman.crm.model.Cliente;
import com.prediman.crm.model.ConfiguracaoAlerta;
import com.prediman.crm.model.Contato;
import com.prediman.crm.model.Documento;
import com.prediman.crm.model.enums.CanalAlerta;
import com.prediman.crm.model.enums.StatusEnvio;
import com.prediman.crm.model.enums.TipoAlerta;
import com.prediman.crm.repository.AlertaLogRepository;
import com.prediman.crm.repository.ConfiguracaoAlertaRepository;
import com.prediman.crm.repository.DocumentoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlertaServiceTest {

    @Mock
    private ConfiguracaoAlertaRepository configuracaoAlertaRepository;

    @Mock
    private AlertaLogRepository alertaLogRepository;

    @Mock
    private DocumentoRepository documentoRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private WhatsAppService whatsAppService;

    @InjectMocks
    private AlertaService alertaService;

    private ConfiguracaoAlerta defaultConfig;

    @BeforeEach
    void setUp() {
        defaultConfig = ConfiguracaoAlerta.builder()
                .id(1L)
                .diasAntecedencia("30,15,7,1")
                .horarioExecucao(LocalTime.of(8, 0))
                .emailAtivo(true)
                .whatsappAtivo(false)
                .templateEmail("Documento {{documento}} do cliente {{cliente}} vence em {{dias}} dias.")
                .templateWhatsapp(null)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // -------------------------------------------------------------------------
    // getConfig
    // -------------------------------------------------------------------------

    @Test
    void getConfig_returnsConfiguration() {
        when(configuracaoAlertaRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(defaultConfig));

        ConfiguracaoAlertaResponse response = alertaService.getConfig();

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getDiasAntecedencia()).isEqualTo("30,15,7,1");
        assertThat(response.getHorarioExecucao()).isEqualTo(LocalTime.of(8, 0));
        assertThat(response.getEmailAtivo()).isTrue();
        assertThat(response.getWhatsappAtivo()).isFalse();
    }

    @Test
    void getConfig_throwsWhenConfigNotFound() {
        when(configuracaoAlertaRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> alertaService.getConfig())
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // -------------------------------------------------------------------------
    // updateConfig
    // -------------------------------------------------------------------------

    @Test
    void updateConfig_updatesAllFields() {
        when(configuracaoAlertaRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(defaultConfig));
        when(configuracaoAlertaRepository.save(any(ConfiguracaoAlerta.class))).thenReturn(defaultConfig);

        ConfiguracaoAlertaRequest request = ConfiguracaoAlertaRequest.builder()
                .diasAntecedencia("60,30,7")
                .horarioExecucao("09:30")
                .emailAtivo(false)
                .whatsappAtivo(true)
                .templateEmail("Novo template email")
                .templateWhatsapp("Novo template whatsapp")
                .build();

        alertaService.updateConfig(request);

        ArgumentCaptor<ConfiguracaoAlerta> captor = ArgumentCaptor.forClass(ConfiguracaoAlerta.class);
        verify(configuracaoAlertaRepository).save(captor.capture());

        ConfiguracaoAlerta saved = captor.getValue();
        assertThat(saved.getDiasAntecedencia()).isEqualTo("60,30,7");
        assertThat(saved.getHorarioExecucao()).isEqualTo(LocalTime.of(9, 30));
        assertThat(saved.getEmailAtivo()).isFalse();
        assertThat(saved.getWhatsappAtivo()).isTrue();
        assertThat(saved.getTemplateEmail()).isEqualTo("Novo template email");
        assertThat(saved.getTemplateWhatsapp()).isEqualTo("Novo template whatsapp");
    }

    @Test
    void updateConfig_ignoresNullFields() {
        when(configuracaoAlertaRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(defaultConfig));
        when(configuracaoAlertaRepository.save(any(ConfiguracaoAlerta.class))).thenReturn(defaultConfig);

        // Only update emailAtivo; everything else is null
        ConfiguracaoAlertaRequest request = ConfiguracaoAlertaRequest.builder()
                .emailAtivo(false)
                .build();

        alertaService.updateConfig(request);

        ArgumentCaptor<ConfiguracaoAlerta> captor = ArgumentCaptor.forClass(ConfiguracaoAlerta.class);
        verify(configuracaoAlertaRepository).save(captor.capture());

        ConfiguracaoAlerta saved = captor.getValue();
        // Only the explicitly set field should change
        assertThat(saved.getEmailAtivo()).isFalse();
        // Original values preserved
        assertThat(saved.getDiasAntecedencia()).isEqualTo("30,15,7,1");
        assertThat(saved.getHorarioExecucao()).isEqualTo(LocalTime.of(8, 0));
    }

    // -------------------------------------------------------------------------
    // getAlertasPendentes
    // -------------------------------------------------------------------------

    @Test
    void getAlertasPendentes_returnsPendingAlerts() {
        LocalDate today = LocalDate.now();
        LocalDate venceEmDezDias = today.plusDays(10);

        Cliente cliente = Cliente.builder()
                .id(10L)
                .razaoSocial("Empresa Teste Ltda")
                .build();

        Documento documento = Documento.builder()
                .id(1L)
                .nome("Licença ABC")
                .dataValidade(venceEmDezDias)
                .cliente(cliente)
                .build();

        Page<Documento> documentPage = new PageImpl<>(List.of(documento));

        when(configuracaoAlertaRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(defaultConfig));
        when(alertaLogRepository.findSnoozedDocumentoIds(eq(StatusEnvio.SNOOZED), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        when(documentoRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(documentPage);

        List<AlertaPendenteResponse> result = alertaService.getAlertasPendentes();

        assertThat(result).hasSize(1);
        AlertaPendenteResponse item = result.get(0);
        assertThat(item.getId()).isEqualTo(1L);
        assertThat(item.getNome()).isEqualTo("Licença ABC");
        assertThat(item.getClienteNome()).isEqualTo("Empresa Teste Ltda");
        assertThat(item.getDataVencimento()).isEqualTo(venceEmDezDias);
        assertThat(item.getStatus()).isEqualTo("A_VENCER");
    }

    @Test
    void getAlertasPendentes_excludesSnoozedDocuments() {
        LocalDate today = LocalDate.now();

        Documento documento = Documento.builder()
                .id(5L)
                .nome("Doc Snoozado")
                .dataValidade(today.plusDays(5))
                .build();

        Page<Documento> documentPage = new PageImpl<>(List.of(documento));

        when(configuracaoAlertaRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(defaultConfig));
        when(alertaLogRepository.findSnoozedDocumentoIds(eq(StatusEnvio.SNOOZED), any(LocalDate.class)))
                .thenReturn(List.of(5L));
        when(documentoRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(documentPage);

        List<AlertaPendenteResponse> result = alertaService.getAlertasPendentes();

        assertThat(result).isEmpty();
    }

    @Test
    void getAlertasPendentes_marksVencidoStatus() {
        LocalDate today = LocalDate.now();
        LocalDate vencidoOntem = today.minusDays(1);

        Documento documento = Documento.builder()
                .id(2L)
                .nome("Doc Vencido")
                .dataValidade(vencidoOntem)
                .build();

        Page<Documento> documentPage = new PageImpl<>(List.of(documento));

        when(configuracaoAlertaRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(defaultConfig));
        when(alertaLogRepository.findSnoozedDocumentoIds(eq(StatusEnvio.SNOOZED), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        when(documentoRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(documentPage);

        List<AlertaPendenteResponse> result = alertaService.getAlertasPendentes();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo("VENCIDO");
    }

    // -------------------------------------------------------------------------
    // getNotificacaoSummary
    // -------------------------------------------------------------------------

    @Test
    void getNotificacaoSummary_returnsCorrectCounts() {
        when(configuracaoAlertaRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(defaultConfig));
        when(documentoRepository.countAVencer(any(LocalDate.class), any(LocalDate.class))).thenReturn(5L);
        when(documentoRepository.countVencidos(any(LocalDate.class))).thenReturn(3L);

        NotificacaoSummaryResponse summary = alertaService.getNotificacaoSummary();

        assertThat(summary.getDocumentosAVencer()).isEqualTo(5L);
        assertThat(summary.getDocumentosVencidos()).isEqualTo(3L);
        assertThat(summary.getCobrancasVencidas()).isEqualTo(0L);
        assertThat(summary.getTotalPendentes()).isEqualTo(8L);
    }

    @Test
    void getNotificacaoSummary_returnsZeroWhenNoPending() {
        when(configuracaoAlertaRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(defaultConfig));
        when(documentoRepository.countAVencer(any(LocalDate.class), any(LocalDate.class))).thenReturn(0L);
        when(documentoRepository.countVencidos(any(LocalDate.class))).thenReturn(0L);

        NotificacaoSummaryResponse summary = alertaService.getNotificacaoSummary();

        assertThat(summary.getTotalPendentes()).isEqualTo(0L);
    }

    // -------------------------------------------------------------------------
    // snoozeAlerta
    // -------------------------------------------------------------------------

    @Test
    void snoozeAlerta_createsSnoozedLogEntry() {
        Documento documento = Documento.builder()
                .id(1L)
                .nome("Licença XYZ")
                .build();

        AlertaLog snoozedLog = AlertaLog.builder()
                .id(10L)
                .documento(documento)
                .tipo(TipoAlerta.DOCUMENTO)
                .canal(CanalAlerta.EMAIL)
                .statusEnvio(StatusEnvio.SNOOZED)
                .snoozedAte(LocalDate.now().plusDays(7))
                .mensagem("Alerta adiado por 7 dia(s). Próximo alerta em: " + LocalDate.now().plusDays(7))
                .build();

        when(documentoRepository.findById(1L)).thenReturn(Optional.of(documento));
        when(alertaLogRepository.save(any(AlertaLog.class))).thenReturn(snoozedLog);

        AlertaLogResponse response = alertaService.snoozeAlerta(1L, 7);

        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getStatusEnvio()).isEqualTo(StatusEnvio.SNOOZED);
        assertThat(response.getSnoozedAte()).isEqualTo(LocalDate.now().plusDays(7));

        ArgumentCaptor<AlertaLog> captor = ArgumentCaptor.forClass(AlertaLog.class);
        verify(alertaLogRepository).save(captor.capture());

        AlertaLog saved = captor.getValue();
        assertThat(saved.getStatusEnvio()).isEqualTo(StatusEnvio.SNOOZED);
        assertThat(saved.getCanal()).isEqualTo(CanalAlerta.EMAIL);
        assertThat(saved.getTipo()).isEqualTo(TipoAlerta.DOCUMENTO);
        assertThat(saved.getSnoozedAte()).isEqualTo(LocalDate.now().plusDays(7));
    }

    @Test
    void snoozeAlerta_throwsWhenDocumentoNotFound() {
        when(documentoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> alertaService.snoozeAlerta(99L, 7))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    // -------------------------------------------------------------------------
    // enviarManual
    // -------------------------------------------------------------------------

    @Test
    void enviarManual_createsManualAlertLog() {
        Contato contato = new Contato();
        contato.setEmail("contato@empresa.com");

        Cliente cliente = Cliente.builder()
                .id(10L)
                .razaoSocial("Empresa Teste")
                .contatos(List.of(contato))
                .build();

        Documento documento = Documento.builder()
                .id(1L)
                .nome("Licença ABC")
                .dataValidade(LocalDate.now().plusDays(15))
                .cliente(cliente)
                .build();

        AlertaLog savedLog = AlertaLog.builder()
                .id(20L)
                .documento(documento)
                .tipo(TipoAlerta.DOCUMENTO)
                .canal(CanalAlerta.EMAIL)
                .destinatario("contato@empresa.com")
                .statusEnvio(StatusEnvio.PENDENTE)
                .build();

        when(documentoRepository.findById(1L)).thenReturn(Optional.of(documento));
        when(configuracaoAlertaRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(defaultConfig));
        when(alertaLogRepository.save(any(AlertaLog.class))).thenReturn(savedLog);

        AlertaLogResponse response = alertaService.enviarManual(1L);

        assertThat(response.getId()).isEqualTo(20L);
        assertThat(response.getStatusEnvio()).isEqualTo(StatusEnvio.PENDENTE);
        assertThat(response.getDestinatario()).isEqualTo("contato@empresa.com");

        ArgumentCaptor<AlertaLog> captor = ArgumentCaptor.forClass(AlertaLog.class);
        verify(alertaLogRepository).save(captor.capture());

        AlertaLog saved = captor.getValue();
        assertThat(saved.getStatusEnvio()).isEqualTo(StatusEnvio.PENDENTE);
        assertThat(saved.getTipo()).isEqualTo(TipoAlerta.DOCUMENTO);
        assertThat(saved.getCanal()).isEqualTo(CanalAlerta.EMAIL);
    }

    @Test
    void enviarManual_throwsWhenDocumentoNotFound() {
        when(documentoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> alertaService.enviarManual(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    // -------------------------------------------------------------------------
    // getLogs
    // -------------------------------------------------------------------------

    @Test
    void getLogs_returnsPaginatedLogs() {
        AlertaLog log = AlertaLog.builder()
                .id(1L)
                .tipo(TipoAlerta.DOCUMENTO)
                .canal(CanalAlerta.EMAIL)
                .statusEnvio(StatusEnvio.PENDENTE)
                .build();

        Page<AlertaLog> logPage = new PageImpl<>(List.of(log));
        Pageable pageable = PageRequest.of(0, 10);

        when(alertaLogRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(logPage);

        Page<AlertaLogResponse> result = alertaService.getLogs(pageable, null, null, null);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(1L);
        assertThat(result.getContent().get(0).getStatusEnvio()).isEqualTo(StatusEnvio.PENDENTE);
    }

    @Test
    void getLogs_returnsEmptyPageWhenNoLogs() {
        Page<AlertaLog> emptyPage = new PageImpl<>(Collections.emptyList());
        Pageable pageable = PageRequest.of(0, 10);

        when(alertaLogRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(emptyPage);

        Page<AlertaLogResponse> result = alertaService.getLogs(pageable, StatusEnvio.ENVIADO, TipoAlerta.DOCUMENTO, 1L);

        assertThat(result.getTotalElements()).isEqualTo(0);
        assertThat(result.getContent()).isEmpty();
    }

    // -------------------------------------------------------------------------
    // getHistorico
    // -------------------------------------------------------------------------

    @Test
    void getHistorico_returnsLogsForDocumento() {
        Documento documento = Documento.builder().id(1L).nome("Doc A").build();
        AlertaLog log = AlertaLog.builder()
                .id(5L)
                .documento(documento)
                .tipo(TipoAlerta.DOCUMENTO)
                .canal(CanalAlerta.EMAIL)
                .statusEnvio(StatusEnvio.PENDENTE)
                .build();

        when(documentoRepository.findById(1L)).thenReturn(Optional.of(documento));
        when(alertaLogRepository.findByDocumentoIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(log));

        List<AlertaLogResponse> result = alertaService.getHistorico(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(5L);
    }

    @Test
    void getHistorico_throwsWhenDocumentoNotFound() {
        when(documentoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> alertaService.getHistorico(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void getHistorico_returnsEmptyListWhenNoLogs() {
        Documento documento = Documento.builder().id(2L).nome("Doc B").build();

        when(documentoRepository.findById(2L)).thenReturn(Optional.of(documento));
        when(alertaLogRepository.findByDocumentoIdOrderByCreatedAtDesc(2L)).thenReturn(Collections.emptyList());

        List<AlertaLogResponse> result = alertaService.getHistorico(2L);

        assertThat(result).isEmpty();
    }

    // -------------------------------------------------------------------------
    // processarAlertasDiarios
    // -------------------------------------------------------------------------

    @Test
    void processarAlertasDiarios_criaAlertaQuandoDiasCorrespondem() {
        LocalDate today = LocalDate.now();
        // diasAntecedencia = "30,15,7,1" — use exatamente 7 dias a partir de hoje
        LocalDate venceEm7Dias = today.plusDays(7);

        Documento documento = Documento.builder()
                .id(1L)
                .nome("Licença XYZ")
                .dataValidade(venceEm7Dias)
                .build();

        Page<Documento> page = new PageImpl<>(List.of(documento));

        when(configuracaoAlertaRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(defaultConfig));
        when(alertaLogRepository.findSnoozedDocumentoIds(eq(StatusEnvio.SNOOZED), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        when(documentoRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(alertaLogRepository.existsByDocumentoIdAndCreatedAtDate(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(false);
        when(alertaLogRepository.save(any(AlertaLog.class))).thenAnswer(inv -> inv.getArgument(0));

        alertaService.processarAlertasDiarios();

        verify(alertaLogRepository, times(1)).save(any(AlertaLog.class));
    }

    @Test
    void processarAlertasDiarios_ignoraDocumentoComSnoozeAtivo() {
        LocalDate today = LocalDate.now();
        LocalDate venceEm7Dias = today.plusDays(7);

        Documento documento = Documento.builder()
                .id(2L)
                .nome("Doc Snoozado")
                .dataValidade(venceEm7Dias)
                .build();

        Page<Documento> page = new PageImpl<>(List.of(documento));

        when(configuracaoAlertaRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(defaultConfig));
        when(alertaLogRepository.findSnoozedDocumentoIds(eq(StatusEnvio.SNOOZED), any(LocalDate.class)))
                .thenReturn(List.of(2L));
        when(documentoRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        alertaService.processarAlertasDiarios();

        verify(alertaLogRepository, never()).save(any(AlertaLog.class));
    }

    @Test
    void processarAlertasDiarios_ignoraDuplicataDoMesmoDia() {
        LocalDate today = LocalDate.now();
        LocalDate venceEm7Dias = today.plusDays(7);

        Documento documento = Documento.builder()
                .id(3L)
                .nome("Doc Duplicado")
                .dataValidade(venceEm7Dias)
                .build();

        Page<Documento> page = new PageImpl<>(List.of(documento));

        when(configuracaoAlertaRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(defaultConfig));
        when(alertaLogRepository.findSnoozedDocumentoIds(eq(StatusEnvio.SNOOZED), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        when(documentoRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(alertaLogRepository.existsByDocumentoIdAndCreatedAtDate(eq(3L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(true);

        alertaService.processarAlertasDiarios();

        verify(alertaLogRepository, never()).save(any(AlertaLog.class));
    }

    @Test
    void processarAlertasDiarios_ignoraDocumentoSemDataValidade() {
        Documento documento = Documento.builder()
                .id(4L)
                .nome("Sem Validade")
                .dataValidade(null)
                .build();

        Page<Documento> page = new PageImpl<>(List.of(documento));

        when(configuracaoAlertaRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(defaultConfig));
        when(alertaLogRepository.findSnoozedDocumentoIds(eq(StatusEnvio.SNOOZED), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        when(documentoRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        alertaService.processarAlertasDiarios();

        verify(alertaLogRepository, never()).save(any(AlertaLog.class));
    }

    @Test
    void processarAlertasDiarios_naoGeraAlertaQuandoDiasNaoCorrespondem() {
        LocalDate today = LocalDate.now();
        // diasAntecedencia = "30,15,7,1" — 5 dias não é nenhum deles
        LocalDate venceEm5Dias = today.plusDays(5);

        Documento documento = Documento.builder()
                .id(5L)
                .nome("Licença 5 dias")
                .dataValidade(venceEm5Dias)
                .build();

        Page<Documento> page = new PageImpl<>(List.of(documento));

        when(configuracaoAlertaRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(defaultConfig));
        when(alertaLogRepository.findSnoozedDocumentoIds(eq(StatusEnvio.SNOOZED), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        when(documentoRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        alertaService.processarAlertasDiarios();

        verify(alertaLogRepository, never()).save(any(AlertaLog.class));
    }

    @Test
    void processarAlertasDiarios_listaVazia_naoSalvaNada() {
        Page<Documento> emptyPage = new PageImpl<>(Collections.emptyList());

        when(configuracaoAlertaRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(defaultConfig));
        when(alertaLogRepository.findSnoozedDocumentoIds(eq(StatusEnvio.SNOOZED), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        when(documentoRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(emptyPage);

        alertaService.processarAlertasDiarios();

        verify(alertaLogRepository, never()).save(any(AlertaLog.class));
    }

    @Test
    void processarAlertasDiarios_criaAlertaComDestinatarioEMensagem() {
        LocalDate today = LocalDate.now();
        LocalDate venceEm1Dia = today.plusDays(1);

        Contato contato = new Contato();
        contato.setEmail("user@empresa.com");

        Cliente cliente = Cliente.builder()
                .id(10L)
                .razaoSocial("Empresa XYZ")
                .contatos(List.of(contato))
                .build();

        Documento documento = Documento.builder()
                .id(6L)
                .nome("Licença 1 dia")
                .dataValidade(venceEm1Dia)
                .cliente(cliente)
                .build();

        Page<Documento> page = new PageImpl<>(List.of(documento));

        when(configuracaoAlertaRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(defaultConfig));
        when(alertaLogRepository.findSnoozedDocumentoIds(eq(StatusEnvio.SNOOZED), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        when(documentoRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(alertaLogRepository.existsByDocumentoIdAndCreatedAtDate(eq(6L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(false);
        when(alertaLogRepository.save(any(AlertaLog.class))).thenAnswer(inv -> inv.getArgument(0));

        alertaService.processarAlertasDiarios();

        ArgumentCaptor<AlertaLog> captor = ArgumentCaptor.forClass(AlertaLog.class);
        verify(alertaLogRepository).save(captor.capture());
        AlertaLog saved = captor.getValue();
        assertThat(saved.getDestinatario()).isEqualTo("user@empresa.com");
        assertThat(saved.getMensagem()).contains("Licença 1 dia");
        assertThat(saved.getMensagem()).contains("Empresa XYZ");
        assertThat(saved.getStatusEnvio()).isEqualTo(StatusEnvio.PENDENTE);
    }

    // -------------------------------------------------------------------------
    // getAlertasPendentes — edge cases
    // -------------------------------------------------------------------------

    @Test
    void getAlertasPendentes_documentoSemCliente_retornaClienteNomeNulo() {
        LocalDate today = LocalDate.now();

        Documento documento = Documento.builder()
                .id(10L)
                .nome("Sem Cliente")
                .dataValidade(today.plusDays(10))
                .cliente(null)
                .build();

        Page<Documento> page = new PageImpl<>(List.of(documento));

        when(configuracaoAlertaRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(defaultConfig));
        when(alertaLogRepository.findSnoozedDocumentoIds(eq(StatusEnvio.SNOOZED), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        when(documentoRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        List<AlertaPendenteResponse> result = alertaService.getAlertasPendentes();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getClienteNome()).isNull();
    }

    @Test
    void getAlertasPendentes_documentoSemDataValidade_retornaDiasZeroEStatusAVencer() {
        Documento documento = Documento.builder()
                .id(11L)
                .nome("Sem Data Validade")
                .dataValidade(null)
                .build();

        Page<Documento> page = new PageImpl<>(List.of(documento));

        when(configuracaoAlertaRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(defaultConfig));
        when(alertaLogRepository.findSnoozedDocumentoIds(eq(StatusEnvio.SNOOZED), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        when(documentoRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        List<AlertaPendenteResponse> result = alertaService.getAlertasPendentes();

        assertThat(result).hasSize(1);
        AlertaPendenteResponse item = result.get(0);
        assertThat(item.getDiasRestantes()).isEqualTo(0);
        assertThat(item.getStatus()).isEqualTo("A_VENCER");
    }

    // -------------------------------------------------------------------------
    // enviarManual — edge cases
    // -------------------------------------------------------------------------

    @Test
    void enviarManual_documentoSemCliente_destinatarioNulo() {
        Documento documento = Documento.builder()
                .id(7L)
                .nome("Doc Sem Cliente")
                .dataValidade(LocalDate.now().plusDays(10))
                .cliente(null)
                .build();

        AlertaLog savedLog = AlertaLog.builder()
                .id(30L)
                .documento(documento)
                .tipo(TipoAlerta.DOCUMENTO)
                .canal(CanalAlerta.EMAIL)
                .destinatario(null)
                .statusEnvio(StatusEnvio.PENDENTE)
                .build();

        when(documentoRepository.findById(7L)).thenReturn(Optional.of(documento));
        when(configuracaoAlertaRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(defaultConfig));
        when(alertaLogRepository.save(any(AlertaLog.class))).thenReturn(savedLog);

        AlertaLogResponse response = alertaService.enviarManual(7L);

        assertThat(response.getDestinatario()).isNull();
    }

    @Test
    void enviarManual_templateNulo_mensagemVazia() {
        ConfiguracaoAlerta configSemTemplate = ConfiguracaoAlerta.builder()
                .id(1L)
                .diasAntecedencia("30,15,7,1")
                .horarioExecucao(LocalTime.of(8, 0))
                .emailAtivo(true)
                .whatsappAtivo(false)
                .templateEmail(null)
                .build();

        Documento documento = Documento.builder()
                .id(8L)
                .nome("Licença Sem Template")
                .dataValidade(LocalDate.now().plusDays(5))
                .build();

        AlertaLog savedLog = AlertaLog.builder()
                .id(31L)
                .documento(documento)
                .tipo(TipoAlerta.DOCUMENTO)
                .canal(CanalAlerta.EMAIL)
                .mensagem("")
                .statusEnvio(StatusEnvio.PENDENTE)
                .build();

        when(documentoRepository.findById(8L)).thenReturn(Optional.of(documento));
        when(configuracaoAlertaRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(configSemTemplate));
        when(alertaLogRepository.save(any(AlertaLog.class))).thenReturn(savedLog);

        AlertaLogResponse response = alertaService.enviarManual(8L);

        ArgumentCaptor<AlertaLog> captor = ArgumentCaptor.forClass(AlertaLog.class);
        verify(alertaLogRepository).save(captor.capture());
        assertThat(captor.getValue().getMensagem()).isEqualTo("");
    }

    // -------------------------------------------------------------------------
    // toAlertaLogResponse — with AlertaLog having no documento
    // -------------------------------------------------------------------------

    @Test
    void getLogs_alertaLogSemDocumento_retornaDocumentoIdNulo() {
        AlertaLog logSemDoc = AlertaLog.builder()
                .id(99L)
                .documento(null)
                .tipo(TipoAlerta.DOCUMENTO)
                .canal(CanalAlerta.EMAIL)
                .statusEnvio(StatusEnvio.PENDENTE)
                .cobrancaId(42L)
                .build();

        Page<AlertaLog> logPage = new PageImpl<>(List.of(logSemDoc));
        Pageable pageable = PageRequest.of(0, 10);

        when(alertaLogRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(logPage);

        Page<AlertaLogResponse> result = alertaService.getLogs(pageable, null, null, null);

        assertThat(result.getTotalElements()).isEqualTo(1);
        AlertaLogResponse item = result.getContent().get(0);
        assertThat(item.getDocumentoId()).isNull();
        assertThat(item.getDocumentoNome()).isNull();
        assertThat(item.getClienteNome()).isNull();
        assertThat(item.getCobrancaId()).isEqualTo(42L);
    }

    @Test
    void getLogs_alertaLogComDocumentoSemCliente_clienteNomeNulo() {
        Documento docSemCliente = Documento.builder()
                .id(50L)
                .nome("Doc sem cliente")
                .cliente(null)
                .build();

        AlertaLog logComDoc = AlertaLog.builder()
                .id(100L)
                .documento(docSemCliente)
                .tipo(TipoAlerta.DOCUMENTO)
                .canal(CanalAlerta.EMAIL)
                .statusEnvio(StatusEnvio.ENVIADO)
                .build();

        Page<AlertaLog> logPage = new PageImpl<>(List.of(logComDoc));
        Pageable pageable = PageRequest.of(0, 10);

        when(alertaLogRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(logPage);

        Page<AlertaLogResponse> result = alertaService.getLogs(pageable, null, null, null);

        assertThat(result.getContent().get(0).getDocumentoId()).isEqualTo(50L);
        assertThat(result.getContent().get(0).getClienteNome()).isNull();
    }

    // -------------------------------------------------------------------------
    // Specification lambda coverage — buildLogsSpec branches
    // -------------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private Specification<AlertaLog> captureLogsSpec(StatusEnvio statusEnvio, TipoAlerta tipo, Long docId) {
        Pageable pageable = PageRequest.of(0, 10);
        Page<AlertaLog> page = new PageImpl<>(Collections.emptyList());
        ArgumentCaptor<Specification<AlertaLog>> captor = ArgumentCaptor.forClass(Specification.class);
        when(alertaLogRepository.findAll(captor.capture(), eq(pageable))).thenReturn(page);
        alertaService.getLogs(pageable, statusEnvio, tipo, docId);
        return captor.getValue();
    }

    @SuppressWarnings("unchecked")
    private void invokeLogsSpec(Specification<AlertaLog> spec) {
        Root<AlertaLog> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Path<Object> statusEnvioPath = mock(Path.class);
        Path<Object> tipoPath = mock(Path.class);
        Path<Object> documentoPath = mock(Path.class);
        Path<Object> documentoIdPath = mock(Path.class);

        lenient().when(root.get("statusEnvio")).thenReturn(statusEnvioPath);
        lenient().when(root.get("tipo")).thenReturn(tipoPath);
        lenient().when(root.get("documento")).thenReturn(documentoPath);
        lenient().when(documentoPath.get("id")).thenReturn(documentoIdPath);
        lenient().when(cb.equal(any(), any())).thenReturn(mock(Predicate.class));
        lenient().when(cb.and(any(Predicate[].class))).thenReturn(mock(Predicate.class));

        spec.toPredicate(root, query, cb);
    }

    @Test
    void logsSpecLambda_semFiltros_executa() {
        invokeLogsSpec(captureLogsSpec(null, null, null));
    }

    @Test
    void logsSpecLambda_comStatusEnvio_executa() {
        invokeLogsSpec(captureLogsSpec(StatusEnvio.PENDENTE, null, null));
    }

    @Test
    void logsSpecLambda_comTipo_executa() {
        invokeLogsSpec(captureLogsSpec(null, TipoAlerta.DOCUMENTO, null));
    }

    @Test
    void logsSpecLambda_comDocumentoId_executa() {
        invokeLogsSpec(captureLogsSpec(null, null, 1L));
    }

    @Test
    void logsSpecLambda_comTodosFiltros_executa() {
        invokeLogsSpec(captureLogsSpec(StatusEnvio.ENVIADO, TipoAlerta.COBRANCA, 5L));
    }

    // -------------------------------------------------------------------------
    // buildDocumentosVencendoSpec lambda coverage
    // -------------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    @Test
    void documentosVencendoSpec_executa() {
        LocalDate today = LocalDate.now();
        Page<Documento> emptyPage = new PageImpl<>(Collections.emptyList());
        ArgumentCaptor<Specification<Documento>> captor = ArgumentCaptor.forClass(Specification.class);

        when(configuracaoAlertaRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(defaultConfig));
        when(alertaLogRepository.findSnoozedDocumentoIds(eq(StatusEnvio.SNOOZED), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        when(documentoRepository.findAll(captor.capture(), any(Pageable.class))).thenReturn(emptyPage);

        alertaService.getAlertasPendentes();

        Specification<Documento> spec = captor.getValue();

        Root<Documento> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Path<Object> dataValidadePath = mock(Path.class);

        lenient().when(root.get("dataValidade")).thenReturn(dataValidadePath);
        lenient().when(cb.isNotNull(any())).thenReturn(mock(Predicate.class));
        lenient().when(cb.lessThanOrEqualTo(any(), any(Comparable.class))).thenReturn(mock(Predicate.class));
        lenient().when(cb.and(any(Predicate[].class))).thenReturn(mock(Predicate.class));

        spec.toPredicate(root, query, cb);
    }

    // -------------------------------------------------------------------------
    // resolveDestinatario — cliente with contatos but none have email
    // -------------------------------------------------------------------------

    @Test
    void enviarManual_clienteComContatoSemEmail_destinatarioNulo() {
        Contato contatoSemEmail = new Contato();
        contatoSemEmail.setEmail(null);

        Cliente cliente = Cliente.builder()
                .id(20L)
                .razaoSocial("Sem Email")
                .contatos(List.of(contatoSemEmail))
                .build();

        Documento documento = Documento.builder()
                .id(15L)
                .nome("Doc 15")
                .dataValidade(LocalDate.now().plusDays(10))
                .cliente(cliente)
                .build();

        AlertaLog savedLog = AlertaLog.builder()
                .id(50L)
                .documento(documento)
                .tipo(TipoAlerta.DOCUMENTO)
                .canal(CanalAlerta.EMAIL)
                .destinatario(null)
                .statusEnvio(StatusEnvio.PENDENTE)
                .build();

        when(documentoRepository.findById(15L)).thenReturn(Optional.of(documento));
        when(configuracaoAlertaRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(defaultConfig));
        when(alertaLogRepository.save(any(AlertaLog.class))).thenReturn(savedLog);

        AlertaLogResponse response = alertaService.enviarManual(15L);

        assertThat(response.getDestinatario()).isNull();
    }

    // -------------------------------------------------------------------------
    // buildMensagem — document has null dataValidade
    // -------------------------------------------------------------------------

    @Test
    void enviarManual_documentoSemDataValidade_mensagemComDiasZero() {
        Documento documento = Documento.builder()
                .id(16L)
                .nome("Doc Sem Validade")
                .dataValidade(null)
                .build();

        AlertaLog savedLog = AlertaLog.builder()
                .id(60L)
                .documento(documento)
                .statusEnvio(StatusEnvio.PENDENTE)
                .tipo(TipoAlerta.DOCUMENTO)
                .canal(CanalAlerta.EMAIL)
                .build();

        when(documentoRepository.findById(16L)).thenReturn(Optional.of(documento));
        when(configuracaoAlertaRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(defaultConfig));
        when(alertaLogRepository.save(any(AlertaLog.class))).thenReturn(savedLog);

        alertaService.enviarManual(16L);

        ArgumentCaptor<AlertaLog> captor = ArgumentCaptor.forClass(AlertaLog.class);
        verify(alertaLogRepository).save(captor.capture());
        assertThat(captor.getValue().getMensagem()).contains("0");
    }

    // -------------------------------------------------------------------------
    // processarEnvioPendentes — EMAIL
    // -------------------------------------------------------------------------

    @Test
    void processarEnvioPendentes_emailEnviadoComSucesso() {
        AlertaLog pendente = AlertaLog.builder()
                .id(100L)
                .canal(CanalAlerta.EMAIL)
                .statusEnvio(StatusEnvio.PENDENTE)
                .destinatario("user@test.com")
                .mensagem("Alerta teste")
                .build();

        when(configuracaoAlertaRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(defaultConfig));
        when(alertaLogRepository.findByStatusEnvioAndCanal(StatusEnvio.PENDENTE, CanalAlerta.EMAIL))
                .thenReturn(List.of(pendente));
        when(emailService.enviar("user@test.com", "Alerta de Vencimento — Prediman CRM", "Alerta teste"))
                .thenReturn(true);
        when(alertaLogRepository.save(any(AlertaLog.class))).thenAnswer(inv -> inv.getArgument(0));

        alertaService.processarEnvioPendentes();

        ArgumentCaptor<AlertaLog> captor = ArgumentCaptor.forClass(AlertaLog.class);
        verify(alertaLogRepository, atLeastOnce()).save(captor.capture());
        assertThat(captor.getValue().getStatusEnvio()).isEqualTo(StatusEnvio.ENVIADO);
        assertThat(captor.getValue().getDataEnvio()).isNotNull();
    }

    @Test
    void processarEnvioPendentes_emailComErro_marcaErro() {
        AlertaLog pendente = AlertaLog.builder()
                .id(101L)
                .canal(CanalAlerta.EMAIL)
                .statusEnvio(StatusEnvio.PENDENTE)
                .destinatario("fail@test.com")
                .mensagem("Alerta falha")
                .build();

        when(configuracaoAlertaRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(defaultConfig));
        when(alertaLogRepository.findByStatusEnvioAndCanal(StatusEnvio.PENDENTE, CanalAlerta.EMAIL))
                .thenReturn(List.of(pendente));
        when(emailService.enviar(anyString(), anyString(), anyString())).thenReturn(false);
        when(alertaLogRepository.save(any(AlertaLog.class))).thenAnswer(inv -> inv.getArgument(0));

        alertaService.processarEnvioPendentes();

        ArgumentCaptor<AlertaLog> captor = ArgumentCaptor.forClass(AlertaLog.class);
        verify(alertaLogRepository, atLeastOnce()).save(captor.capture());
        assertThat(captor.getValue().getStatusEnvio()).isEqualTo(StatusEnvio.ERRO);
    }

    @Test
    void processarEnvioPendentes_emailDesativado_naoProcesa() {
        defaultConfig.setEmailAtivo(false);
        defaultConfig.setWhatsappAtivo(false);
        when(configuracaoAlertaRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(defaultConfig));

        alertaService.processarEnvioPendentes();

        verify(alertaLogRepository, never()).findByStatusEnvioAndCanal(any(), any());
    }

    // -------------------------------------------------------------------------
    // processarEnvioPendentes — WHATSAPP
    // -------------------------------------------------------------------------

    @Test
    void processarEnvioPendentes_whatsappEnviadoComSucesso() {
        defaultConfig.setWhatsappAtivo(true);
        AlertaLog pendente = AlertaLog.builder()
                .id(102L)
                .canal(CanalAlerta.WHATSAPP)
                .statusEnvio(StatusEnvio.PENDENTE)
                .destinatario("11999887766")
                .mensagem("WhatsApp teste")
                .build();

        when(configuracaoAlertaRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(defaultConfig));
        when(alertaLogRepository.findByStatusEnvioAndCanal(StatusEnvio.PENDENTE, CanalAlerta.EMAIL))
                .thenReturn(List.of());
        when(alertaLogRepository.findByStatusEnvioAndCanal(StatusEnvio.PENDENTE, CanalAlerta.WHATSAPP))
                .thenReturn(List.of(pendente));
        when(whatsAppService.enviar("11999887766", "WhatsApp teste")).thenReturn(true);
        when(alertaLogRepository.save(any(AlertaLog.class))).thenAnswer(inv -> inv.getArgument(0));

        alertaService.processarEnvioPendentes();

        ArgumentCaptor<AlertaLog> captor = ArgumentCaptor.forClass(AlertaLog.class);
        verify(alertaLogRepository, atLeastOnce()).save(captor.capture());
        assertThat(captor.getValue().getStatusEnvio()).isEqualTo(StatusEnvio.ENVIADO);
    }

    @Test
    void processarEnvioPendentes_whatsappComErro() {
        defaultConfig.setWhatsappAtivo(true);
        AlertaLog pendente = AlertaLog.builder()
                .id(103L)
                .canal(CanalAlerta.WHATSAPP)
                .statusEnvio(StatusEnvio.PENDENTE)
                .destinatario("11999887766")
                .mensagem("WhatsApp falha")
                .build();

        when(configuracaoAlertaRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(defaultConfig));
        when(alertaLogRepository.findByStatusEnvioAndCanal(StatusEnvio.PENDENTE, CanalAlerta.EMAIL))
                .thenReturn(List.of());
        when(alertaLogRepository.findByStatusEnvioAndCanal(StatusEnvio.PENDENTE, CanalAlerta.WHATSAPP))
                .thenReturn(List.of(pendente));
        when(whatsAppService.enviar(anyString(), anyString())).thenReturn(false);
        when(alertaLogRepository.save(any(AlertaLog.class))).thenAnswer(inv -> inv.getArgument(0));

        alertaService.processarEnvioPendentes();

        ArgumentCaptor<AlertaLog> captor = ArgumentCaptor.forClass(AlertaLog.class);
        verify(alertaLogRepository, atLeastOnce()).save(captor.capture());
        assertThat(captor.getValue().getStatusEnvio()).isEqualTo(StatusEnvio.ERRO);
    }

    // -------------------------------------------------------------------------
    // processarAlertasDiarios — creates both EMAIL and WHATSAPP when both active
    // -------------------------------------------------------------------------

    @Test
    void processarAlertasDiarios_criaAlertaEmailEWhatsapp() {
        defaultConfig.setEmailAtivo(true);
        defaultConfig.setWhatsappAtivo(true);
        defaultConfig.setTemplateWhatsapp("WA: {{documento}} vence em {{dias}} dias");

        Contato contato = new Contato();
        contato.setEmail("user@test.com");
        contato.setWhatsapp("11999887766");

        Cliente cliente = Cliente.builder()
                .id(1L)
                .razaoSocial("Empresa")
                .contatos(List.of(contato))
                .build();

        LocalDate validade = LocalDate.now().plusDays(7);
        Documento documento = Documento.builder()
                .id(1L)
                .nome("Doc")
                .dataValidade(validade)
                .cliente(cliente)
                .build();

        when(configuracaoAlertaRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(defaultConfig));
        when(alertaLogRepository.findSnoozedDocumentoIds(any(), any())).thenReturn(List.of());
        when(documentoRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(documento)));
        when(alertaLogRepository.existsByDocumentoIdAndCreatedAtDate(any(), any(), any())).thenReturn(false);
        when(alertaLogRepository.save(any(AlertaLog.class))).thenAnswer(inv -> inv.getArgument(0));

        alertaService.processarAlertasDiarios();

        ArgumentCaptor<AlertaLog> captor = ArgumentCaptor.forClass(AlertaLog.class);
        verify(alertaLogRepository, atLeast(2)).save(captor.capture());

        List<AlertaLog> saved = captor.getAllValues();
        assertThat(saved).extracting("canal").contains(CanalAlerta.EMAIL, CanalAlerta.WHATSAPP);
    }

    // -------------------------------------------------------------------------
    // resolveDestinatarioWhatsapp
    // -------------------------------------------------------------------------

    @Test
    void processarAlertasDiarios_whatsappSemContato_destinatarioNulo() {
        defaultConfig.setEmailAtivo(false);
        defaultConfig.setWhatsappAtivo(true);
        defaultConfig.setTemplateWhatsapp("WA: {{documento}}");

        Cliente cliente = Cliente.builder()
                .id(1L)
                .razaoSocial("Sem WA")
                .contatos(List.of())
                .build();

        Documento documento = Documento.builder()
                .id(1L)
                .nome("Doc")
                .dataValidade(LocalDate.now().plusDays(7))
                .cliente(cliente)
                .build();

        when(configuracaoAlertaRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(defaultConfig));
        when(alertaLogRepository.findSnoozedDocumentoIds(any(), any())).thenReturn(List.of());
        when(documentoRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(documento)));
        when(alertaLogRepository.existsByDocumentoIdAndCreatedAtDate(any(), any(), any())).thenReturn(false);
        when(alertaLogRepository.save(any(AlertaLog.class))).thenAnswer(inv -> inv.getArgument(0));

        alertaService.processarAlertasDiarios();

        ArgumentCaptor<AlertaLog> captor = ArgumentCaptor.forClass(AlertaLog.class);
        verify(alertaLogRepository).save(captor.capture());
        assertThat(captor.getValue().getDestinatario()).isNull();
    }
}
