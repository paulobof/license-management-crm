package com.prediman.crm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prediman.crm.dto.DashboardSummaryResponse;
import com.prediman.crm.dto.DocumentoRequest;
import com.prediman.crm.dto.DocumentoResponse;
import com.prediman.crm.model.enums.CategoriaDocumento;
import com.prediman.crm.model.enums.StatusDocumento;
import com.prediman.crm.security.JwtAuthenticationFilter;
import com.prediman.crm.security.JwtTokenProvider;
import com.prediman.crm.security.RateLimitFilter;
import com.prediman.crm.security.UserDetailsServiceImpl;
import com.prediman.crm.service.DocumentoService;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DocumentoController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("DocumentoController — testes de unidade")
class DocumentoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DocumentoService documentoService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @MockBean
    private RateLimitFilter rateLimitFilter;

    private DocumentoResponse buildDocumentoResponse(Long id, String nome) {
        return DocumentoResponse.builder()
                .id(id)
                .nome(nome)
                .categoria(CategoriaDocumento.LICENCA)
                .clienteId(10L)
                .clienteNome("Empresa XYZ")
                .statusCalculado(StatusDocumento.VALIDO)
                .dataValidade(LocalDate.now().plusDays(30))
                .build();
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/documentos
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/v1/documentos — retorna página de documentos")
    void findAll_returns200() throws Exception {
        DocumentoResponse doc = buildDocumentoResponse(1L, "Alvará");
        Page<DocumentoResponse> page = new PageImpl<>(List.of(doc));

        when(documentoService.findAll(isNull(), isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/documentos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].nome").value("Alvará"));
    }

    @Test
    @DisplayName("GET /api/v1/documentos — filtros aplicados")
    void findAll_withFilters_returns200() throws Exception {
        Page<DocumentoResponse> page = new PageImpl<>(List.of());

        when(documentoService.findAll(eq("Alvará"), eq(CategoriaDocumento.LICENCA), eq("VIGENTE"), eq(10L), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/documentos")
                        .param("search", "Alvará")
                        .param("categoria", "LICENCA")
                        .param("status", "VIGENTE")
                        .param("clienteId", "10"))
                .andExpect(status().isOk());
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/documentos/{id}
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/v1/documentos/{id} — retorna 200 com documento")
    void findById_returns200() throws Exception {
        DocumentoResponse doc = buildDocumentoResponse(1L, "Alvará");
        when(documentoService.findById(1L)).thenReturn(doc);

        mockMvc.perform(get("/api/v1/documentos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Alvará"));
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/clientes/{clienteId}/documentos
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/v1/clientes/{clienteId}/documentos — retorna lista de documentos do cliente")
    void findByCliente_returns200() throws Exception {
        DocumentoResponse doc = buildDocumentoResponse(1L, "Alvará");
        when(documentoService.findByClienteId(10L)).thenReturn(List.of(doc));

        mockMvc.perform(get("/api/v1/clientes/10/documentos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].clienteId").value(10));
    }

    // -------------------------------------------------------------------------
    // POST /api/v1/documentos
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("POST /api/v1/documentos — cria documento e retorna 201")
    void create_returns201() throws Exception {
        DocumentoRequest request = DocumentoRequest.builder()
                .nome("Alvará Municipal")
                .clienteId(10L)
                .build();
        DocumentoResponse response = buildDocumentoResponse(1L, "Alvará Municipal");

        when(documentoService.create(any(DocumentoRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/documentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Alvará Municipal"));
    }

    @Test
    @DisplayName("POST /api/v1/documentos — nome em branco retorna 400")
    void create_blankNome_returns400() throws Exception {
        DocumentoRequest request = DocumentoRequest.builder()
                .nome("")
                .clienteId(10L)
                .build();

        mockMvc.perform(post("/api/v1/documentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/documentos — clienteId nulo retorna 400")
    void create_nullClienteId_returns400() throws Exception {
        DocumentoRequest request = DocumentoRequest.builder()
                .nome("Alvará")
                .clienteId(null)
                .build();

        mockMvc.perform(post("/api/v1/documentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // -------------------------------------------------------------------------
    // PUT /api/v1/documentos/{id}
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("PUT /api/v1/documentos/{id} — atualiza e retorna 200")
    void update_returns200() throws Exception {
        DocumentoRequest request = DocumentoRequest.builder()
                .nome("Alvará Atualizado")
                .clienteId(10L)
                .build();
        DocumentoResponse response = buildDocumentoResponse(1L, "Alvará Atualizado");

        when(documentoService.update(eq(1L), any(DocumentoRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/documentos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Alvará Atualizado"));
    }

    // -------------------------------------------------------------------------
    // DELETE /api/v1/documentos/{id}
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("DELETE /api/v1/documentos/{id} — exclui e retorna 204")
    void delete_returns204() throws Exception {
        doNothing().when(documentoService).delete(1L);

        mockMvc.perform(delete("/api/v1/documentos/1"))
                .andExpect(status().isNoContent());
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/dashboard/summary
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/v1/dashboard/summary — retorna resumo do dashboard")
    void getDashboardSummary_returns200() throws Exception {
        DashboardSummaryResponse summary = DashboardSummaryResponse.builder()
                .totalClientes(100)
                .clientesAtivos(80)
                .documentosAVencer(5)
                .documentosVencidos(2)
                .build();

        when(documentoService.getDashboardSummary()).thenReturn(summary);

        mockMvc.perform(get("/api/v1/dashboard/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalClientes").value(100))
                .andExpect(jsonPath("$.clientesAtivos").value(80))
                .andExpect(jsonPath("$.documentosAVencer").value(5))
                .andExpect(jsonPath("$.documentosVencidos").value(2));
    }

    // -------------------------------------------------------------------------
    // POST /api/v1/documentos/upload
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("POST /api/v1/documentos/upload — delega ao servico e retorna 201")
    void upload_returns201() throws Exception {
        DocumentoRequest request = DocumentoRequest.builder()
                .nome("Alvará Municipal")
                .clienteId(10L)
                .dataEmissao(LocalDate.of(2024, 3, 5))
                .build();
        DocumentoResponse response = buildDocumentoResponse(1L, "Alvará Municipal");

        MockMultipartFile arquivo = new MockMultipartFile(
                "file", "alvara.pdf", MediaType.APPLICATION_PDF_VALUE, "conteudo".getBytes());
        MockMultipartFile dados = new MockMultipartFile(
                "data", "", MediaType.APPLICATION_JSON_VALUE, objectMapper.writeValueAsBytes(request));

        when(documentoService.upload(any(DocumentoRequest.class), any(MultipartFile.class)))
                .thenReturn(response);

        mockMvc.perform(multipart("/api/v1/documentos/upload").file(arquivo).file(dados))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Alvará Municipal"));

        verify(documentoService).upload(any(DocumentoRequest.class), any(MultipartFile.class));
    }

    @Test
    @DisplayName("POST /api/v1/documentos/upload — metadados invalidos retorna 400")
    void upload_dadosInvalidos_returns400() throws Exception {
        DocumentoRequest request = DocumentoRequest.builder()
                .nome("")
                .clienteId(null)
                .build();

        MockMultipartFile arquivo = new MockMultipartFile(
                "file", "alvara.pdf", MediaType.APPLICATION_PDF_VALUE, "conteudo".getBytes());
        MockMultipartFile dados = new MockMultipartFile(
                "data", "", MediaType.APPLICATION_JSON_VALUE, objectMapper.writeValueAsBytes(request));

        mockMvc.perform(multipart("/api/v1/documentos/upload").file(arquivo).file(dados))
                .andExpect(status().isBadRequest());
    }

    // -------------------------------------------------------------------------
    // Politica de papeis
    // -------------------------------------------------------------------------

    private String preAuthorizeDe(String metodo, Class<?>... parametros) throws NoSuchMethodException {
        Method method = DocumentoController.class.getMethod(metodo, parametros);
        PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);
        return preAuthorize == null ? null : preAuthorize.value();
    }

    @Test
    @DisplayName("Escrita de documentos liberada para ADMIN e USUARIO")
    void escrita_liberadaParaAdminEUsuario() throws Exception {
        assertThat(preAuthorizeDe("create", DocumentoRequest.class))
                .isEqualTo("hasAnyRole('ADMIN','USUARIO')");
        assertThat(preAuthorizeDe("update", Long.class, DocumentoRequest.class))
                .isEqualTo("hasAnyRole('ADMIN','USUARIO')");
        assertThat(preAuthorizeDe("upload", MultipartFile.class, DocumentoRequest.class))
                .isEqualTo("hasAnyRole('ADMIN','USUARIO')");
    }

    @Test
    @DisplayName("Exclusao de documentos restrita a ADMIN")
    void delete_restritoAdmin() throws Exception {
        assertThat(preAuthorizeDe("delete", Long.class)).isEqualTo("hasRole('ADMIN')");
    }

    @Test
    @DisplayName("Leitura de documentos nao exige papel especifico")
    void leitura_semRestricaoDePapel() throws Exception {
        assertThat(preAuthorizeDe("findById", Long.class)).isNull();
        assertThat(preAuthorizeDe("findByCliente", Long.class)).isNull();
    }
}
