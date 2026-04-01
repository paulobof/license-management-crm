package com.prediman.crm.model;

import com.prediman.crm.model.enums.StatusCobranca;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Cobranca")
class CobrancaTest {

    @Test
    @DisplayName("retorna PAGO quando dataPagamento e valorRecebido estao preenchidos")
    void getStatusCalculado_pago() {
        Cobranca cobranca = Cobranca.builder()
                .valorEsperado(new BigDecimal("1000.00"))
                .valorRecebido(new BigDecimal("1000.00"))
                .dataVencimento(LocalDate.now().minusDays(5))
                .dataPagamento(LocalDate.now().minusDays(7))
                .build();

        assertEquals(StatusCobranca.PAGO, cobranca.getStatusCalculado());
    }

    @Test
    @DisplayName("retorna PAGO mesmo quando dataVencimento ja passou, se dataPagamento foi informado")
    void getStatusCalculado_pagoMesmoVencido() {
        Cobranca cobranca = Cobranca.builder()
                .valorEsperado(new BigDecimal("200.00"))
                .valorRecebido(new BigDecimal("200.00"))
                .dataVencimento(LocalDate.now().minusDays(30))
                .dataPagamento(LocalDate.now().minusDays(1))
                .build();

        assertEquals(StatusCobranca.PAGO, cobranca.getStatusCalculado());
    }

    @Test
    @DisplayName("retorna VENCIDO quando dataVencimento e passada e nao ha pagamento")
    void getStatusCalculado_vencido() {
        Cobranca cobranca = Cobranca.builder()
                .valorEsperado(new BigDecimal("500.00"))
                .dataVencimento(LocalDate.now().minusDays(1))
                .dataPagamento(null)
                .valorRecebido(null)
                .build();

        assertEquals(StatusCobranca.VENCIDO, cobranca.getStatusCalculado());
    }

    @Test
    @DisplayName("retorna VENCIDO quando dataVencimento e ontem e nao ha pagamento")
    void getStatusCalculado_vencidoOntem() {
        Cobranca cobranca = Cobranca.builder()
                .valorEsperado(new BigDecimal("300.00"))
                .dataVencimento(LocalDate.now().minusDays(1))
                .dataPagamento(null)
                .valorRecebido(null)
                .build();

        assertEquals(StatusCobranca.VENCIDO, cobranca.getStatusCalculado());
    }

    @Test
    @DisplayName("retorna PENDENTE quando dataVencimento e no futuro e nao ha pagamento")
    void getStatusCalculado_pendente() {
        Cobranca cobranca = Cobranca.builder()
                .valorEsperado(new BigDecimal("750.00"))
                .dataVencimento(LocalDate.now().plusDays(10))
                .dataPagamento(null)
                .valorRecebido(null)
                .build();

        assertEquals(StatusCobranca.PENDENTE, cobranca.getStatusCalculado());
    }

    @Test
    @DisplayName("retorna PENDENTE quando dataVencimento e hoje e nao ha pagamento")
    void getStatusCalculado_pendenteHoje() {
        Cobranca cobranca = Cobranca.builder()
                .valorEsperado(new BigDecimal("400.00"))
                .dataVencimento(LocalDate.now())
                .dataPagamento(null)
                .valorRecebido(null)
                .build();

        assertEquals(StatusCobranca.PENDENTE, cobranca.getStatusCalculado());
    }

    @Test
    @DisplayName("retorna PENDENTE quando dataPagamento esta preenchido mas valorRecebido e null")
    void getStatusCalculado_pagamentoSemValorRecebidoEhPendente() {
        Cobranca cobranca = Cobranca.builder()
                .valorEsperado(new BigDecimal("600.00"))
                .valorRecebido(null)
                .dataVencimento(LocalDate.now().plusDays(5))
                .dataPagamento(LocalDate.now())
                .build();

        assertEquals(StatusCobranca.PENDENTE, cobranca.getStatusCalculado());
    }

    // ---- builder / getters / setters / lifecycle coverage ----

    @Test
    @DisplayName("builder cria instancia com todos os campos preenchidos")
    void builder_criaInstanciaCompleta() {
        LocalDateTime now = LocalDateTime.now();
        Contrato contrato = Contrato.builder().id(1L).descricao("C").valor(BigDecimal.TEN).dataInicio(LocalDate.now()).build();

        Cobranca cobranca = Cobranca.builder()
                .id(1L)
                .contrato(contrato)
                .valorEsperado(new BigDecimal("1500.00"))
                .valorRecebido(new BigDecimal("1500.00"))
                .dataVencimento(LocalDate.of(2024, 6, 1))
                .dataPagamento(LocalDate.of(2024, 5, 30))
                .formaPagamento("PIX")
                .comprovanteDriveId("comprovante-id")
                .status(StatusCobranca.PAGO)
                .createdAt(now)
                .updatedAt(now)
                .build();

        assertThat(cobranca.getId()).isEqualTo(1L);
        assertThat(cobranca.getContrato()).isEqualTo(contrato);
        assertThat(cobranca.getValorEsperado()).isEqualByComparingTo("1500.00");
        assertThat(cobranca.getValorRecebido()).isEqualByComparingTo("1500.00");
        assertThat(cobranca.getDataVencimento()).isEqualTo(LocalDate.of(2024, 6, 1));
        assertThat(cobranca.getDataPagamento()).isEqualTo(LocalDate.of(2024, 5, 30));
        assertThat(cobranca.getFormaPagamento()).isEqualTo("PIX");
        assertThat(cobranca.getComprovanteDriveId()).isEqualTo("comprovante-id");
        assertThat(cobranca.getStatus()).isEqualTo(StatusCobranca.PAGO);
        assertThat(cobranca.getCreatedAt()).isEqualTo(now);
        assertThat(cobranca.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("builder usa status PENDENTE como default")
    void builder_usaDefaultStatus() {
        Cobranca cobranca = Cobranca.builder()
                .valorEsperado(BigDecimal.TEN)
                .dataVencimento(LocalDate.now())
                .build();

        assertThat(cobranca.getStatus()).isEqualTo(StatusCobranca.PENDENTE);
    }

    @Test
    @DisplayName("setters alteram todos os campos corretamente")
    void setters_alteramValores() {
        Cobranca cobranca = new Cobranca();
        LocalDateTime now = LocalDateTime.now();

        cobranca.setId(10L);
        cobranca.setValorEsperado(new BigDecimal("200.00"));
        cobranca.setValorRecebido(new BigDecimal("200.00"));
        cobranca.setDataVencimento(LocalDate.of(2024, 3, 15));
        cobranca.setDataPagamento(LocalDate.of(2024, 3, 10));
        cobranca.setFormaPagamento("TED");
        cobranca.setComprovanteDriveId("drive-comp");
        cobranca.setStatus(StatusCobranca.VENCIDO);
        cobranca.setCreatedAt(now);
        cobranca.setUpdatedAt(now);

        assertThat(cobranca.getId()).isEqualTo(10L);
        assertThat(cobranca.getValorEsperado()).isEqualByComparingTo("200.00");
        assertThat(cobranca.getFormaPagamento()).isEqualTo("TED");
        assertThat(cobranca.getStatus()).isEqualTo(StatusCobranca.VENCIDO);
    }

    @Test
    @DisplayName("equals retorna true para cobrancas com todos os campos iguais")
    void equals_mesmosValores_retornaTrue() {
        LocalDate venc = LocalDate.of(2025, 1, 1);
        Cobranca c1 = Cobranca.builder().id(1L).valorEsperado(BigDecimal.ONE).dataVencimento(venc).build();
        Cobranca c2 = Cobranca.builder().id(1L).valorEsperado(BigDecimal.ONE).dataVencimento(venc).build();

        assertThat(c1).isEqualTo(c2);
    }

    @Test
    @DisplayName("equals retorna false para cobrancas com valorEsperado diferente")
    void equals_valorDistinto_retornaFalse() {
        LocalDate venc = LocalDate.of(2025, 1, 1);
        Cobranca c1 = Cobranca.builder().id(1L).valorEsperado(BigDecimal.ONE).dataVencimento(venc).build();
        Cobranca c2 = Cobranca.builder().id(1L).valorEsperado(BigDecimal.TEN).dataVencimento(venc).build();

        assertThat(c1).isNotEqualTo(c2);
    }

    @Test
    @DisplayName("hashCode e consistente para cobrancas com mesmos campos")
    void hashCode_consistente() {
        LocalDate venc = LocalDate.of(2025, 6, 1);
        Cobranca c1 = Cobranca.builder().id(6L).valorEsperado(BigDecimal.ONE).dataVencimento(venc).build();
        Cobranca c2 = Cobranca.builder().id(6L).valorEsperado(BigDecimal.ONE).dataVencimento(venc).build();

        assertThat(c1.hashCode()).isEqualTo(c2.hashCode());
    }

    @Test
    @DisplayName("toString retorna string nao vazia")
    void toString_retornaStringNaoVazia() {
        Cobranca cobranca = Cobranca.builder()
                .id(1L)
                .valorEsperado(new BigDecimal("999.00"))
                .dataVencimento(LocalDate.now())
                .build();

        assertThat(cobranca.toString()).isNotBlank();
    }

    @Test
    @DisplayName("noArgsConstructor cria instancia vazia")
    void noArgsConstructor_criaInstanciaVazia() {
        Cobranca cobranca = new Cobranca();

        assertThat(cobranca.getId()).isNull();
        assertThat(cobranca.getValorEsperado()).isNull();
    }

    @Test
    @DisplayName("allArgsConstructor preenche todos os campos")
    void allArgsConstructor_preencheAllFields() {
        LocalDateTime now = LocalDateTime.now();
        Cobranca cobranca = new Cobranca(
                1L, null,
                new BigDecimal("500.00"), new BigDecimal("500.00"),
                LocalDate.now(), LocalDate.now().minusDays(1),
                "BOLETO", "comp-id",
                StatusCobranca.PAGO,
                now, now
        );

        assertThat(cobranca.getId()).isEqualTo(1L);
        assertThat(cobranca.getFormaPagamento()).isEqualTo("BOLETO");
        assertThat(cobranca.getStatus()).isEqualTo(StatusCobranca.PAGO);
    }

    @Test
    @DisplayName("onCreate define createdAt e updatedAt quando nulos")
    void onCreate_defineTimestamps() throws Exception {
        Cobranca cobranca = new Cobranca();

        var method = Cobranca.class.getDeclaredMethod("onCreate");
        method.setAccessible(true);
        method.invoke(cobranca);

        assertThat(cobranca.getCreatedAt()).isNotNull();
        assertThat(cobranca.getUpdatedAt()).isNotNull();
        assertThat(cobranca.getStatus()).isEqualTo(StatusCobranca.PENDENTE);
    }

    @Test
    @DisplayName("onCreate nao sobrescreve createdAt e status quando ja definidos")
    void onCreate_naoSobreescreveValoresExistentes() throws Exception {
        LocalDateTime original = LocalDateTime.of(2023, 3, 3, 3, 3);
        Cobranca cobranca = new Cobranca();
        cobranca.setCreatedAt(original);
        cobranca.setStatus(StatusCobranca.VENCIDO);

        var method = Cobranca.class.getDeclaredMethod("onCreate");
        method.setAccessible(true);
        method.invoke(cobranca);

        assertThat(cobranca.getCreatedAt()).isEqualTo(original);
        assertThat(cobranca.getStatus()).isEqualTo(StatusCobranca.VENCIDO);
    }

    @Test
    @DisplayName("onUpdate atualiza updatedAt")
    void onUpdate_atualizaUpdatedAt() throws Exception {
        Cobranca cobranca = new Cobranca();
        LocalDateTime before = LocalDateTime.now().minusDays(1);
        cobranca.setUpdatedAt(before);

        var method = Cobranca.class.getDeclaredMethod("onUpdate");
        method.setAccessible(true);
        method.invoke(cobranca);

        assertThat(cobranca.getUpdatedAt()).isAfter(before);
    }

    @Test
    @DisplayName("getStatusCalculado retorna PENDENTE quando dataPagamento e null e dataVencimento nulo")
    void getStatusCalculado_pendenteQuandoVencimentoNulo() {
        Cobranca cobranca = Cobranca.builder()
                .valorEsperado(BigDecimal.TEN)
                .dataPagamento(null)
                .valorRecebido(null)
                .dataVencimento(null)
                .build();

        assertThat(cobranca.getStatusCalculado()).isEqualTo(StatusCobranca.PENDENTE);
    }

    @Test
    @DisplayName("onCreate define status PENDENTE quando nulo explicitamente")
    void onCreate_defineStatusQuandoNulo() throws Exception {
        Cobranca cobranca = new Cobranca();
        // no-args constructor sets status via @Builder.Default — force null
        cobranca.setStatus(null);
        assertThat(cobranca.getStatus()).isNull();

        var method = Cobranca.class.getDeclaredMethod("onCreate");
        method.setAccessible(true);
        method.invoke(cobranca);

        assertThat(cobranca.getStatus()).isEqualTo(StatusCobranca.PENDENTE);
    }

    @Test
    @DisplayName("equals retorna false quando comparado com null")
    void equals_comNull_retornaFalse() {
        Cobranca c = Cobranca.builder().id(1L).valorEsperado(BigDecimal.ONE).dataVencimento(LocalDate.now()).build();
        assertThat(c).isNotEqualTo(null);
    }

    @Test
    @DisplayName("equals retorna true quando comparado consigo mesmo")
    void equals_comSiMesmo_retornaTrue() {
        Cobranca c = Cobranca.builder().id(1L).valorEsperado(BigDecimal.ONE).dataVencimento(LocalDate.now()).build();
        assertThat(c).isEqualTo(c);
    }
}
