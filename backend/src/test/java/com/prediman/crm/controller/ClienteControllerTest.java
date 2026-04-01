package com.prediman.crm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prediman.crm.dto.ClienteRequest;
import com.prediman.crm.dto.ClienteResponse;
import com.prediman.crm.dto.ViaCepResponse;
import com.prediman.crm.model.enums.StatusCliente;
import com.prediman.crm.model.enums.TipoPessoa;
import com.prediman.crm.security.JwtAuthenticationFilter;
import com.prediman.crm.security.JwtTokenProvider;
import com.prediman.crm.security.RateLimitFilter;
import com.prediman.crm.security.UserDetailsServiceImpl;
import com.prediman.crm.service.ClienteService;
import com.prediman.crm.service.ViaCepService;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ClienteController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("ClienteController — testes de unidade")
class ClienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ClienteService clienteService;

    @MockBean
    private ViaCepService viaCepService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @MockBean
    private RateLimitFilter rateLimitFilter;

    private ClienteResponse buildClienteResponse(Long id, String razaoSocial) {
        return ClienteResponse.builder()
                .id(id)
                .razaoSocial(razaoSocial)
                .tipoPessoa(TipoPessoa.JURIDICA)
                .status(StatusCliente.ATIVO)
                .build();
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/clientes
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/v1/clientes — retorna página de clientes")
    void findAll_returns200() throws Exception {
        ClienteResponse cliente = buildClienteResponse(1L, "Empresa ABC");
        Page<ClienteResponse> page = new PageImpl<>(List.of(cliente));

        when(clienteService.findAll(isNull(), isNull(), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/clientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].razaoSocial").value("Empresa ABC"));
    }

    @Test
    @DisplayName("GET /api/v1/clientes — filtros search e status aplicados")
    void findAll_withFilters_returns200() throws Exception {
        Page<ClienteResponse> page = new PageImpl<>(List.of());

        when(clienteService.findAll(eq("ABC"), eq(StatusCliente.ATIVO), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/clientes")
                        .param("search", "ABC")
                        .param("status", "ATIVO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/clientes/{id}
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/v1/clientes/{id} — retorna 200 com cliente")
    void findById_returns200() throws Exception {
        ClienteResponse cliente = buildClienteResponse(1L, "Empresa ABC");
        when(clienteService.findById(1L)).thenReturn(cliente);

        mockMvc.perform(get("/api/v1/clientes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.razaoSocial").value("Empresa ABC"));
    }

    // -------------------------------------------------------------------------
    // POST /api/v1/clientes
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("POST /api/v1/clientes — cria cliente e retorna 201")
    void create_returns201() throws Exception {
        ClienteRequest request = ClienteRequest.builder()
                .razaoSocial("Nova Empresa")
                .build();
        ClienteResponse response = buildClienteResponse(2L, "Nova Empresa");

        when(clienteService.create(any(ClienteRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.razaoSocial").value("Nova Empresa"));
    }

    @Test
    @DisplayName("POST /api/v1/clientes — razaoSocial em branco retorna 400")
    void create_blankRazaoSocial_returns400() throws Exception {
        ClienteRequest request = ClienteRequest.builder()
                .razaoSocial("")
                .build();

        mockMvc.perform(post("/api/v1/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // -------------------------------------------------------------------------
    // PUT /api/v1/clientes/{id}
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("PUT /api/v1/clientes/{id} — atualiza cliente e retorna 200")
    void update_returns200() throws Exception {
        ClienteRequest request = ClienteRequest.builder()
                .razaoSocial("Empresa Atualizada")
                .build();
        ClienteResponse response = buildClienteResponse(1L, "Empresa Atualizada");

        when(clienteService.update(eq(1L), any(ClienteRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/clientes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.razaoSocial").value("Empresa Atualizada"));
    }

    // -------------------------------------------------------------------------
    // DELETE /api/v1/clientes/{id}
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("DELETE /api/v1/clientes/{id} — exclui cliente e retorna 204")
    void delete_returns204() throws Exception {
        doNothing().when(clienteService).delete(1L);

        mockMvc.perform(delete("/api/v1/clientes/1"))
                .andExpect(status().isNoContent());
    }

    // -------------------------------------------------------------------------
    // PATCH /api/v1/clientes/{id}/toggle-status
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("PATCH /api/v1/clientes/{id}/toggle-status — alterna status e retorna 200")
    void toggleStatus_returns200() throws Exception {
        ClienteResponse response = buildClienteResponse(1L, "Empresa ABC");
        response.setStatus(StatusCliente.INATIVO);

        when(clienteService.toggleStatus(1L)).thenReturn(response);

        mockMvc.perform(patch("/api/v1/clientes/1/toggle-status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INATIVO"));
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/cep/{cep}
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/v1/cep/{cep} — retorna dados do CEP")
    void buscarCep_returns200() throws Exception {
        ViaCepResponse cepResponse = new ViaCepResponse(
                "01310-100", "Av. Paulista", "", "Bela Vista", "São Paulo", "SP", null);

        when(viaCepService.buscarCep("01310100")).thenReturn(cepResponse);

        mockMvc.perform(get("/api/v1/cep/01310100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cep").value("01310-100"))
                .andExpect(jsonPath("$.logradouro").value("Av. Paulista"));
    }
}
