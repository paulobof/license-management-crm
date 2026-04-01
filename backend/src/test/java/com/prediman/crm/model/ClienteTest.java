package com.prediman.crm.model;

import com.prediman.crm.model.enums.StatusCliente;
import com.prediman.crm.model.enums.TipoPessoa;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Cliente — cobertura de getters, setters, builder, equals, hashCode e toString")
class ClienteTest {

    @Test
    @DisplayName("builder cria instancia com todos os campos preenchidos")
    void builder_criaInstanciaCompleta() {
        LocalDateTime now = LocalDateTime.now();

        Cliente cliente = Cliente.builder()
                .id(1L)
                .tipoPessoa(TipoPessoa.JURIDICA)
                .razaoSocial("Empresa Teste Ltda")
                .nomeFantasia("Empresa Teste")
                .cnpj("12.345.678/0001-99")
                .cpf(null)
                .ie("123456789")
                .segmento("Tecnologia")
                .dataFundacao(LocalDate.of(2000, 1, 1))
                .dataInicioCliente(LocalDate.of(2020, 6, 15))
                .status(StatusCliente.ATIVO)
                .googleDriveFolderId("drive-folder-abc")
                .createdAt(now)
                .updatedAt(now)
                .contatos(new ArrayList<>())
                .enderecos(new ArrayList<>())
                .documentos(new ArrayList<>())
                .contratos(new ArrayList<>())
                .build();

        assertThat(cliente.getId()).isEqualTo(1L);
        assertThat(cliente.getTipoPessoa()).isEqualTo(TipoPessoa.JURIDICA);
        assertThat(cliente.getRazaoSocial()).isEqualTo("Empresa Teste Ltda");
        assertThat(cliente.getNomeFantasia()).isEqualTo("Empresa Teste");
        assertThat(cliente.getCnpj()).isEqualTo("12.345.678/0001-99");
        assertThat(cliente.getCpf()).isNull();
        assertThat(cliente.getIe()).isEqualTo("123456789");
        assertThat(cliente.getSegmento()).isEqualTo("Tecnologia");
        assertThat(cliente.getDataFundacao()).isEqualTo(LocalDate.of(2000, 1, 1));
        assertThat(cliente.getDataInicioCliente()).isEqualTo(LocalDate.of(2020, 6, 15));
        assertThat(cliente.getStatus()).isEqualTo(StatusCliente.ATIVO);
        assertThat(cliente.getGoogleDriveFolderId()).isEqualTo("drive-folder-abc");
        assertThat(cliente.getCreatedAt()).isEqualTo(now);
        assertThat(cliente.getUpdatedAt()).isEqualTo(now);
        assertThat(cliente.getContatos()).isEmpty();
        assertThat(cliente.getEnderecos()).isEmpty();
        assertThat(cliente.getDocumentos()).isEmpty();
        assertThat(cliente.getContratos()).isEmpty();
    }

    @Test
    @DisplayName("builder usa defaults para tipoPessoa e status quando nao informados")
    void builder_usaDefaultsQuandoNaoInformados() {
        Cliente cliente = Cliente.builder()
                .razaoSocial("Sem Defaults")
                .build();

        assertThat(cliente.getTipoPessoa()).isEqualTo(TipoPessoa.JURIDICA);
        assertThat(cliente.getStatus()).isEqualTo(StatusCliente.ATIVO);
    }

    @Test
    @DisplayName("setters alteram os valores dos campos corretamente")
    void setters_alteramValores() {
        Cliente cliente = new Cliente();

        cliente.setId(10L);
        cliente.setRazaoSocial("Nova Razao Social");
        cliente.setNomeFantasia("Novo Fantasia");
        cliente.setCnpj("99.999.999/0001-00");
        cliente.setCpf("123.456.789-00");
        cliente.setIe("987654");
        cliente.setSegmento("Saude");
        cliente.setTipoPessoa(TipoPessoa.FISICA);
        cliente.setStatus(StatusCliente.INATIVO);
        cliente.setDataFundacao(LocalDate.of(1990, 3, 10));
        cliente.setDataInicioCliente(LocalDate.of(2015, 7, 20));
        cliente.setGoogleDriveFolderId("new-folder");
        LocalDateTime ts = LocalDateTime.of(2024, 1, 1, 0, 0);
        cliente.setCreatedAt(ts);
        cliente.setUpdatedAt(ts);

        assertThat(cliente.getId()).isEqualTo(10L);
        assertThat(cliente.getRazaoSocial()).isEqualTo("Nova Razao Social");
        assertThat(cliente.getNomeFantasia()).isEqualTo("Novo Fantasia");
        assertThat(cliente.getCnpj()).isEqualTo("99.999.999/0001-00");
        assertThat(cliente.getCpf()).isEqualTo("123.456.789-00");
        assertThat(cliente.getIe()).isEqualTo("987654");
        assertThat(cliente.getSegmento()).isEqualTo("Saude");
        assertThat(cliente.getTipoPessoa()).isEqualTo(TipoPessoa.FISICA);
        assertThat(cliente.getStatus()).isEqualTo(StatusCliente.INATIVO);
        assertThat(cliente.getDataFundacao()).isEqualTo(LocalDate.of(1990, 3, 10));
        assertThat(cliente.getDataInicioCliente()).isEqualTo(LocalDate.of(2015, 7, 20));
        assertThat(cliente.getGoogleDriveFolderId()).isEqualTo("new-folder");
        assertThat(cliente.getCreatedAt()).isEqualTo(ts);
        assertThat(cliente.getUpdatedAt()).isEqualTo(ts);
    }

    @Test
    @DisplayName("equals retorna true para dois clientes com todos os campos iguais")
    void equals_mesmosValores_retornaTrue() {
        Cliente c1 = Cliente.builder().id(1L).razaoSocial("Empresa A").build();
        Cliente c2 = Cliente.builder().id(1L).razaoSocial("Empresa A").build();

        assertThat(c1).isEqualTo(c2);
    }

    @Test
    @DisplayName("equals retorna false para clientes com razaoSocial diferente")
    void equals_razaoSocialDistinta_retornaFalse() {
        Cliente c1 = Cliente.builder().id(1L).razaoSocial("Empresa A").build();
        Cliente c2 = Cliente.builder().id(1L).razaoSocial("Empresa B").build();

        assertThat(c1).isNotEqualTo(c2);
    }

    @Test
    @DisplayName("hashCode e consistente entre dois clientes com mesmos campos")
    void hashCode_consistente() {
        Cliente c1 = Cliente.builder().id(5L).razaoSocial("X").build();
        Cliente c2 = Cliente.builder().id(5L).razaoSocial("X").build();

        assertThat(c1.hashCode()).isEqualTo(c2.hashCode());
    }

    @Test
    @DisplayName("toString retorna representacao nao nula e nao vazia")
    void toString_retornaStringNaoVazia() {
        Cliente cliente = Cliente.builder().id(1L).razaoSocial("Empresa").build();

        assertThat(cliente.toString()).isNotBlank();
        assertThat(cliente.toString()).contains("Empresa");
    }

    @Test
    @DisplayName("noArgsConstructor cria instancia com campos nulos")
    void noArgsConstructor_criaInstanciaVazia() {
        Cliente cliente = new Cliente();

        assertThat(cliente.getId()).isNull();
        assertThat(cliente.getRazaoSocial()).isNull();
    }

    @Test
    @DisplayName("allArgsConstructor preenche todos os campos")
    void allArgsConstructor_preencheAllFields() {
        LocalDateTime now = LocalDateTime.now();
        Cliente cliente = new Cliente(
                1L,
                TipoPessoa.FISICA,
                "Razao Social",
                "Fantasia",
                "00.000.000/0001-00",
                "000.000.000-00",
                "IE123",
                "Segmento",
                LocalDate.of(2000, 1, 1),
                LocalDate.of(2020, 1, 1),
                StatusCliente.ATIVO,
                "folder-id",
                now,
                now,
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>()
        );

        assertThat(cliente.getId()).isEqualTo(1L);
        assertThat(cliente.getRazaoSocial()).isEqualTo("Razao Social");
    }

    @Test
    @DisplayName("onCreate define createdAt e updatedAt quando nulos")
    void onCreate_defineTimestamps() throws Exception {
        Cliente cliente = new Cliente();
        assertThat(cliente.getCreatedAt()).isNull();

        // Invocar metodo protegido via reflexao
        var method = Cliente.class.getDeclaredMethod("onCreate");
        method.setAccessible(true);
        method.invoke(cliente);

        assertThat(cliente.getCreatedAt()).isNotNull();
        assertThat(cliente.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("onCreate nao sobrescreve createdAt quando ja definido")
    void onCreate_naoSobreescreveCreatedAtExistente() throws Exception {
        LocalDateTime original = LocalDateTime.of(2023, 1, 1, 10, 0);
        Cliente cliente = new Cliente();
        cliente.setCreatedAt(original);

        var method = Cliente.class.getDeclaredMethod("onCreate");
        method.setAccessible(true);
        method.invoke(cliente);

        assertThat(cliente.getCreatedAt()).isEqualTo(original);
    }

    @Test
    @DisplayName("onUpdate atualiza updatedAt")
    void onUpdate_atualizaUpdatedAt() throws Exception {
        Cliente cliente = new Cliente();
        LocalDateTime before = LocalDateTime.now().minusDays(1);
        cliente.setUpdatedAt(before);

        var method = Cliente.class.getDeclaredMethod("onUpdate");
        method.setAccessible(true);
        method.invoke(cliente);

        assertThat(cliente.getUpdatedAt()).isAfter(before);
    }

    @Test
    @DisplayName("onCreate define status quando nulo")
    void onCreate_defineStatusQuandoNulo() throws Exception {
        Cliente cliente = new Cliente();
        // status is null by default with no-args constructor when Builder.Default not used
        // Set explicitly to null
        cliente.setStatus(null);

        var method = Cliente.class.getDeclaredMethod("onCreate");
        method.setAccessible(true);
        method.invoke(cliente);

        assertThat(cliente.getStatus()).isEqualTo(com.prediman.crm.model.enums.StatusCliente.ATIVO);
    }

    @Test
    @DisplayName("equals retorna false quando comparado com null")
    void equals_comNull_retornaFalse() {
        Cliente c = Cliente.builder().id(1L).razaoSocial("X").build();
        assertThat(c).isNotEqualTo(null);
    }

    @Test
    @DisplayName("equals retorna true quando comparado consigo mesmo")
    void equals_comSiMesmo_retornaTrue() {
        Cliente c = Cliente.builder().id(1L).razaoSocial("X").build();
        assertThat(c).isEqualTo(c);
    }
}
