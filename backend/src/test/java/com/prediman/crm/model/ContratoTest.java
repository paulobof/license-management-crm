package com.prediman.crm.model;

import com.prediman.crm.model.enums.Periodicidade;
import com.prediman.crm.model.enums.StatusContrato;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Contrato — cobertura de getters, setters, builder, equals, hashCode e toString")
class ContratoTest {

    @Test
    @DisplayName("builder cria instancia com todos os campos preenchidos")
    void builder_criaInstanciaCompleta() {
        LocalDateTime now = LocalDateTime.now();
        Cliente cliente = Cliente.builder().id(1L).razaoSocial("Cliente Ref").build();

        Contrato contrato = Contrato.builder()
                .id(1L)
                .cliente(cliente)
                .descricao("Contrato de suporte anual")
                .valor(new BigDecimal("12000.00"))
                .periodicidade(Periodicidade.ANUAL)
                .dataInicio(LocalDate.of(2024, 1, 1))
                .dataFim(LocalDate.of(2025, 1, 1))
                .status(StatusContrato.ATIVO)
                .observacoes("Observacoes de teste")
                .createdAt(now)
                .updatedAt(now)
                .cobrancas(new ArrayList<>())
                .build();

        assertThat(contrato.getId()).isEqualTo(1L);
        assertThat(contrato.getCliente()).isEqualTo(cliente);
        assertThat(contrato.getDescricao()).isEqualTo("Contrato de suporte anual");
        assertThat(contrato.getValor()).isEqualByComparingTo("12000.00");
        assertThat(contrato.getPeriodicidade()).isEqualTo(Periodicidade.ANUAL);
        assertThat(contrato.getDataInicio()).isEqualTo(LocalDate.of(2024, 1, 1));
        assertThat(contrato.getDataFim()).isEqualTo(LocalDate.of(2025, 1, 1));
        assertThat(contrato.getStatus()).isEqualTo(StatusContrato.ATIVO);
        assertThat(contrato.getObservacoes()).isEqualTo("Observacoes de teste");
        assertThat(contrato.getCreatedAt()).isEqualTo(now);
        assertThat(contrato.getUpdatedAt()).isEqualTo(now);
        assertThat(contrato.getCobrancas()).isEmpty();
    }

    @Test
    @DisplayName("builder usa defaults para periodicidade e status quando nao informados")
    void builder_usaDefaults() {
        Contrato contrato = Contrato.builder()
                .descricao("Contrato")
                .valor(BigDecimal.TEN)
                .dataInicio(LocalDate.now())
                .build();

        assertThat(contrato.getPeriodicidade()).isEqualTo(Periodicidade.MENSAL);
        assertThat(contrato.getStatus()).isEqualTo(StatusContrato.ATIVO);
        assertThat(contrato.getCobrancas()).isNotNull();
    }

    @Test
    @DisplayName("setters alteram os valores dos campos corretamente")
    void setters_alteramValores() {
        Contrato contrato = new Contrato();
        LocalDateTime now = LocalDateTime.now();

        contrato.setId(5L);
        contrato.setDescricao("Nova descricao");
        contrato.setValor(new BigDecimal("999.99"));
        contrato.setPeriodicidade(Periodicidade.TRIMESTRAL);
        contrato.setDataInicio(LocalDate.of(2023, 3, 1));
        contrato.setDataFim(LocalDate.of(2024, 3, 1));
        contrato.setStatus(StatusContrato.ENCERRADO);
        contrato.setObservacoes("Nova obs");
        contrato.setCreatedAt(now);
        contrato.setUpdatedAt(now);
        contrato.setCobrancas(new ArrayList<>());

        assertThat(contrato.getId()).isEqualTo(5L);
        assertThat(contrato.getDescricao()).isEqualTo("Nova descricao");
        assertThat(contrato.getValor()).isEqualByComparingTo("999.99");
        assertThat(contrato.getPeriodicidade()).isEqualTo(Periodicidade.TRIMESTRAL);
        assertThat(contrato.getStatus()).isEqualTo(StatusContrato.ENCERRADO);
        assertThat(contrato.getObservacoes()).isEqualTo("Nova obs");
    }

    @Test
    @DisplayName("equals retorna true para contratos com todos os campos iguais")
    void equals_mesmosValores_retornaTrue() {
        LocalDate inicio = LocalDate.of(2024, 1, 1);
        Contrato c1 = Contrato.builder().id(1L).descricao("A").valor(BigDecimal.ONE).dataInicio(inicio).build();
        Contrato c2 = Contrato.builder().id(1L).descricao("A").valor(BigDecimal.ONE).dataInicio(inicio).build();

        assertThat(c1).isEqualTo(c2);
    }

    @Test
    @DisplayName("equals retorna false para contratos com descricao diferente")
    void equals_descricaoDistinta_retornaFalse() {
        LocalDate inicio = LocalDate.of(2024, 1, 1);
        Contrato c1 = Contrato.builder().id(1L).descricao("A").valor(BigDecimal.ONE).dataInicio(inicio).build();
        Contrato c2 = Contrato.builder().id(1L).descricao("B").valor(BigDecimal.ONE).dataInicio(inicio).build();

        assertThat(c1).isNotEqualTo(c2);
    }

    @Test
    @DisplayName("hashCode e consistente para contratos com mesmos campos")
    void hashCode_consistente() {
        LocalDate inicio = LocalDate.of(2024, 3, 1);
        Contrato c1 = Contrato.builder().id(3L).descricao("X").valor(BigDecimal.ONE).dataInicio(inicio).build();
        Contrato c2 = Contrato.builder().id(3L).descricao("X").valor(BigDecimal.ONE).dataInicio(inicio).build();

        assertThat(c1.hashCode()).isEqualTo(c2.hashCode());
    }

    @Test
    @DisplayName("toString retorna representacao nao nula com descricao")
    void toString_retornaStringNaoVazia() {
        Contrato contrato = Contrato.builder()
                .id(1L)
                .descricao("Servico")
                .valor(BigDecimal.TEN)
                .dataInicio(LocalDate.now())
                .build();

        assertThat(contrato.toString()).isNotBlank();
        assertThat(contrato.toString()).contains("Servico");
    }

    @Test
    @DisplayName("noArgsConstructor cria instancia vazia")
    void noArgsConstructor_criaInstanciaVazia() {
        Contrato contrato = new Contrato();

        assertThat(contrato.getId()).isNull();
        assertThat(contrato.getDescricao()).isNull();
    }

    @Test
    @DisplayName("allArgsConstructor preenche todos os campos")
    void allArgsConstructor_preencheAllFields() {
        LocalDateTime now = LocalDateTime.now();
        Contrato contrato = new Contrato(
                1L,
                null,
                "Descricao",
                new BigDecimal("500.00"),
                Periodicidade.MENSAL,
                LocalDate.now(),
                LocalDate.now().plusYears(1),
                StatusContrato.ATIVO,
                "Obs",
                now,
                now,
                new ArrayList<>()
        );

        assertThat(contrato.getId()).isEqualTo(1L);
        assertThat(contrato.getDescricao()).isEqualTo("Descricao");
        assertThat(contrato.getValor()).isEqualByComparingTo("500.00");
    }

    @Test
    @DisplayName("onCreate define timestamps e defaults quando nulos")
    void onCreate_defineTimestampsEDefaults() throws Exception {
        Contrato contrato = new Contrato();
        assertThat(contrato.getCreatedAt()).isNull();

        var method = Contrato.class.getDeclaredMethod("onCreate");
        method.setAccessible(true);
        method.invoke(contrato);

        assertThat(contrato.getCreatedAt()).isNotNull();
        assertThat(contrato.getUpdatedAt()).isNotNull();
        assertThat(contrato.getPeriodicidade()).isEqualTo(Periodicidade.MENSAL);
        assertThat(contrato.getStatus()).isEqualTo(StatusContrato.ATIVO);
    }

    @Test
    @DisplayName("onCreate nao sobrescreve createdAt quando ja definido")
    void onCreate_naoSobreescreveCreatedAt() throws Exception {
        LocalDateTime original = LocalDateTime.of(2023, 5, 10, 12, 0);
        Contrato contrato = new Contrato();
        contrato.setCreatedAt(original);
        contrato.setPeriodicidade(Periodicidade.ANUAL);
        contrato.setStatus(StatusContrato.ENCERRADO);

        var method = Contrato.class.getDeclaredMethod("onCreate");
        method.setAccessible(true);
        method.invoke(contrato);

        assertThat(contrato.getCreatedAt()).isEqualTo(original);
        assertThat(contrato.getPeriodicidade()).isEqualTo(Periodicidade.ANUAL);
        assertThat(contrato.getStatus()).isEqualTo(StatusContrato.ENCERRADO);
    }

    @Test
    @DisplayName("onUpdate atualiza updatedAt")
    void onUpdate_atualizaUpdatedAt() throws Exception {
        Contrato contrato = new Contrato();
        LocalDateTime before = LocalDateTime.now().minusDays(1);
        contrato.setUpdatedAt(before);

        var method = Contrato.class.getDeclaredMethod("onUpdate");
        method.setAccessible(true);
        method.invoke(contrato);

        assertThat(contrato.getUpdatedAt()).isAfter(before);
    }

    @Test
    @DisplayName("onCreate define periodicidade e status quando nulos explicitamente")
    void onCreate_defineDefaultsQuandoNulos() throws Exception {
        Contrato contrato = new Contrato();
        // no-args constructor sets @Builder.Default fields — force null
        contrato.setPeriodicidade(null);
        contrato.setStatus(null);
        assertThat(contrato.getPeriodicidade()).isNull();
        assertThat(contrato.getStatus()).isNull();

        var method = Contrato.class.getDeclaredMethod("onCreate");
        method.setAccessible(true);
        method.invoke(contrato);

        assertThat(contrato.getPeriodicidade()).isEqualTo(Periodicidade.MENSAL);
        assertThat(contrato.getStatus()).isEqualTo(StatusContrato.ATIVO);
    }

    @Test
    @DisplayName("equals retorna false quando comparado com null")
    void equals_comNull_retornaFalse() {
        Contrato c = Contrato.builder().id(1L).descricao("X").valor(BigDecimal.ONE).dataInicio(LocalDate.now()).build();
        assertThat(c).isNotEqualTo(null);
    }

    @Test
    @DisplayName("equals retorna true quando comparado consigo mesmo")
    void equals_comSiMesmo_retornaTrue() {
        Contrato c = Contrato.builder().id(1L).descricao("X").valor(BigDecimal.ONE).dataInicio(LocalDate.now()).build();
        assertThat(c).isEqualTo(c);
    }
}
