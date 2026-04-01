package com.prediman.crm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.prediman.crm.dto.ContratoRequest;
import com.prediman.crm.dto.ContratoResponse;
import com.prediman.crm.model.enums.Periodicidade;
import com.prediman.crm.model.enums.StatusContrato;
import com.prediman.crm.security.JwtAuthenticationFilter;
import com.prediman.crm.security.JwtTokenProvider;
import com.prediman.crm.security.RateLimitFilter;
import com.prediman.crm.security.UserDetailsServiceImpl;
import com.prediman.crm.service.ContratoService;
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

@WebMvcTest(ContratoController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("ContratoController — testes de unidade")
class ContratoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ContratoService contratoService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @MockBean
    private RateLimitFilter rateLimitFilter;

    private ContratoResponse buildContratoResponse(Long id, String descricao) {
        return ContratoResponse.builder()
                .id(id)
                .descricao(descricao)
                .valor(new BigDecimal("1500.00"))
                .periodicidade(Periodicidade.MENSAL)
                .dataInicio(LocalDate.of(2024, 1, 1))
                .status(StatusContrato.ATIVO)
                .clienteId(10L)
                .clienteNome("Empresa XYZ")
                .build();
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/contratos
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/v1/contratos — retorna página de contratos")
    void findAll_returns200() throws Exception {
        ContratoResponse contrato = buildContratoResponse(1L, "Contrato de Manutenção");
        Page<ContratoResponse> page = new PageImpl<>(List.of(contrato));

        when(contratoService.findAll(isNull(), isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/contratos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].descricao").value("Contrato de Manutenção"));
    }

    @Test
    @DisplayName("GET /api/v1/contratos — filtros aplicados")
    void findAll_withFilters_returns200() throws Exception {
        Page<ContratoResponse> page = new PageImpl<>(List.of());

        when(contratoService.findAll(eq("Manutenção"), eq(10L), eq(StatusContrato.ATIVO), eq(Periodicidade.MENSAL), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/contratos")
                        .param("search", "Manutenção")
                        .param("clienteId", "10")
                        .param("status", "ATIVO")
                        .param("periodicidade", "MENSAL"))
                .andExpect(status().isOk());
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/contratos/{id}
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/v1/contratos/{id} — retorna 200 com contrato")
    void findById_returns200() throws Exception {
        ContratoResponse contrato = buildContratoResponse(1L, "Contrato de Manutenção");
        when(contratoService.findById(1L)).thenReturn(contrato);

        mockMvc.perform(get("/api/v1/contratos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.descricao").value("Contrato de Manutenção"));
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/clientes/{clienteId}/contratos
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/v1/clientes/{clienteId}/contratos — retorna lista de contratos do cliente")
    void findByCliente_returns200() throws Exception {
        ContratoResponse contrato = buildContratoResponse(1L, "Contrato de Manutenção");
        when(contratoService.findByClienteId(10L)).thenReturn(List.of(contrato));

        mockMvc.perform(get("/api/v1/clientes/10/contratos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].clienteId").value(10));
    }

    // -------------------------------------------------------------------------
    // POST /api/v1/contratos
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("POST /api/v1/contratos — cria contrato e retorna 201")
    void create_returns201() throws Exception {
        ContratoRequest request = ContratoRequest.builder()
                .descricao("Novo Contrato")
                .valor(new BigDecimal("2000.00"))
                .dataInicio(LocalDate.of(2024, 1, 1))
                .clienteId(10L)
                .build();
        ContratoResponse response = buildContratoResponse(2L, "Novo Contrato");

        when(contratoService.create(any(ContratoRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/contratos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.descricao").value("Novo Contrato"));
    }

    @Test
    @DisplayName("POST /api/v1/contratos — descricao em branco retorna 400")
    void create_blankDescricao_returns400() throws Exception {
        ContratoRequest request = ContratoRequest.builder()
                .descricao("")
                .valor(new BigDecimal("2000.00"))
                .dataInicio(LocalDate.of(2024, 1, 1))
                .clienteId(10L)
                .build();

        mockMvc.perform(post("/api/v1/contratos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/contratos — valor nulo retorna 400")
    void create_nullValor_returns400() throws Exception {
        ContratoRequest request = ContratoRequest.builder()
                .descricao("Contrato")
                .valor(null)
                .dataInicio(LocalDate.of(2024, 1, 1))
                .clienteId(10L)
                .build();

        mockMvc.perform(post("/api/v1/contratos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // -------------------------------------------------------------------------
    // PUT /api/v1/contratos/{id}
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("PUT /api/v1/contratos/{id} — atualiza e retorna 200")
    void update_returns200() throws Exception {
        ContratoRequest request = ContratoRequest.builder()
                .descricao("Contrato Atualizado")
                .valor(new BigDecimal("3000.00"))
                .dataInicio(LocalDate.of(2024, 1, 1))
                .clienteId(10L)
                .build();
        ContratoResponse response = buildContratoResponse(1L, "Contrato Atualizado");

        when(contratoService.update(eq(1L), any(ContratoRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/contratos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descricao").value("Contrato Atualizado"));
    }

    // -------------------------------------------------------------------------
    // DELETE /api/v1/contratos/{id}
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("DELETE /api/v1/contratos/{id} — exclui e retorna 204")
    void delete_returns204() throws Exception {
        doNothing().when(contratoService).delete(1L);

        mockMvc.perform(delete("/api/v1/contratos/1"))
                .andExpect(status().isNoContent());
    }

    // -------------------------------------------------------------------------
    // POST /api/v1/contratos/{id}/gerar-cobrancas
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("POST /api/v1/contratos/{id}/gerar-cobrancas — gera cobranças e retorna 200")
    void gerarCobrancas_returns200() throws Exception {
        ContratoResponse response = buildContratoResponse(1L, "Contrato de Manutenção");
        when(contratoService.gerarCobrancasMensais(1L)).thenReturn(response);

        mockMvc.perform(post("/api/v1/contratos/1/gerar-cobrancas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }
}
