package com.prediman.crm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prediman.crm.dto.AlertaLogResponse;
import com.prediman.crm.dto.AlertaPendenteResponse;
import com.prediman.crm.dto.ConfiguracaoAlertaRequest;
import com.prediman.crm.dto.ConfiguracaoAlertaResponse;
import com.prediman.crm.dto.NotificacaoSummaryResponse;
import com.prediman.crm.model.enums.CanalAlerta;
import com.prediman.crm.model.enums.StatusEnvio;
import com.prediman.crm.model.enums.TipoAlerta;
import com.prediman.crm.security.JwtAuthenticationFilter;
import com.prediman.crm.security.JwtTokenProvider;
import com.prediman.crm.security.RateLimitFilter;
import com.prediman.crm.security.UserDetailsServiceImpl;
import com.prediman.crm.service.AlertaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AlertaController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("AlertaController — testes de unidade")
class AlertaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AlertaService alertaService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @MockBean
    private RateLimitFilter rateLimitFilter;

    private AlertaLogResponse buildAlertaLogResponse(Long id) {
        return AlertaLogResponse.builder()
                .id(id)
                .documentoId(10L)
                .documentoNome("Alvará")
                .clienteNome("Empresa XYZ")
                .tipo(TipoAlerta.DOCUMENTO)
                .canal(CanalAlerta.EMAIL)
                .destinatario("contato@empresa.com")
                .statusEnvio(StatusEnvio.ENVIADO)
                .dataEnvio(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/alertas/config
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/v1/alertas/config — retorna configuração de alertas")
    void getConfig_returns200() throws Exception {
        ConfiguracaoAlertaResponse config = ConfiguracaoAlertaResponse.builder()
                .id(1L)
                .diasAntecedencia("30,15,7,1")
                .emailAtivo(true)
                .whatsappAtivo(false)
                .build();

        when(alertaService.getConfig()).thenReturn(config);

        mockMvc.perform(get("/api/v1/alertas/config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.diasAntecedencia").value("30,15,7,1"))
                .andExpect(jsonPath("$.emailAtivo").value(true));
    }

    // -------------------------------------------------------------------------
    // PUT /api/v1/alertas/config
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("PUT /api/v1/alertas/config — atualiza configuração e retorna 200")
    void updateConfig_returns200() throws Exception {
        ConfiguracaoAlertaRequest request = ConfiguracaoAlertaRequest.builder()
                .diasAntecedencia("30,15,7")
                .horarioExecucao("08:00")
                .emailAtivo(true)
                .whatsappAtivo(true)
                .build();
        ConfiguracaoAlertaResponse response = ConfiguracaoAlertaResponse.builder()
                .id(1L)
                .diasAntecedencia("30,15,7")
                .emailAtivo(true)
                .whatsappAtivo(true)
                .build();

        when(alertaService.updateConfig(any(ConfiguracaoAlertaRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/alertas/config")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.diasAntecedencia").value("30,15,7"))
                .andExpect(jsonPath("$.whatsappAtivo").value(true));
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/alertas/pendentes
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/v1/alertas/pendentes — retorna lista de alertas pendentes")
    void getPendentes_returns200() throws Exception {
        AlertaPendenteResponse pendente = AlertaPendenteResponse.builder()
                .id(10L)
                .tipo(TipoAlerta.DOCUMENTO)
                .nome("Alvará")
                .clienteNome("Empresa XYZ")
                .dataVencimento(LocalDate.now().plusDays(7))
                .diasRestantes(7)
                .status("A_VENCER")
                .build();

        when(alertaService.getAlertasPendentes()).thenReturn(List.of(pendente));

        mockMvc.perform(get("/api/v1/alertas/pendentes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].nome").value("Alvará"))
                .andExpect(jsonPath("$[0].status").value("A_VENCER"));
    }

    @Test
    @DisplayName("GET /api/v1/alertas/pendentes — lista vazia retorna 200")
    void getPendentes_emptyList_returns200() throws Exception {
        when(alertaService.getAlertasPendentes()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/alertas/pendentes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/alertas/summary
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/v1/alertas/summary — retorna resumo de notificações")
    void getSummary_returns200() throws Exception {
        NotificacaoSummaryResponse summary = NotificacaoSummaryResponse.builder()
                .totalPendentes(10)
                .documentosAVencer(5)
                .documentosVencidos(3)
                .cobrancasVencidas(2)
                .build();

        when(alertaService.getNotificacaoSummary()).thenReturn(summary);

        mockMvc.perform(get("/api/v1/alertas/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPendentes").value(10))
                .andExpect(jsonPath("$.documentosAVencer").value(5))
                .andExpect(jsonPath("$.documentosVencidos").value(3))
                .andExpect(jsonPath("$.cobrancasVencidas").value(2));
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/alertas/historico
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/v1/alertas/historico — retorna página de histórico")
    void getHistorico_returns200() throws Exception {
        AlertaLogResponse log = buildAlertaLogResponse(1L);
        Page<AlertaLogResponse> page = new PageImpl<>(List.of(log));

        when(alertaService.getLogs(any(Pageable.class), isNull(), isNull(), isNull()))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/alertas/historico"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].documentoNome").value("Alvará"));
    }

    @Test
    @DisplayName("GET /api/v1/alertas/historico — filtros aplicados")
    void getHistorico_withFilters_returns200() throws Exception {
        Page<AlertaLogResponse> page = new PageImpl<>(List.of());

        when(alertaService.getLogs(any(Pageable.class), eq(StatusEnvio.ENVIADO), eq(TipoAlerta.DOCUMENTO), eq(10L)))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/alertas/historico")
                        .param("status", "ENVIADO")
                        .param("tipo", "DOCUMENTO")
                        .param("documentoId", "10"))
                .andExpect(status().isOk());
    }

    // -------------------------------------------------------------------------
    // POST /api/v1/alertas/{documentoId}/snooze
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("POST /api/v1/alertas/{documentoId}/snooze — adia alerta com padrão 7 dias")
    void snooze_defaultDias_returns200() throws Exception {
        AlertaLogResponse response = buildAlertaLogResponse(1L);
        response.setSnoozedAte(LocalDate.now().plusDays(7));
        response.setStatusEnvio(StatusEnvio.SNOOZED);

        when(alertaService.snoozeAlerta(10L, 7)).thenReturn(response);

        mockMvc.perform(post("/api/v1/alertas/10/snooze"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("POST /api/v1/alertas/{documentoId}/snooze — dias customizados")
    void snooze_customDias_returns200() throws Exception {
        AlertaLogResponse response = buildAlertaLogResponse(1L);
        response.setSnoozedAte(LocalDate.now().plusDays(14));
        response.setStatusEnvio(StatusEnvio.SNOOZED);

        when(alertaService.snoozeAlerta(10L, 14)).thenReturn(response);

        mockMvc.perform(post("/api/v1/alertas/10/snooze")
                        .param("dias", "14"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("POST /api/v1/alertas/{documentoId}/snooze — dias abaixo do mínimo retorna 400")
    void snooze_diasBelowMin_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/alertas/10/snooze")
                        .param("dias", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/alertas/{documentoId}/snooze — dias acima do máximo retorna 400")
    void snooze_diasAboveMax_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/alertas/10/snooze")
                        .param("dias", "91"))
                .andExpect(status().isBadRequest());
    }

    // -------------------------------------------------------------------------
    // POST /api/v1/alertas/enviar-manual/{documentoId}
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("POST /api/v1/alertas/enviar-manual/{documentoId} — envia alerta manual e retorna 200")
    void enviarManual_returns200() throws Exception {
        AlertaLogResponse response = buildAlertaLogResponse(1L);
        response.setStatusEnvio(StatusEnvio.PENDENTE);

        when(alertaService.enviarManual(10L)).thenReturn(response);

        mockMvc.perform(post("/api/v1/alertas/enviar-manual/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.documentoId").value(10));
    }
}
