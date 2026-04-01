package com.prediman.crm.service;

import com.prediman.crm.dto.ClienteRequest;
import com.prediman.crm.dto.ClienteResponse;
import com.prediman.crm.dto.ContatoDTO;
import com.prediman.crm.dto.EnderecoDTO;
import com.prediman.crm.model.Cliente;
import com.prediman.crm.model.Contato;
import com.prediman.crm.model.Endereco;
import com.prediman.crm.model.enums.StatusCliente;
import com.prediman.crm.model.enums.TipoEndereco;
import com.prediman.crm.model.enums.TipoPessoa;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ClienteMapper")
class ClienteMapperTest {

    private ClienteMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ClienteMapper();
    }

    // ---------------------------------------------------------------
    // toEntity
    // ---------------------------------------------------------------

    @Test
    @DisplayName("toEntity mapeia todos os campos de ClienteRequest")
    void toEntity_mapeiaTodasOsCampos() {
        ContatoDTO contatoDTO = ContatoDTO.builder()
                .nome("Ana Lima")
                .cargo("Diretora")
                .email("ana@empresa.com")
                .telefone("11999990000")
                .whatsapp("11999990000")
                .principal(true)
                .build();

        EnderecoDTO enderecoDTO = EnderecoDTO.builder()
                .tipo(TipoEndereco.ENTREGA)
                .cep("01310-100")
                .logradouro("Av. Paulista")
                .numero("1000")
                .complemento("Apto 10")
                .bairro("Bela Vista")
                .cidade("São Paulo")
                .estado("SP")
                .build();

        ClienteRequest request = ClienteRequest.builder()
                .tipoPessoa("JURIDICA")
                .razaoSocial("Empresa XYZ Ltda")
                .nomeFantasia("XYZ")
                .cnpj("12.345.678/0001-90")
                .cpf(null)
                .ie("123456789")
                .segmento("Tecnologia")
                .dataFundacao(LocalDate.of(2010, 1, 15))
                .dataInicioCliente(LocalDate.of(2020, 6, 1))
                .googleDriveFolderId("folder-abc123")
                .contatos(List.of(contatoDTO))
                .enderecos(List.of(enderecoDTO))
                .build();

        Cliente entity = mapper.toEntity(request);

        assertAll("campos escalares",
                () -> assertEquals(TipoPessoa.JURIDICA, entity.getTipoPessoa()),
                () -> assertEquals("Empresa XYZ Ltda", entity.getRazaoSocial()),
                () -> assertEquals("XYZ", entity.getNomeFantasia()),
                () -> assertEquals("12.345.678/0001-90", entity.getCnpj()),
                () -> assertNull(entity.getCpf()),
                () -> assertEquals("123456789", entity.getIe()),
                () -> assertEquals("Tecnologia", entity.getSegmento()),
                () -> assertEquals(LocalDate.of(2010, 1, 15), entity.getDataFundacao()),
                () -> assertEquals(LocalDate.of(2020, 6, 1), entity.getDataInicioCliente()),
                () -> assertEquals("folder-abc123", entity.getGoogleDriveFolderId())
        );

        assertAll("contato mapeado",
                () -> assertEquals(1, entity.getContatos().size()),
                () -> assertEquals("Ana Lima", entity.getContatos().get(0).getNome()),
                () -> assertEquals(entity, entity.getContatos().get(0).getCliente())
        );

        assertAll("endereco mapeado",
                () -> assertEquals(1, entity.getEnderecos().size()),
                () -> assertEquals(TipoEndereco.ENTREGA, entity.getEnderecos().get(0).getTipo()),
                () -> assertEquals(entity, entity.getEnderecos().get(0).getCliente())
        );
    }

    @Test
    @DisplayName("toEntity usa JURIDICA como default quando tipoPessoa e null")
    void toEntity_defaultTipoPessoaJuridica() {
        ClienteRequest request = ClienteRequest.builder()
                .tipoPessoa(null)
                .razaoSocial("Empresa Sem Tipo")
                .build();

        Cliente entity = mapper.toEntity(request);

        assertEquals(TipoPessoa.JURIDICA, entity.getTipoPessoa());
    }

    @Test
    @DisplayName("toEntity usa JURIDICA como default quando tipoPessoa e string vazia")
    void toEntity_defaultTipoPessoaJuridicaParaStringVazia() {
        ClienteRequest request = ClienteRequest.builder()
                .tipoPessoa("")
                .razaoSocial("Empresa Sem Tipo")
                .build();

        Cliente entity = mapper.toEntity(request);

        assertEquals(TipoPessoa.JURIDICA, entity.getTipoPessoa());
    }

    @Test
    @DisplayName("toEntity mapeia FISICA quando tipoPessoa e FISICA")
    void toEntity_mapeiaFisica() {
        ClienteRequest request = ClienteRequest.builder()
                .tipoPessoa("FISICA")
                .razaoSocial("João Silva")
                .cpf("123.456.789-09")
                .build();

        Cliente entity = mapper.toEntity(request);

        assertEquals(TipoPessoa.FISICA, entity.getTipoPessoa());
        assertEquals("123.456.789-09", entity.getCpf());
    }

    @Test
    @DisplayName("toEntity trata colecao de contatos nula sem lancar excecao")
    void toEntity_contatosNulos() {
        ClienteRequest request = ClienteRequest.builder()
                .razaoSocial("Empresa Sem Contatos")
                .contatos(null)
                .enderecos(null)
                .build();

        Cliente entity = mapper.toEntity(request);

        assertNotNull(entity.getContatos());
        assertTrue(entity.getContatos().isEmpty());
        assertTrue(entity.getEnderecos().isEmpty());
    }

    // ---------------------------------------------------------------
    // toResponse
    // ---------------------------------------------------------------

    @Test
    @DisplayName("toResponse mapeia todos os campos de Cliente entity")
    void toResponse_mapeiaTodasOsCampos() {
        LocalDateTime now = LocalDateTime.of(2024, 3, 10, 8, 0);

        Contato contato = Contato.builder()
                .id(10L)
                .nome("Carlos Souza")
                .cargo("Gerente")
                .email("carlos@empresa.com")
                .telefone("11988880000")
                .whatsapp("11988880000")
                .principal(true)
                .build();

        Endereco endereco = Endereco.builder()
                .id(20L)
                .tipo(TipoEndereco.COBRANCA)
                .cep("04538-133")
                .logradouro("Rua Fidêncio Ramos")
                .numero("302")
                .complemento("Cj 74")
                .bairro("Vila Olímpia")
                .cidade("São Paulo")
                .estado("SP")
                .build();

        Cliente cliente = Cliente.builder()
                .id(1L)
                .tipoPessoa(TipoPessoa.JURIDICA)
                .razaoSocial("Empresa ABC Ltda")
                .nomeFantasia("ABC")
                .cnpj("99.999.999/0001-99")
                .cpf(null)
                .ie("9876543")
                .segmento("Saúde")
                .dataFundacao(LocalDate.of(2005, 5, 20))
                .dataInicioCliente(LocalDate.of(2021, 1, 1))
                .status(StatusCliente.ATIVO)
                .googleDriveFolderId("drive-folder-xyz")
                .createdAt(now)
                .updatedAt(now)
                .contatos(new java.util.ArrayList<>(List.of(contato)))
                .enderecos(new java.util.ArrayList<>(List.of(endereco)))
                .build();

        ClienteResponse response = mapper.toResponse(cliente);

        assertAll("campos escalares",
                () -> assertEquals(1L, response.getId()),
                () -> assertEquals(TipoPessoa.JURIDICA, response.getTipoPessoa()),
                () -> assertEquals("Empresa ABC Ltda", response.getRazaoSocial()),
                () -> assertEquals("ABC", response.getNomeFantasia()),
                () -> assertEquals("99.999.999/0001-99", response.getCnpj()),
                () -> assertNull(response.getCpf()),
                () -> assertEquals("9876543", response.getIe()),
                () -> assertEquals("Saúde", response.getSegmento()),
                () -> assertEquals(LocalDate.of(2005, 5, 20), response.getDataFundacao()),
                () -> assertEquals(LocalDate.of(2021, 1, 1), response.getDataInicioCliente()),
                () -> assertEquals(StatusCliente.ATIVO, response.getStatus()),
                () -> assertEquals("drive-folder-xyz", response.getGoogleDriveFolderId()),
                () -> assertEquals(now, response.getCreatedAt()),
                () -> assertEquals(now, response.getUpdatedAt())
        );

        assertAll("contato na response",
                () -> assertEquals(1, response.getContatos().size()),
                () -> assertEquals(10L, response.getContatos().get(0).getId()),
                () -> assertEquals("Carlos Souza", response.getContatos().get(0).getNome())
        );

        assertAll("endereco na response",
                () -> assertEquals(1, response.getEnderecos().size()),
                () -> assertEquals(20L, response.getEnderecos().get(0).getId()),
                () -> assertEquals(TipoEndereco.COBRANCA, response.getEnderecos().get(0).getTipo())
        );
    }

    @Test
    @DisplayName("toResponse retorna listas vazias quando contatos e enderecos sao null na entity")
    void toResponse_colecaoNulaTrataGraciosamente() {
        Cliente cliente = Cliente.builder()
                .id(2L)
                .razaoSocial("Empresa Vazia")
                .contatos(null)
                .enderecos(null)
                .build();

        ClienteResponse response = mapper.toResponse(cliente);

        assertNotNull(response.getContatos());
        assertNotNull(response.getEnderecos());
        assertTrue(response.getContatos().isEmpty());
        assertTrue(response.getEnderecos().isEmpty());
    }

    // ---------------------------------------------------------------
    // toContatoEntity / toContatoDTO
    // ---------------------------------------------------------------

    @Test
    @DisplayName("toContatoEntity mapeia todos os campos de ContatoDTO")
    void toContatoEntity_mapeiaTodasOsCampos() {
        ContatoDTO dto = ContatoDTO.builder()
                .nome("Luisa Fernandes")
                .cargo("Analista")
                .email("luisa@exemplo.com")
                .telefone("21999990001")
                .whatsapp("21999990001")
                .principal(false)
                .build();

        Contato entity = mapper.toContatoEntity(dto);

        assertAll(
                () -> assertEquals("Luisa Fernandes", entity.getNome()),
                () -> assertEquals("Analista", entity.getCargo()),
                () -> assertEquals("luisa@exemplo.com", entity.getEmail()),
                () -> assertEquals("21999990001", entity.getTelefone()),
                () -> assertEquals("21999990001", entity.getWhatsapp()),
                () -> assertFalse(entity.getPrincipal())
        );
    }

    @Test
    @DisplayName("toContatoEntity usa false quando principal e null")
    void toContatoEntity_principalNullVirarFalse() {
        ContatoDTO dto = ContatoDTO.builder().nome("Teste").principal(null).build();

        Contato entity = mapper.toContatoEntity(dto);

        assertFalse(entity.getPrincipal());
    }

    @Test
    @DisplayName("toContatoDTO mapeia todos os campos de Contato entity")
    void toContatoDTO_mapeiaTodasOsCampos() {
        Contato entity = Contato.builder()
                .id(5L)
                .nome("Roberto Alves")
                .cargo("CEO")
                .email("roberto@firma.com")
                .telefone("31988887777")
                .whatsapp("31988887777")
                .principal(true)
                .build();

        ContatoDTO dto = mapper.toContatoDTO(entity);

        assertAll(
                () -> assertEquals(5L, dto.getId()),
                () -> assertEquals("Roberto Alves", dto.getNome()),
                () -> assertEquals("CEO", dto.getCargo()),
                () -> assertEquals("roberto@firma.com", dto.getEmail()),
                () -> assertEquals("31988887777", dto.getTelefone()),
                () -> assertEquals("31988887777", dto.getWhatsapp()),
                () -> assertTrue(dto.getPrincipal())
        );
    }

    // ---------------------------------------------------------------
    // toEnderecoEntity / toEnderecoDTO
    // ---------------------------------------------------------------

    @Test
    @DisplayName("toEnderecoEntity mapeia todos os campos de EnderecoDTO")
    void toEnderecoEntity_mapeiaTodasOsCampos() {
        EnderecoDTO dto = EnderecoDTO.builder()
                .tipo(TipoEndereco.FILIAL)
                .cep("30140-010")
                .logradouro("Av. Afonso Pena")
                .numero("555")
                .complemento("Sala 1")
                .bairro("Centro")
                .cidade("Belo Horizonte")
                .estado("MG")
                .build();

        Endereco entity = mapper.toEnderecoEntity(dto);

        assertAll(
                () -> assertEquals(TipoEndereco.FILIAL, entity.getTipo()),
                () -> assertEquals("30140-010", entity.getCep()),
                () -> assertEquals("Av. Afonso Pena", entity.getLogradouro()),
                () -> assertEquals("555", entity.getNumero()),
                () -> assertEquals("Sala 1", entity.getComplemento()),
                () -> assertEquals("Centro", entity.getBairro()),
                () -> assertEquals("Belo Horizonte", entity.getCidade()),
                () -> assertEquals("MG", entity.getEstado())
        );
    }

    @Test
    @DisplayName("toEnderecoEntity usa COBRANCA como default quando tipo e null")
    void toEnderecoEntity_defaultTipoCobranca() {
        EnderecoDTO dto = EnderecoDTO.builder().tipo(null).cep("00000-000").build();

        Endereco entity = mapper.toEnderecoEntity(dto);

        assertEquals(TipoEndereco.COBRANCA, entity.getTipo());
    }

    @Test
    @DisplayName("toEnderecoDTO mapeia todos os campos de Endereco entity")
    void toEnderecoDTO_mapeiaTodasOsCampos() {
        Endereco entity = Endereco.builder()
                .id(7L)
                .tipo(TipoEndereco.ENTREGA)
                .cep("90040-060")
                .logradouro("Rua dos Andradas")
                .numero("1234")
                .complemento("Bloco B")
                .bairro("Centro Histórico")
                .cidade("Porto Alegre")
                .estado("RS")
                .build();

        EnderecoDTO dto = mapper.toEnderecoDTO(entity);

        assertAll(
                () -> assertEquals(7L, dto.getId()),
                () -> assertEquals(TipoEndereco.ENTREGA, dto.getTipo()),
                () -> assertEquals("90040-060", dto.getCep()),
                () -> assertEquals("Rua dos Andradas", dto.getLogradouro()),
                () -> assertEquals("1234", dto.getNumero()),
                () -> assertEquals("Bloco B", dto.getComplemento()),
                () -> assertEquals("Centro Histórico", dto.getBairro()),
                () -> assertEquals("Porto Alegre", dto.getCidade()),
                () -> assertEquals("RS", dto.getEstado())
        );
    }
}
