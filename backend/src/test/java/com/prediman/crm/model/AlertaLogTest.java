package com.prediman.crm.model;

import com.prediman.crm.model.enums.CanalAlerta;
import com.prediman.crm.model.enums.StatusEnvio;
import com.prediman.crm.model.enums.TipoAlerta;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AlertaLog — cobertura de getters, setters, builder, equals, hashCode e toString")
class AlertaLogTest {

    @Test
    @DisplayName("builder cria instancia com todos os campos preenchidos")
    void builder_criaInstanciaCompleta() {
        LocalDateTime now = LocalDateTime.now();
        Documento documento = Documento.builder().id(10L).nome("Licenca.pdf").build();

        AlertaLog log = AlertaLog.builder()
                .id(1L)
                .documento(documento)
                .cobrancaId(5L)
                .tipo(TipoAlerta.DOCUMENTO)
                .canal(CanalAlerta.EMAIL)
                .destinatario("contato@empresa.com")
                .mensagem("Documento vencendo em 7 dias")
                .statusEnvio(StatusEnvio.ENVIADO)
                .dataEnvio(now)
                .snoozedAte(LocalDate.of(2025, 3, 1))
                .createdAt(now)
                .build();

        assertThat(log.getId()).isEqualTo(1L);
        assertThat(log.getDocumento()).isEqualTo(documento);
        assertThat(log.getCobrancaId()).isEqualTo(5L);
        assertThat(log.getTipo()).isEqualTo(TipoAlerta.DOCUMENTO);
        assertThat(log.getCanal()).isEqualTo(CanalAlerta.EMAIL);
        assertThat(log.getDestinatario()).isEqualTo("contato@empresa.com");
        assertThat(log.getMensagem()).isEqualTo("Documento vencendo em 7 dias");
        assertThat(log.getStatusEnvio()).isEqualTo(StatusEnvio.ENVIADO);
        assertThat(log.getDataEnvio()).isEqualTo(now);
        assertThat(log.getSnoozedAte()).isEqualTo(LocalDate.of(2025, 3, 1));
        assertThat(log.getCreatedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("builder usa statusEnvio PENDENTE como default")
    void builder_usaDefaultStatusPendente() {
        AlertaLog log = AlertaLog.builder()
                .tipo(TipoAlerta.COBRANCA)
                .canal(CanalAlerta.WHATSAPP)
                .build();

        assertThat(log.getStatusEnvio()).isEqualTo(StatusEnvio.PENDENTE);
    }

    @Test
    @DisplayName("setters alteram todos os campos corretamente")
    void setters_alteramValores() {
        AlertaLog log = new AlertaLog();
        LocalDateTime now = LocalDateTime.now();

        log.setId(2L);
        log.setCobrancaId(8L);
        log.setTipo(TipoAlerta.COBRANCA);
        log.setCanal(CanalAlerta.WHATSAPP);
        log.setDestinatario("+5511999998888");
        log.setMensagem("Cobranca vencida");
        log.setStatusEnvio(StatusEnvio.ERRO);
        log.setDataEnvio(now);
        log.setSnoozedAte(LocalDate.of(2025, 6, 1));
        log.setCreatedAt(now);

        assertThat(log.getId()).isEqualTo(2L);
        assertThat(log.getCobrancaId()).isEqualTo(8L);
        assertThat(log.getTipo()).isEqualTo(TipoAlerta.COBRANCA);
        assertThat(log.getCanal()).isEqualTo(CanalAlerta.WHATSAPP);
        assertThat(log.getDestinatario()).isEqualTo("+5511999998888");
        assertThat(log.getMensagem()).isEqualTo("Cobranca vencida");
        assertThat(log.getStatusEnvio()).isEqualTo(StatusEnvio.ERRO);
        assertThat(log.getDataEnvio()).isEqualTo(now);
        assertThat(log.getSnoozedAte()).isEqualTo(LocalDate.of(2025, 6, 1));
        assertThat(log.getCreatedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("equals retorna true para logs com todos os campos iguais exceto documento")
    void equals_mesmosValores_retornaTrue() {
        AlertaLog l1 = AlertaLog.builder().id(1L).tipo(TipoAlerta.DOCUMENTO).canal(CanalAlerta.EMAIL).build();
        AlertaLog l2 = AlertaLog.builder().id(1L).tipo(TipoAlerta.DOCUMENTO).canal(CanalAlerta.EMAIL).build();

        assertThat(l1).isEqualTo(l2);
    }

    @Test
    @DisplayName("equals retorna false para logs com tipo diferente")
    void equals_tipoDistinto_retornaFalse() {
        AlertaLog l1 = AlertaLog.builder().id(1L).tipo(TipoAlerta.DOCUMENTO).canal(CanalAlerta.EMAIL).build();
        AlertaLog l2 = AlertaLog.builder().id(1L).tipo(TipoAlerta.COBRANCA).canal(CanalAlerta.EMAIL).build();

        assertThat(l1).isNotEqualTo(l2);
    }

    @Test
    @DisplayName("hashCode e consistente para logs com mesmos campos")
    void hashCode_consistente() {
        AlertaLog l1 = AlertaLog.builder().id(7L).tipo(TipoAlerta.DOCUMENTO).canal(CanalAlerta.EMAIL).build();
        AlertaLog l2 = AlertaLog.builder().id(7L).tipo(TipoAlerta.DOCUMENTO).canal(CanalAlerta.EMAIL).build();

        assertThat(l1.hashCode()).isEqualTo(l2.hashCode());
    }

    @Test
    @DisplayName("toString retorna representacao nao nula")
    void toString_retornaStringNaoVazia() {
        AlertaLog log = AlertaLog.builder()
                .id(1L)
                .tipo(TipoAlerta.DOCUMENTO)
                .canal(CanalAlerta.EMAIL)
                .destinatario("test@test.com")
                .build();

        assertThat(log.toString()).isNotBlank();
    }

    @Test
    @DisplayName("noArgsConstructor cria instancia vazia")
    void noArgsConstructor_criaInstanciaVazia() {
        AlertaLog log = new AlertaLog();

        assertThat(log.getId()).isNull();
        assertThat(log.getTipo()).isNull();
    }

    @Test
    @DisplayName("allArgsConstructor preenche todos os campos")
    void allArgsConstructor_preencheAllFields() {
        LocalDateTime now = LocalDateTime.now();
        AlertaLog log = new AlertaLog(
                1L,
                null,
                2L,
                TipoAlerta.DOCUMENTO,
                CanalAlerta.EMAIL,
                "dest@dest.com",
                "mensagem",
                StatusEnvio.PENDENTE,
                now,
                LocalDate.now(),
                now
        );

        assertThat(log.getId()).isEqualTo(1L);
        assertThat(log.getTipo()).isEqualTo(TipoAlerta.DOCUMENTO);
        assertThat(log.getStatusEnvio()).isEqualTo(StatusEnvio.PENDENTE);
    }

    @Test
    @DisplayName("onCreate define createdAt quando nulo e mantém statusEnvio PENDENTE")
    void onCreate_defineCreatedAtEStatusDefault() throws Exception {
        // @Builder.Default initializes statusEnvio=PENDENTE even on no-args constructor (Lombok behaviour)
        AlertaLog log = new AlertaLog();
        assertThat(log.getCreatedAt()).isNull();

        var method = AlertaLog.class.getDeclaredMethod("onCreate");
        method.setAccessible(true);
        method.invoke(log);

        assertThat(log.getCreatedAt()).isNotNull();
        assertThat(log.getStatusEnvio()).isEqualTo(StatusEnvio.PENDENTE);
    }

    @Test
    @DisplayName("onCreate nao sobrescreve createdAt quando ja definido")
    void onCreate_naoSobreescreveCreatedAt() throws Exception {
        LocalDateTime original = LocalDateTime.of(2023, 2, 20, 8, 0);
        AlertaLog log = new AlertaLog();
        log.setCreatedAt(original);
        log.setStatusEnvio(StatusEnvio.ENVIADO);

        var method = AlertaLog.class.getDeclaredMethod("onCreate");
        method.setAccessible(true);
        method.invoke(log);

        assertThat(log.getCreatedAt()).isEqualTo(original);
        assertThat(log.getStatusEnvio()).isEqualTo(StatusEnvio.ENVIADO);
    }

    @Test
    @DisplayName("onCreate define statusEnvio PENDENTE quando nulo explicitamente")
    void onCreate_defineStatusQuandoNulo() throws Exception {
        AlertaLog log = new AlertaLog();
        // no-args constructor sets statusEnvio via @Builder.Default — force null
        log.setStatusEnvio(null);
        assertThat(log.getStatusEnvio()).isNull();

        var method = AlertaLog.class.getDeclaredMethod("onCreate");
        method.setAccessible(true);
        method.invoke(log);

        assertThat(log.getStatusEnvio()).isEqualTo(StatusEnvio.PENDENTE);
    }

    @Test
    @DisplayName("equals retorna false quando comparado com null")
    void equals_comNull_retornaFalse() {
        AlertaLog log = AlertaLog.builder().id(1L).tipo(TipoAlerta.DOCUMENTO).canal(CanalAlerta.EMAIL).build();

        assertThat(log).isNotEqualTo(null);
    }

    @Test
    @DisplayName("equals retorna false quando comparado com objeto de outro tipo")
    void equals_comOutroTipo_retornaFalse() {
        AlertaLog log = AlertaLog.builder().id(1L).tipo(TipoAlerta.DOCUMENTO).canal(CanalAlerta.EMAIL).build();

        assertThat(log).isNotEqualTo("string");
    }

    @Test
    @DisplayName("equals retorna true quando comparado consigo mesmo")
    void equals_comSiMesmo_retornaTrue() {
        AlertaLog log = AlertaLog.builder().id(1L).tipo(TipoAlerta.DOCUMENTO).canal(CanalAlerta.EMAIL).build();

        assertThat(log).isEqualTo(log);
    }
}
