package com.prediman.crm.service;

import com.prediman.crm.model.Cliente;
import com.prediman.crm.model.Documento;
import com.prediman.crm.model.enums.CategoriaDocumento;
import com.prediman.crm.repository.ClienteRepository;
import com.prediman.crm.repository.DocumentoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Exercita a exportacao contra um PostgreSQL real.
 *
 * <p>Os testes unitarios de {@code ExportServiceTest} mockam os repositories, entao a query
 * montada por Specification nunca chega ao banco. Foi assim que passou despercebido que
 * {@code Sort.Order.asc("dataValidade").nullsLast()} faz o Hibernate lancar
 * {@code UnsupportedOperationException: Applying Null Precedence using Criteria Queries is not
 * yet supported} — o endpoint respondia HTTP 500 com a suite verde.</p>
 */
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@EnabledIf(value = "isDockerAvailable", disabledReason = "Docker não disponível")
class ExportServiceIntegrationTest {

    static boolean isDockerAvailable() {
        try {
            return org.testcontainers.DockerClientFactory.instance().isDockerAvailable();
        } catch (Exception e) {
            return false;
        }
    }

    @Autowired
    private ExportService exportService;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private DocumentoRepository documentoRepository;

    private Cliente cliente;

    @BeforeEach
    void setUp() {
        documentoRepository.deleteAll();
        clienteRepository.deleteAll();

        cliente = clienteRepository.save(Cliente.builder()
                .razaoSocial("Petrobras Distribuidora SA")
                .nomeFantasia("Petrobras SP")
                .build());
    }

    private Documento documento(String nome, LocalDate validade) {
        return documentoRepository.save(Documento.builder()
                .cliente(cliente)
                .nome(nome)
                .categoria(CategoriaDocumento.ALVARA)
                .dataEmissao(LocalDate.of(2024, 3, 15))
                .dataValidade(validade)
                .build());
    }

    @Test
    void exportarDocumentos_comBancoReal_geraCsvSemFalhar() {
        documento("Alvara", LocalDate.now().plusDays(10));

        byte[] csv = exportService.exportarDocumentos(null, null, null, null);
        String conteudo = new String(csv, StandardCharsets.UTF_8);

        assertThat(csv).startsWith((byte) 0xEF, (byte) 0xBB, (byte) 0xBF);
        assertThat(conteudo).contains("Cliente;Documento");
        assertThat(conteudo).contains("Petrobras Distribuidora SA");
        assertThat(conteudo).contains("Alvara");
    }

    @Test
    void exportarDocumentos_ordenaPorValidadeComSemValidadePorUltimo() {
        documento("Sem validade", null);
        documento("Vence depois", LocalDate.now().plusDays(60));
        documento("Vence antes", LocalDate.now().plusDays(5));

        String conteudo = new String(exportService.exportarDocumentos(null, null, null, null),
                StandardCharsets.UTF_8);

        List<String> linhas = conteudo.lines().skip(1).toList();
        assertThat(linhas).hasSize(3);
        assertThat(linhas.get(0)).contains("Vence antes");
        assertThat(linhas.get(1)).contains("Vence depois");
        assertThat(linhas.get(2)).contains("Sem validade");
    }

    @Test
    void exportarDocumentos_aplicaFiltroDeCliente() {
        documento("Alvara", LocalDate.now().plusDays(10));
        Cliente outro = clienteRepository.save(Cliente.builder().razaoSocial("Outro Cliente").build());
        documentoRepository.save(Documento.builder()
                .cliente(outro)
                .nome("Documento do outro")
                .categoria(CategoriaDocumento.OUTRO)
                .build());

        String conteudo = new String(exportService.exportarDocumentos(null, null, null, cliente.getId()),
                StandardCharsets.UTF_8);

        assertThat(conteudo).contains("Alvara");
        assertThat(conteudo).doesNotContain("Documento do outro");
    }

    @Test
    void exportarCobrancas_comBancoReal_geraCsvSemFalhar() {
        byte[] csv = exportService.exportarCobrancas(null, null, null, null);
        String conteudo = new String(csv, StandardCharsets.UTF_8);

        assertThat(csv).startsWith((byte) 0xEF, (byte) 0xBB, (byte) 0xBF);
        assertThat(conteudo).contains("Cliente;Contrato");
    }
}
