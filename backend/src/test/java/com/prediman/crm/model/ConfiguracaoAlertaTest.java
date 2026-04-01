package com.prediman.crm.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ConfiguracaoAlerta")
class ConfiguracaoAlertaTest {

    @Test
    @DisplayName("parseia corretamente string com quatro valores separados por virgula")
    void getDiasAntecedenciaList_parseia4Valores() {
        ConfiguracaoAlerta config = ConfiguracaoAlerta.builder()
                .diasAntecedencia("30,15,7,1")
                .build();

        List<Integer> dias = config.getDiasAntecedenciaList();

        assertAll(
                () -> assertEquals(4, dias.size()),
                () -> assertEquals(30, dias.get(0)),
                () -> assertEquals(15, dias.get(1)),
                () -> assertEquals(7, dias.get(2)),
                () -> assertEquals(1, dias.get(3))
        );
    }

    @Test
    @DisplayName("parseia corretamente string com espacos ao redor dos valores")
    void getDiasAntecedenciaList_ignoraEspacos() {
        ConfiguracaoAlerta config = ConfiguracaoAlerta.builder()
                .diasAntecedencia(" 60 , 30 , 7 ")
                .build();

        List<Integer> dias = config.getDiasAntecedenciaList();

        assertAll(
                () -> assertEquals(3, dias.size()),
                () -> assertEquals(60, dias.get(0)),
                () -> assertEquals(30, dias.get(1)),
                () -> assertEquals(7, dias.get(2))
        );
    }

    @Test
    @DisplayName("parseia corretamente string com valor unico")
    void getDiasAntecedenciaList_valorUnico() {
        ConfiguracaoAlerta config = ConfiguracaoAlerta.builder()
                .diasAntecedencia("7")
                .build();

        List<Integer> dias = config.getDiasAntecedenciaList();

        assertEquals(List.of(7), dias);
    }

    @Test
    @DisplayName("retorna default [30, 15, 7, 1] quando diasAntecedencia e string vazia")
    void getDiasAntecedenciaList_stringVaziaRetornaDefault() {
        ConfiguracaoAlerta config = ConfiguracaoAlerta.builder()
                .diasAntecedencia("")
                .build();

        List<Integer> dias = config.getDiasAntecedenciaList();

        assertEquals(List.of(30, 15, 7, 1), dias);
    }

    @Test
    @DisplayName("retorna default [30, 15, 7, 1] quando diasAntecedencia e apenas espacos")
    void getDiasAntecedenciaList_apenasEspacosRetornaDefault() {
        ConfiguracaoAlerta config = ConfiguracaoAlerta.builder()
                .diasAntecedencia("   ")
                .build();

        List<Integer> dias = config.getDiasAntecedenciaList();

        assertEquals(List.of(30, 15, 7, 1), dias);
    }

    @Test
    @DisplayName("retorna default [30, 15, 7, 1] quando diasAntecedencia e null")
    void getDiasAntecedenciaList_nullRetornaDefault() {
        ConfiguracaoAlerta config = ConfiguracaoAlerta.builder()
                .diasAntecedencia(null)
                .build();

        List<Integer> dias = config.getDiasAntecedenciaList();

        assertEquals(List.of(30, 15, 7, 1), dias);
    }

    // ---- builder / getters / setters / lifecycle coverage ----

    @Test
    @DisplayName("builder cria instancia com todos os campos preenchidos")
    void builder_criaInstanciaCompleta() {
        LocalDateTime now = LocalDateTime.now();
        LocalTime horario = LocalTime.of(9, 30);

        ConfiguracaoAlerta config = ConfiguracaoAlerta.builder()
                .id(1L)
                .diasAntecedencia("30,15,7")
                .horarioExecucao(horario)
                .emailAtivo(true)
                .whatsappAtivo(false)
                .templateEmail("Seu documento vence em {dias} dias")
                .templateWhatsapp("Ola, seu documento vence em breve")
                .createdAt(now)
                .updatedAt(now)
                .build();

        assertThat(config.getId()).isEqualTo(1L);
        assertThat(config.getDiasAntecedencia()).isEqualTo("30,15,7");
        assertThat(config.getHorarioExecucao()).isEqualTo(horario);
        assertThat(config.getEmailAtivo()).isTrue();
        assertThat(config.getWhatsappAtivo()).isFalse();
        assertThat(config.getTemplateEmail()).isEqualTo("Seu documento vence em {dias} dias");
        assertThat(config.getTemplateWhatsapp()).isEqualTo("Ola, seu documento vence em breve");
        assertThat(config.getCreatedAt()).isEqualTo(now);
        assertThat(config.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("builder usa defaults para emailAtivo e whatsappAtivo quando nao informados")
    void builder_usaDefaults() {
        ConfiguracaoAlerta config = ConfiguracaoAlerta.builder()
                .diasAntecedencia("30")
                .build();

        assertThat(config.getEmailAtivo()).isTrue();
        assertThat(config.getWhatsappAtivo()).isFalse();
    }

    @Test
    @DisplayName("setters alteram todos os campos corretamente")
    void setters_alteramValores() {
        ConfiguracaoAlerta config = new ConfiguracaoAlerta();
        LocalDateTime now = LocalDateTime.now();
        LocalTime horario = LocalTime.of(10, 0);

        config.setId(2L);
        config.setDiasAntecedencia("7,1");
        config.setHorarioExecucao(horario);
        config.setEmailAtivo(false);
        config.setWhatsappAtivo(true);
        config.setTemplateEmail("Template email");
        config.setTemplateWhatsapp("Template wpp");
        config.setCreatedAt(now);
        config.setUpdatedAt(now);

        assertThat(config.getId()).isEqualTo(2L);
        assertThat(config.getDiasAntecedencia()).isEqualTo("7,1");
        assertThat(config.getHorarioExecucao()).isEqualTo(horario);
        assertThat(config.getEmailAtivo()).isFalse();
        assertThat(config.getWhatsappAtivo()).isTrue();
        assertThat(config.getTemplateEmail()).isEqualTo("Template email");
        assertThat(config.getTemplateWhatsapp()).isEqualTo("Template wpp");
    }

    @Test
    @DisplayName("equals retorna true para configuracoes com mesmo id")
    void equals_mesmosIds_retornaTrue() {
        ConfiguracaoAlerta c1 = ConfiguracaoAlerta.builder().id(1L).build();
        ConfiguracaoAlerta c2 = ConfiguracaoAlerta.builder().id(1L).diasAntecedencia("7").build();

        assertThat(c1).isEqualTo(c2);
    }

    @Test
    @DisplayName("equals retorna false para configuracoes com ids diferentes")
    void equals_idsDistintos_retornaFalse() {
        ConfiguracaoAlerta c1 = ConfiguracaoAlerta.builder().id(1L).build();
        ConfiguracaoAlerta c2 = ConfiguracaoAlerta.builder().id(2L).build();

        assertThat(c1).isNotEqualTo(c2);
    }

    @Test
    @DisplayName("hashCode e consistente")
    void hashCode_consistente() {
        ConfiguracaoAlerta c1 = ConfiguracaoAlerta.builder().id(3L).build();
        ConfiguracaoAlerta c2 = ConfiguracaoAlerta.builder().id(3L).diasAntecedencia("30").build();

        assertThat(c1.hashCode()).isEqualTo(c2.hashCode());
    }

    @Test
    @DisplayName("noArgsConstructor cria instancia vazia")
    void noArgsConstructor_criaInstanciaVazia() {
        ConfiguracaoAlerta config = new ConfiguracaoAlerta();

        assertThat(config.getId()).isNull();
        assertThat(config.getDiasAntecedencia()).isNull();
    }

    @Test
    @DisplayName("allArgsConstructor preenche todos os campos")
    void allArgsConstructor_preencheAllFields() {
        LocalDateTime now = LocalDateTime.now();
        ConfiguracaoAlerta config = new ConfiguracaoAlerta(
                1L, "30,15,7,1", LocalTime.of(8, 0),
                true, false,
                "template email", "template wpp",
                now, now
        );

        assertThat(config.getId()).isEqualTo(1L);
        assertThat(config.getDiasAntecedencia()).isEqualTo("30,15,7,1");
        assertThat(config.getHorarioExecucao()).isEqualTo(LocalTime.of(8, 0));
    }

    @Test
    @DisplayName("onCreate define timestamps e defaults quando nulos")
    void onCreate_defineTimestampsEDefaults() throws Exception {
        ConfiguracaoAlerta config = new ConfiguracaoAlerta();
        assertThat(config.getCreatedAt()).isNull();

        var method = ConfiguracaoAlerta.class.getDeclaredMethod("onCreate");
        method.setAccessible(true);
        method.invoke(config);

        assertThat(config.getCreatedAt()).isNotNull();
        assertThat(config.getUpdatedAt()).isNotNull();
        assertThat(config.getEmailAtivo()).isTrue();
        assertThat(config.getWhatsappAtivo()).isFalse();
    }

    @Test
    @DisplayName("onCreate nao sobrescreve valores quando ja definidos")
    void onCreate_naoSobreescreveValoresExistentes() throws Exception {
        LocalDateTime original = LocalDateTime.of(2023, 8, 8, 8, 8);
        ConfiguracaoAlerta config = new ConfiguracaoAlerta();
        config.setCreatedAt(original);
        config.setEmailAtivo(false);
        config.setWhatsappAtivo(true);

        var method = ConfiguracaoAlerta.class.getDeclaredMethod("onCreate");
        method.setAccessible(true);
        method.invoke(config);

        assertThat(config.getCreatedAt()).isEqualTo(original);
        assertThat(config.getEmailAtivo()).isFalse();
        assertThat(config.getWhatsappAtivo()).isTrue();
    }

    @Test
    @DisplayName("onUpdate atualiza updatedAt")
    void onUpdate_atualizaUpdatedAt() throws Exception {
        ConfiguracaoAlerta config = new ConfiguracaoAlerta();
        LocalDateTime before = LocalDateTime.now().minusDays(1);
        config.setUpdatedAt(before);

        var method = ConfiguracaoAlerta.class.getDeclaredMethod("onUpdate");
        method.setAccessible(true);
        method.invoke(config);

        assertThat(config.getUpdatedAt()).isAfter(before);
    }

    @Test
    @DisplayName("onCreate define emailAtivo e whatsappAtivo quando nulos explicitamente")
    void onCreate_defineDefaultsQuandoNulos() throws Exception {
        ConfiguracaoAlerta config = new ConfiguracaoAlerta();
        // no-args constructor sets @Builder.Default fields — force null
        config.setEmailAtivo(null);
        config.setWhatsappAtivo(null);
        assertThat(config.getEmailAtivo()).isNull();
        assertThat(config.getWhatsappAtivo()).isNull();

        var method = ConfiguracaoAlerta.class.getDeclaredMethod("onCreate");
        method.setAccessible(true);
        method.invoke(config);

        assertThat(config.getEmailAtivo()).isTrue();
        assertThat(config.getWhatsappAtivo()).isFalse();
    }

    @Test
    @DisplayName("equals retorna false quando comparado com null")
    void equals_comNull_retornaFalse() {
        ConfiguracaoAlerta c = ConfiguracaoAlerta.builder().id(1L).build();
        assertThat(c).isNotEqualTo(null);
    }

    @Test
    @DisplayName("equals retorna true quando comparado consigo mesmo")
    void equals_comSiMesmo_retornaTrue() {
        ConfiguracaoAlerta c = ConfiguracaoAlerta.builder().id(1L).build();
        assertThat(c).isEqualTo(c);
    }
}
