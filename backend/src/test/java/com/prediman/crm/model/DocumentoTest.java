package com.prediman.crm.model;

import com.prediman.crm.model.enums.CategoriaDocumento;
import com.prediman.crm.model.enums.StatusDocumento;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Documento")
class DocumentoTest {

    // DocumentoConstants.DIAS_ALERTA_VENCIMENTO = 30

    @Test
    @DisplayName("retorna VALIDO quando dataValidade e mais de 30 dias a partir de hoje")
    void getStatusCalculado_valido() {
        Documento documento = Documento.builder()
                .nome("Documento Valido")
                .dataValidade(LocalDate.now().plusDays(31))
                .build();

        assertEquals(StatusDocumento.VALIDO, documento.getStatusCalculado());
    }

    @Test
    @DisplayName("retorna A_VENCER quando dataValidade esta dentro de 30 dias")
    void getStatusCalculado_aVencer_dentroDe30Dias() {
        Documento documento = Documento.builder()
                .nome("Documento A Vencer")
                .dataValidade(LocalDate.now().plusDays(15))
                .build();

        assertEquals(StatusDocumento.A_VENCER, documento.getStatusCalculado());
    }

    @Test
    @DisplayName("retorna A_VENCER quando dataValidade e exatamente 30 dias a partir de hoje")
    void getStatusCalculado_aVencer_exatamente30Dias() {
        Documento documento = Documento.builder()
                .nome("Documento Limite")
                .dataValidade(LocalDate.now().plusDays(30))
                .build();

        assertEquals(StatusDocumento.A_VENCER, documento.getStatusCalculado());
    }

    @Test
    @DisplayName("retorna A_VENCER quando dataValidade e hoje")
    void getStatusCalculado_aVencer_hoje() {
        Documento documento = Documento.builder()
                .nome("Documento Vence Hoje")
                .dataValidade(LocalDate.now())
                .build();

        assertEquals(StatusDocumento.A_VENCER, documento.getStatusCalculado());
    }

    @Test
    @DisplayName("retorna VENCIDO quando dataValidade e ontem")
    void getStatusCalculado_vencido_ontem() {
        Documento documento = Documento.builder()
                .nome("Documento Vencido Ontem")
                .dataValidade(LocalDate.now().minusDays(1))
                .build();

        assertEquals(StatusDocumento.VENCIDO, documento.getStatusCalculado());
    }

    @Test
    @DisplayName("retorna VENCIDO quando dataValidade esta no passado")
    void getStatusCalculado_vencido_passadoDistante() {
        Documento documento = Documento.builder()
                .nome("Documento Muito Vencido")
                .dataValidade(LocalDate.of(2020, 1, 1))
                .build();

        assertEquals(StatusDocumento.VENCIDO, documento.getStatusCalculado());
    }

    @Test
    @DisplayName("retorna SEM_VALIDADE quando dataValidade e null")
    void getStatusCalculado_semValidade() {
        Documento documento = Documento.builder()
                .nome("Documento Permanente")
                .dataValidade(null)
                .build();

        assertEquals(StatusDocumento.SEM_VALIDADE, documento.getStatusCalculado());
    }

    // ---- builder / getters / setters / lifecycle coverage ----

    @Test
    @DisplayName("builder cria instancia com todos os campos preenchidos")
    void builder_criaInstanciaCompleta() {
        LocalDateTime now = LocalDateTime.now();
        Cliente cliente = Cliente.builder().id(1L).razaoSocial("Cliente").build();

        Documento doc = Documento.builder()
                .id(1L)
                .cliente(cliente)
                .nome("Alvara.pdf")
                .categoria(CategoriaDocumento.ALVARA)
                .dataEmissao(LocalDate.of(2023, 1, 1))
                .dataValidade(LocalDate.of(2024, 1, 1))
                .revisao("v1")
                .observacoes("Obs teste")
                .googleDriveFileId("file-id-abc")
                .googleDriveUrl("https://drive.google.com/abc")
                .tamanhoBytes(2048L)
                .mimeType("application/pdf")
                .createdAt(now)
                .updatedAt(now)
                .build();

        assertThat(doc.getId()).isEqualTo(1L);
        assertThat(doc.getCliente()).isEqualTo(cliente);
        assertThat(doc.getNome()).isEqualTo("Alvara.pdf");
        assertThat(doc.getCategoria()).isEqualTo(CategoriaDocumento.ALVARA);
        assertThat(doc.getDataEmissao()).isEqualTo(LocalDate.of(2023, 1, 1));
        assertThat(doc.getDataValidade()).isEqualTo(LocalDate.of(2024, 1, 1));
        assertThat(doc.getRevisao()).isEqualTo("v1");
        assertThat(doc.getObservacoes()).isEqualTo("Obs teste");
        assertThat(doc.getGoogleDriveFileId()).isEqualTo("file-id-abc");
        assertThat(doc.getGoogleDriveUrl()).isEqualTo("https://drive.google.com/abc");
        assertThat(doc.getTamanhoBytes()).isEqualTo(2048L);
        assertThat(doc.getMimeType()).isEqualTo("application/pdf");
        assertThat(doc.getCreatedAt()).isEqualTo(now);
        assertThat(doc.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("builder usa categoria OUTRO como default")
    void builder_usaDefaultCategoria() {
        Documento doc = Documento.builder().nome("Doc").build();

        assertThat(doc.getCategoria()).isEqualTo(CategoriaDocumento.OUTRO);
    }

    @Test
    @DisplayName("setters alteram todos os campos corretamente")
    void setters_alteramValores() {
        Documento doc = new Documento();
        LocalDateTime now = LocalDateTime.now();

        doc.setId(3L);
        doc.setNome("Novo nome");
        doc.setCategoria(CategoriaDocumento.CONTRATO);
        doc.setDataEmissao(LocalDate.of(2022, 5, 10));
        doc.setDataValidade(LocalDate.of(2023, 5, 10));
        doc.setRevisao("v2");
        doc.setObservacoes("Nova obs");
        doc.setGoogleDriveFileId("new-file");
        doc.setGoogleDriveUrl("https://drive.google.com/new");
        doc.setTamanhoBytes(4096L);
        doc.setMimeType("image/png");
        doc.setCreatedAt(now);
        doc.setUpdatedAt(now);

        assertThat(doc.getId()).isEqualTo(3L);
        assertThat(doc.getNome()).isEqualTo("Novo nome");
        assertThat(doc.getCategoria()).isEqualTo(CategoriaDocumento.CONTRATO);
        assertThat(doc.getRevisao()).isEqualTo("v2");
        assertThat(doc.getTamanhoBytes()).isEqualTo(4096L);
        assertThat(doc.getMimeType()).isEqualTo("image/png");
    }

    @Test
    @DisplayName("equals retorna true para documentos com todos os campos iguais")
    void equals_mesmosValores_retornaTrue() {
        Documento d1 = Documento.builder().id(1L).nome("DocA").build();
        Documento d2 = Documento.builder().id(1L).nome("DocA").build();

        assertThat(d1).isEqualTo(d2);
    }

    @Test
    @DisplayName("equals retorna false para documentos com nome diferente")
    void equals_nomeDistinto_retornaFalse() {
        Documento d1 = Documento.builder().id(1L).nome("DocA").build();
        Documento d2 = Documento.builder().id(1L).nome("DocB").build();

        assertThat(d1).isNotEqualTo(d2);
    }

    @Test
    @DisplayName("hashCode e consistente para documentos com mesmos campos")
    void hashCode_consistente() {
        Documento d1 = Documento.builder().id(4L).nome("DocX").build();
        Documento d2 = Documento.builder().id(4L).nome("DocX").build();

        assertThat(d1.hashCode()).isEqualTo(d2.hashCode());
    }

    @Test
    @DisplayName("toString retorna string nao vazia com nome do documento")
    void toString_retornaStringNaoVazia() {
        Documento doc = Documento.builder().id(1L).nome("Licenca").build();

        assertThat(doc.toString()).isNotBlank();
        assertThat(doc.toString()).contains("Licenca");
    }

    @Test
    @DisplayName("noArgsConstructor cria instancia vazia")
    void noArgsConstructor_criaInstanciaVazia() {
        Documento doc = new Documento();

        assertThat(doc.getId()).isNull();
        assertThat(doc.getNome()).isNull();
    }

    @Test
    @DisplayName("allArgsConstructor preenche todos os campos")
    void allArgsConstructor_preencheAllFields() {
        LocalDateTime now = LocalDateTime.now();
        Documento doc = new Documento(
                1L, null, "Doc.pdf", CategoriaDocumento.OUTRO,
                LocalDate.now(), LocalDate.now().plusYears(1),
                "v1", "obs", "file-id", "https://url", 1024L, "application/pdf",
                now, now
        );

        assertThat(doc.getId()).isEqualTo(1L);
        assertThat(doc.getNome()).isEqualTo("Doc.pdf");
    }

    @Test
    @DisplayName("onCreate define createdAt e updatedAt quando nulos")
    void onCreate_defineTimestamps() throws Exception {
        Documento doc = new Documento();

        var method = Documento.class.getDeclaredMethod("onCreate");
        method.setAccessible(true);
        method.invoke(doc);

        assertThat(doc.getCreatedAt()).isNotNull();
        assertThat(doc.getUpdatedAt()).isNotNull();
        assertThat(doc.getCategoria()).isEqualTo(CategoriaDocumento.OUTRO);
    }

    @Test
    @DisplayName("onCreate nao sobrescreve createdAt e categoria quando ja definidos")
    void onCreate_naoSobreescreveValoresExistentes() throws Exception {
        LocalDateTime original = LocalDateTime.of(2022, 1, 1, 0, 0);
        Documento doc = new Documento();
        doc.setCreatedAt(original);
        doc.setCategoria(CategoriaDocumento.ALVARA);

        var method = Documento.class.getDeclaredMethod("onCreate");
        method.setAccessible(true);
        method.invoke(doc);

        assertThat(doc.getCreatedAt()).isEqualTo(original);
        assertThat(doc.getCategoria()).isEqualTo(CategoriaDocumento.ALVARA);
    }

    @Test
    @DisplayName("onUpdate atualiza updatedAt")
    void onUpdate_atualizaUpdatedAt() throws Exception {
        Documento doc = new Documento();
        LocalDateTime before = LocalDateTime.now().minusDays(1);
        doc.setUpdatedAt(before);

        var method = Documento.class.getDeclaredMethod("onUpdate");
        method.setAccessible(true);
        method.invoke(doc);

        assertThat(doc.getUpdatedAt()).isAfter(before);
    }

    @Test
    @DisplayName("onCreate define categoria OUTRO quando nula explicitamente")
    void onCreate_defineCategoriaQuandoNula() throws Exception {
        Documento doc = new Documento();
        // no-args constructor sets categoria via @Builder.Default — force null
        doc.setCategoria(null);
        assertThat(doc.getCategoria()).isNull();

        var method = Documento.class.getDeclaredMethod("onCreate");
        method.setAccessible(true);
        method.invoke(doc);

        assertThat(doc.getCategoria()).isEqualTo(CategoriaDocumento.OUTRO);
    }

    @Test
    @DisplayName("equals retorna false quando comparado com null")
    void equals_comNull_retornaFalse() {
        Documento d = Documento.builder().id(1L).nome("Doc").build();
        assertThat(d).isNotEqualTo(null);
    }

    @Test
    @DisplayName("equals retorna true quando comparado consigo mesmo")
    void equals_comSiMesmo_retornaTrue() {
        Documento d = Documento.builder().id(1L).nome("Doc").build();
        assertThat(d).isEqualTo(d);
    }
}
