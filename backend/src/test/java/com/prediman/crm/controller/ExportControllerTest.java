package com.prediman.crm.controller;

import com.prediman.crm.model.enums.CategoriaDocumento;
import com.prediman.crm.model.enums.StatusCobranca;
import com.prediman.crm.security.JwtAuthenticationFilter;
import com.prediman.crm.security.JwtTokenProvider;
import com.prediman.crm.security.RateLimitFilter;
import com.prediman.crm.security.UserDetailsServiceImpl;
import com.prediman.crm.service.ExportService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.test.web.servlet.MockMvc;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ExportController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("ExportController — testes de unidade")
class ExportControllerTest {

    private static final String CSV_DOCUMENTOS =
            "\uFEFFCliente;Documento\r\nEmpresa Teste Ltda;Alvará\r\n";
    private static final String CSV_COBRANCAS =
            "\uFEFFCliente;Contrato\r\nEmpresa Teste Ltda;Licença XYZ\r\n";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ExportService exportService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @MockBean
    private RateLimitFilter rateLimitFilter;

    // -------------------------------------------------------------------------
    // GET /api/v1/export/documentos
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/v1/export/documentos — retorna CSV com BOM UTF-8 e cabeçalhos de download")
    void exportarDocumentos_returns200ComCsv() throws Exception {
        when(exportService.exportarDocumentos(isNull(), isNull(), isNull(), isNull()))
                .thenReturn(CSV_DOCUMENTOS.getBytes(StandardCharsets.UTF_8));

        byte[] corpo = mockMvc.perform(get("/api/v1/export/documentos"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, containsString("text/csv")))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, containsStringIgnoringCase("charset=UTF-8")))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
                        startsWith("attachment; filename=\"documentos_")))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, endsWith(".csv\"")))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS,
                        HttpHeaders.CONTENT_DISPOSITION))
                .andReturn().getResponse().getContentAsByteArray();

        assertThat(corpo).isEqualTo(CSV_DOCUMENTOS.getBytes(StandardCharsets.UTF_8));
        assertThat(corpo[0]).isEqualTo((byte) 0xEF);
        assertThat(corpo[1]).isEqualTo((byte) 0xBB);
        assertThat(corpo[2]).isEqualTo((byte) 0xBF);
    }

    @Test
    @DisplayName("GET /api/v1/export/documentos — repassa os mesmos filtros da listagem de documentos")
    void exportarDocumentos_repassaFiltros() throws Exception {
        when(exportService.exportarDocumentos(eq("Alvará"), eq(CategoriaDocumento.LICENCA),
                eq("A_VENCER"), eq(10L)))
                .thenReturn(CSV_DOCUMENTOS.getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(get("/api/v1/export/documentos")
                        .param("search", "Alvará")
                        .param("categoria", "LICENCA")
                        .param("status", "A_VENCER")
                        .param("clienteId", "10"))
                .andExpect(status().isOk());

        verify(exportService).exportarDocumentos("Alvará", CategoriaDocumento.LICENCA, "A_VENCER", 10L);
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/export/cobrancas
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/v1/export/cobrancas — retorna CSV com cabeçalhos de download")
    void exportarCobrancas_returns200ComCsv() throws Exception {
        when(exportService.exportarCobrancas(isNull(), isNull(), isNull(), isNull()))
                .thenReturn(CSV_COBRANCAS.getBytes(StandardCharsets.UTF_8));

        byte[] corpo = mockMvc.perform(get("/api/v1/export/cobrancas"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, containsString("text/csv")))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
                        startsWith("attachment; filename=\"cobrancas_")))
                .andReturn().getResponse().getContentAsByteArray();

        assertThat(corpo).isEqualTo(CSV_COBRANCAS.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("GET /api/v1/export/cobrancas — repassa os mesmos filtros da listagem financeira")
    void exportarCobrancas_repassaFiltros() throws Exception {
        when(exportService.exportarCobrancas(eq(20L), eq(StatusCobranca.PENDENTE), eq(2), eq(2024)))
                .thenReturn(CSV_COBRANCAS.getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(get("/api/v1/export/cobrancas")
                        .param("contratoId", "20")
                        .param("status", "PENDENTE")
                        .param("month", "2")
                        .param("year", "2024"))
                .andExpect(status().isOk());

        verify(exportService).exportarCobrancas(20L, StatusCobranca.PENDENTE, 2, 2024);
    }

    // -------------------------------------------------------------------------
    // Autorização
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("exportarDocumentos exige perfil ADMIN ou USUARIO")
    void exportarDocumentos_exigePerfilAdminOuUsuario() throws Exception {
        Method metodo = ExportController.class.getMethod("exportarDocumentos",
                String.class, CategoriaDocumento.class, String.class, Long.class);

        PreAuthorize preAuthorize = metodo.getAnnotation(PreAuthorize.class);

        assertThat(preAuthorize).isNotNull();
        assertThat(preAuthorize.value()).isEqualTo("hasAnyRole('ADMIN','USUARIO')");
    }

    @Test
    @DisplayName("exportarCobrancas exige perfil ADMIN ou USUARIO")
    void exportarCobrancas_exigePerfilAdminOuUsuario() throws Exception {
        Method metodo = ExportController.class.getMethod("exportarCobrancas",
                Long.class, StatusCobranca.class, Integer.class, Integer.class);

        PreAuthorize preAuthorize = metodo.getAnnotation(PreAuthorize.class);

        assertThat(preAuthorize).isNotNull();
        assertThat(preAuthorize.value()).isEqualTo("hasAnyRole('ADMIN','USUARIO')");
    }
}
