package com.prediman.crm.service;

import com.prediman.crm.dto.ContratoResponse;
import com.prediman.crm.model.Cobranca;
import com.prediman.crm.model.Cliente;
import com.prediman.crm.model.Contrato;
import com.prediman.crm.model.enums.Periodicidade;
import com.prediman.crm.model.enums.StatusCobranca;
import com.prediman.crm.model.enums.StatusContrato;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ContratoMapper")
class ContratoMapperTest {

    private ContratoMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ContratoMapper(new CobrancaMapper());
    }

    @Test
    @DisplayName("toResponse mapeia todos os campos do Contrato incluindo cobrancas aninhadas")
    void toResponse_mapeiaTodasOsCamposComCobrancas() {
        LocalDateTime now = LocalDateTime.of(2024, 4, 1, 12, 0);
        LocalDate inicio = LocalDate.of(2024, 1, 1);
        LocalDate fim = LocalDate.of(2024, 12, 31);

        Cliente cliente = Cliente.builder()
                .id(10L)
                .razaoSocial("Cliente Teste Ltda")
                .build();

        Cobranca cobranca = Cobranca.builder()
                .id(100L)
                .valorEsperado(new BigDecimal("1500.00"))
                .valorRecebido(new BigDecimal("1500.00"))
                .dataVencimento(LocalDate.of(2024, 1, 10))
                .dataPagamento(LocalDate.of(2024, 1, 8))
                .formaPagamento("PIX")
                .comprovanteDriveId("comprovante-drive-id")
                .status(StatusCobranca.PAGO)
                .createdAt(now)
                .updatedAt(now)
                .build();

        Contrato contrato = Contrato.builder()
                .id(1L)
                .descricao("Licenca de Software Anual")
                .valor(new BigDecimal("18000.00"))
                .periodicidade(Periodicidade.ANUAL)
                .dataInicio(inicio)
                .dataFim(fim)
                .status(StatusContrato.ATIVO)
                .observacoes("Renovacao automatica")
                .createdAt(now)
                .updatedAt(now)
                .cliente(cliente)
                .cobrancas(new java.util.ArrayList<>(List.of(cobranca)))
                .build();

        // cobranca precisa conhecer o contrato para getStatusCalculado via mapper
        cobranca.setContrato(contrato);

        ContratoResponse response = mapper.toResponse(contrato);

        assertAll("campos escalares do contrato",
                () -> assertEquals(1L, response.getId()),
                () -> assertEquals("Licenca de Software Anual", response.getDescricao()),
                () -> assertEquals(new BigDecimal("18000.00"), response.getValor()),
                () -> assertEquals(Periodicidade.ANUAL, response.getPeriodicidade()),
                () -> assertEquals(inicio, response.getDataInicio()),
                () -> assertEquals(fim, response.getDataFim()),
                () -> assertEquals(StatusContrato.ATIVO, response.getStatus()),
                () -> assertEquals("Renovacao automatica", response.getObservacoes()),
                () -> assertEquals(now, response.getCreatedAt()),
                () -> assertEquals(now, response.getUpdatedAt())
        );

        assertAll("referencia ao cliente",
                () -> assertEquals(10L, response.getClienteId()),
                () -> assertEquals("Cliente Teste Ltda", response.getClienteNome())
        );

        assertAll("cobrancas aninhadas",
                () -> assertEquals(1, response.getCobrancas().size()),
                () -> assertEquals(100L, response.getCobrancas().get(0).getId()),
                () -> assertEquals(new BigDecimal("1500.00"), response.getCobrancas().get(0).getValorEsperado()),
                () -> assertEquals(LocalDate.of(2024, 1, 8), response.getCobrancas().get(0).getDataPagamento()),
                () -> assertEquals(StatusCobranca.PAGO, response.getCobrancas().get(0).getStatus())
        );
    }

    @Test
    @DisplayName("toResponse mapeia contrato sem cobrancas — lista vazia sem excecao")
    void toResponse_semCobrancas() {
        Cliente cliente = Cliente.builder()
                .id(2L)
                .razaoSocial("Cliente Sem Cobrancas")
                .build();

        Contrato contrato = Contrato.builder()
                .id(2L)
                .descricao("Contrato Basico")
                .valor(new BigDecimal("500.00"))
                .periodicidade(Periodicidade.MENSAL)
                .dataInicio(LocalDate.of(2024, 6, 1))
                .status(StatusContrato.ATIVO)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .cliente(cliente)
                .cobrancas(new java.util.ArrayList<>())
                .build();

        ContratoResponse response = mapper.toResponse(contrato);

        assertNotNull(response.getCobrancas());
        assertTrue(response.getCobrancas().isEmpty());
    }
}
