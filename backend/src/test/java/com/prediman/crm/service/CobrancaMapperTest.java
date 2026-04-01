package com.prediman.crm.service;

import com.prediman.crm.dto.CobrancaResponse;
import com.prediman.crm.model.Cobranca;
import com.prediman.crm.model.Contrato;
import com.prediman.crm.model.enums.StatusCobranca;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CobrancaMapper")
class CobrancaMapperTest {

    private CobrancaMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new CobrancaMapper();
    }

    private Contrato contratoComId(Long id) {
        return Contrato.builder()
                .id(id)
                .descricao("Contrato " + id)
                .valor(BigDecimal.ZERO)
                .dataInicio(LocalDate.of(2024, 1, 1))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("toResponse mapeia todos os campos de Cobranca paga incluindo statusCalculado PAGO")
    void toResponse_mapeiaCobrancaPaga() {
        LocalDateTime now = LocalDateTime.of(2024, 2, 15, 10, 0);
        Contrato contrato = contratoComId(7L);

        Cobranca cobranca = Cobranca.builder()
                .id(200L)
                .valorEsperado(new BigDecimal("999.90"))
                .valorRecebido(new BigDecimal("999.90"))
                .dataVencimento(LocalDate.of(2024, 2, 10))
                .dataPagamento(LocalDate.of(2024, 2, 8))
                .formaPagamento("Boleto")
                .comprovanteDriveId("comprovante-001")
                .status(StatusCobranca.PAGO)
                .createdAt(now)
                .updatedAt(now)
                .contrato(contrato)
                .build();

        CobrancaResponse response = mapper.toResponse(cobranca);

        assertAll("campos escalares",
                () -> assertEquals(200L, response.getId()),
                () -> assertEquals(new BigDecimal("999.90"), response.getValorEsperado()),
                () -> assertEquals(new BigDecimal("999.90"), response.getValorRecebido()),
                () -> assertEquals(LocalDate.of(2024, 2, 10), response.getDataVencimento()),
                () -> assertEquals(LocalDate.of(2024, 2, 8), response.getDataPagamento()),
                () -> assertEquals("Boleto", response.getFormaPagamento()),
                () -> assertEquals("comprovante-001", response.getComprovanteDriveId()),
                () -> assertEquals(StatusCobranca.PAGO, response.getStatus()),
                () -> assertEquals(now, response.getCreatedAt()),
                () -> assertEquals(now, response.getUpdatedAt())
        );

        assertAll("referencia ao contrato e statusCalculado",
                () -> assertEquals(7L, response.getContratoId()),
                () -> assertEquals(StatusCobranca.PAGO, response.getStatusCalculado())
        );
    }

    @Test
    @DisplayName("toResponse mapeia statusCalculado VENCIDO para cobranca vencida sem pagamento")
    void toResponse_statusCalculadoVencido() {
        Contrato contrato = contratoComId(8L);

        Cobranca cobranca = Cobranca.builder()
                .id(201L)
                .valorEsperado(new BigDecimal("500.00"))
                .dataVencimento(LocalDate.now().minusDays(5))
                .dataPagamento(null)
                .valorRecebido(null)
                .status(StatusCobranca.PENDENTE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .contrato(contrato)
                .build();

        CobrancaResponse response = mapper.toResponse(cobranca);

        assertEquals(StatusCobranca.VENCIDO, response.getStatusCalculado());
    }

    @Test
    @DisplayName("toResponse mapeia statusCalculado PENDENTE para cobranca futura sem pagamento")
    void toResponse_statusCalculadoPendente() {
        Contrato contrato = contratoComId(9L);

        Cobranca cobranca = Cobranca.builder()
                .id(202L)
                .valorEsperado(new BigDecimal("750.00"))
                .dataVencimento(LocalDate.now().plusDays(10))
                .dataPagamento(null)
                .valorRecebido(null)
                .status(StatusCobranca.PENDENTE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .contrato(contrato)
                .build();

        CobrancaResponse response = mapper.toResponse(cobranca);

        assertEquals(StatusCobranca.PENDENTE, response.getStatusCalculado());
    }
}
