package com.prediman.crm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prediman.crm.dto.CobrancaRequest;
import com.prediman.crm.dto.CobrancaResponse;
import com.prediman.crm.dto.FinanceiroSummaryResponse;
import com.prediman.crm.model.enums.StatusCobranca;
import com.prediman.crm.security.JwtAuthenticationFilter;
import com.prediman.crm.security.JwtTokenProvider;
import com.prediman.crm.security.RateLimitFilter;
import com.prediman.crm.security.UserDetailsServiceImpl;
import com.prediman.crm.service.CobrancaService;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CobrancaController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("CobrancaController — testes de unidade")
class CobrancaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CobrancaService cobrancaService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @MockBean
    private RateLimitFilter rateLimitFilter;

    private CobrancaResponse buildCobrancaResponse(Long id) {
        return CobrancaResponse.builder()
                .id(id)
                .valorEsperado(new BigDecimal("1500.00"))
                .dataVencimento(LocalDate.of(2024, 3, 1))
                .status(StatusCobranca.PENDENTE)
                .statusCalculado(StatusCobranca.PENDENTE)
                .contratoId(5L)
                .build();
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/cobrancas
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/v1/cobrancas — retorna página de cobranças")
    void findAll_returns200() throws Exception {
        CobrancaResponse cobranca = buildCobrancaResponse(1L);
        Page<CobrancaResponse> page = new PageImpl<>(List.of(cobranca));

        when(cobrancaService.findAll(isNull(), isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/cobrancas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].contratoId").value(5));
    }

    @Test
    @DisplayName("GET /api/v1/cobrancas — filtros aplicados")
    void findAll_withFilters_returns200() throws Exception {
        Page<CobrancaResponse> page = new PageImpl<>(List.of());

        when(cobrancaService.findAll(eq(5L), eq(StatusCobranca.PENDENTE), eq(3), eq(2024), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/cobrancas")
                        .param("contratoId", "5")
                        .param("status", "PENDENTE")
                        .param("month", "3")
                        .param("year", "2024"))
                .andExpect(status().isOk());
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/cobrancas/{id}
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/v1/cobrancas/{id} — retorna 200 com cobrança")
    void findById_returns200() throws Exception {
        CobrancaResponse cobranca = buildCobrancaResponse(1L);
        when(cobrancaService.findById(1L)).thenReturn(cobranca);

        mockMvc.perform(get("/api/v1/cobrancas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.valorEsperado").value(1500.00));
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/contratos/{contratoId}/cobrancas
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/v1/contratos/{contratoId}/cobrancas — retorna lista de cobranças do contrato")
    void findByContrato_returns200() throws Exception {
        CobrancaResponse cobranca = buildCobrancaResponse(1L);
        when(cobrancaService.findByContratoId(5L)).thenReturn(List.of(cobranca));

        mockMvc.perform(get("/api/v1/contratos/5/cobrancas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].contratoId").value(5));
    }

    // -------------------------------------------------------------------------
    // POST /api/v1/cobrancas
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("POST /api/v1/cobrancas — cria cobrança e retorna 201")
    void create_returns201() throws Exception {
        CobrancaRequest request = CobrancaRequest.builder()
                .valorEsperado(new BigDecimal("1500.00"))
                .dataVencimento(LocalDate.of(2024, 3, 1))
                .contratoId(5L)
                .build();
        CobrancaResponse response = buildCobrancaResponse(1L);

        when(cobrancaService.create(any(CobrancaRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/cobrancas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("POST /api/v1/cobrancas — valorEsperado nulo retorna 400")
    void create_nullValorEsperado_returns400() throws Exception {
        CobrancaRequest request = CobrancaRequest.builder()
                .valorEsperado(null)
                .dataVencimento(LocalDate.of(2024, 3, 1))
                .contratoId(5L)
                .build();

        mockMvc.perform(post("/api/v1/cobrancas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/cobrancas — dataVencimento nula retorna 400")
    void create_nullDataVencimento_returns400() throws Exception {
        CobrancaRequest request = CobrancaRequest.builder()
                .valorEsperado(new BigDecimal("1500.00"))
                .dataVencimento(null)
                .contratoId(5L)
                .build();

        mockMvc.perform(post("/api/v1/cobrancas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // -------------------------------------------------------------------------
    // PUT /api/v1/cobrancas/{id}
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("PUT /api/v1/cobrancas/{id} — atualiza e retorna 200")
    void update_returns200() throws Exception {
        CobrancaRequest request = CobrancaRequest.builder()
                .valorEsperado(new BigDecimal("2000.00"))
                .dataVencimento(LocalDate.of(2024, 4, 1))
                .contratoId(5L)
                .build();
        CobrancaResponse response = buildCobrancaResponse(1L);

        when(cobrancaService.update(eq(1L), any(CobrancaRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/cobrancas/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    // -------------------------------------------------------------------------
    // PATCH /api/v1/cobrancas/{id}/pagar
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("PATCH /api/v1/cobrancas/{id}/pagar — registra pagamento e retorna 200")
    void registrarPagamento_returns200() throws Exception {
        CobrancaRequest request = CobrancaRequest.builder()
                .valorEsperado(new BigDecimal("1500.00"))
                .valorRecebido(new BigDecimal("1500.00"))
                .dataPagamento(LocalDate.of(2024, 3, 1))
                .dataVencimento(LocalDate.of(2024, 3, 1))
                .contratoId(5L)
                .formaPagamento("PIX")
                .build();
        CobrancaResponse response = CobrancaResponse.builder()
                .id(1L)
                .valorEsperado(new BigDecimal("1500.00"))
                .valorRecebido(new BigDecimal("1500.00"))
                .dataVencimento(LocalDate.of(2024, 3, 1))
                .dataPagamento(LocalDate.of(2024, 3, 1))
                .status(StatusCobranca.PAGO)
                .statusCalculado(StatusCobranca.PAGO)
                .contratoId(5L)
                .build();

        when(cobrancaService.registrarPagamento(eq(1L), any(CobrancaRequest.class))).thenReturn(response);

        mockMvc.perform(patch("/api/v1/cobrancas/1/pagar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("PAGO"));
    }

    // -------------------------------------------------------------------------
    // DELETE /api/v1/cobrancas/{id}
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("DELETE /api/v1/cobrancas/{id} — exclui e retorna 204")
    void delete_returns204() throws Exception {
        doNothing().when(cobrancaService).delete(1L);

        mockMvc.perform(delete("/api/v1/cobrancas/1"))
                .andExpect(status().isNoContent());
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/financeiro/summary
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/v1/financeiro/summary — retorna resumo financeiro")
    void getFinanceiroSummary_returns200() throws Exception {
        FinanceiroSummaryResponse summary = FinanceiroSummaryResponse.builder()
                .aReceber(new BigDecimal("10000.00"))
                .recebido(new BigDecimal("8000.00"))
                .emAtraso(new BigDecimal("1500.00"))
                .vencendo7dias(new BigDecimal("500.00"))
                .build();

        when(cobrancaService.getFinanceiroSummary()).thenReturn(summary);

        mockMvc.perform(get("/api/v1/financeiro/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.areceber").exists())
                .andExpect(jsonPath("$.recebido").exists())
                .andExpect(jsonPath("$.emAtraso").exists())
                .andExpect(jsonPath("$.vencendo7dias").exists());
    }
}
