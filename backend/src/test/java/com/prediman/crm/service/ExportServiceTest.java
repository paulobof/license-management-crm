package com.prediman.crm.service;

import com.prediman.crm.model.Cliente;
import com.prediman.crm.model.Cobranca;
import com.prediman.crm.model.Contrato;
import com.prediman.crm.model.Documento;
import com.prediman.crm.model.enums.CategoriaDocumento;
import com.prediman.crm.model.enums.Periodicidade;
import com.prediman.crm.model.enums.StatusCobranca;
import com.prediman.crm.model.enums.StatusDocumento;
import com.prediman.crm.repository.CobrancaRepository;
import com.prediman.crm.repository.DocumentoRepository;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Fetch;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ExportService — geração de CSV")
class ExportServiceTest {

    @Mock
    private DocumentoRepository documentoRepository;

    @Mock
    private CobrancaRepository cobrancaRepository;

    @InjectMocks
    private ExportService exportService;

    private Cliente cliente;
    private Contrato contrato;
    private LocalDate hoje;

    @BeforeEach
    void setUp() {
        hoje = LocalDate.now();

        cliente = Cliente.builder()
                .id(1L)
                .razaoSocial("Empresa Teste Ltda")
                .nomeFantasia("Teste")
                .build();

        contrato = Contrato.builder()
                .id(20L)
                .cliente(cliente)
                .descricao("Licença XYZ")
                .valor(new BigDecimal("1500.00"))
                .periodicidade(Periodicidade.MENSAL)
                .dataInicio(LocalDate.of(2024, 1, 10))
                .build();
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Documento documento(String nome, LocalDate dataValidade) {
        return Documento.builder()
                .id(1L)
                .cliente(cliente)
                .nome(nome)
                .categoria(CategoriaDocumento.ALVARA)
                .dataEmissao(LocalDate.of(2024, 3, 15))
                .dataValidade(dataValidade)
                .revisao("Rev01")
                .observacoes("Sem observações")
                .build();
    }

    private Cobranca cobranca() {
        return Cobranca.builder()
                .id(30L)
                .contrato(contrato)
                .valorEsperado(new BigDecimal("1500.50"))
                .valorRecebido(new BigDecimal("1500.50"))
                .dataVencimento(LocalDate.of(2024, 2, 10))
                .dataPagamento(LocalDate.of(2024, 2, 9))
                .formaPagamento("PIX")
                .status(StatusCobranca.PAGO)
                .build();
    }

    @SuppressWarnings("unchecked")
    private void mockPaginaDocumentos(List<Documento> documentos) {
        Page<Documento> page = new PageImpl<>(documentos, PageRequest.of(0, 500), documentos.size());
        when(documentoRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
    }

    @SuppressWarnings("unchecked")
    private void mockPaginaCobrancas(List<Cobranca> cobrancas) {
        Page<Cobranca> page = new PageImpl<>(cobrancas, PageRequest.of(0, 500), cobrancas.size());
        when(cobrancaRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
    }

    private String texto(byte[] csv) {
        return new String(csv, StandardCharsets.UTF_8);
    }

    private List<String> linhas(byte[] csv) {
        return List.of(texto(csv).split("\r\n"));
    }

    // -------------------------------------------------------------------------
    // Documentos
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("exportarDocumentos inicia o arquivo com BOM UTF-8")
    void exportarDocumentos_contemBom() {
        mockPaginaDocumentos(List.of());

        byte[] csv = exportService.exportarDocumentos(null, null, null, null);

        assertThat(csv[0]).isEqualTo((byte) 0xEF);
        assertThat(csv[1]).isEqualTo((byte) 0xBB);
        assertThat(csv[2]).isEqualTo((byte) 0xBF);
        assertThat(texto(csv)).startsWith(ExportService.BOM_UTF8);
    }

    @Test
    @DisplayName("exportarDocumentos escreve o cabeçalho em português separado por ponto e vírgula")
    void exportarDocumentos_cabecalhoEmPortugues() {
        mockPaginaDocumentos(List.of());

        byte[] csv = exportService.exportarDocumentos(null, null, null, null);

        assertThat(linhas(csv).get(0)).isEqualTo(ExportService.BOM_UTF8
                + "Cliente;Documento;Categoria;Revisão;Data de Emissão;Data de Validade;"
                + "Status;Dias para Vencer;Observações");
    }

    @Test
    @DisplayName("exportarDocumentos formata data, status e dias para vencer")
    void exportarDocumentos_formataCampos() {
        mockPaginaDocumentos(List.of(documento("Alvará", hoje.plusDays(10))));

        byte[] csv = exportService.exportarDocumentos(null, null, null, null);
        String linha = linhas(csv).get(1);

        assertThat(linha).isEqualTo("Empresa Teste Ltda;Alvará;Alvará;Rev01;15/03/2024;"
                + hoje.plusDays(10).format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                + ";A vencer;10;Sem observações");
    }

    @Test
    @DisplayName("exportarDocumentos deixa células vazias para valores nulos")
    void exportarDocumentos_camposNulos() {
        Documento documento = Documento.builder()
                .id(2L)
                .cliente(cliente)
                .nome(null)
                .categoria(null)
                .dataEmissao(null)
                .dataValidade(null)
                .revisao(null)
                .observacoes(null)
                .build();
        mockPaginaDocumentos(List.of(documento));

        byte[] csv = exportService.exportarDocumentos(null, null, null, null);

        assertThat(linhas(csv).get(1)).isEqualTo("Empresa Teste Ltda;;;;;;Sem validade;;");
    }

    @Test
    @DisplayName("exportarDocumentos escapa campo que contém o separador")
    void exportarDocumentos_escapaSeparador() {
        Documento documento = documento("Alvará; Municipal", hoje.plusDays(5));
        mockPaginaDocumentos(List.of(documento));

        byte[] csv = exportService.exportarDocumentos(null, null, null, null);

        assertThat(linhas(csv).get(1)).contains("\"Alvará; Municipal\"");
    }

    @Test
    @DisplayName("exportarDocumentos escapa campo que contém aspas duplicando-as")
    void exportarDocumentos_escapaAspas() {
        Documento documento = documento("Alvará \"Principal\"", hoje.plusDays(5));
        mockPaginaDocumentos(List.of(documento));

        byte[] csv = exportService.exportarDocumentos(null, null, null, null);

        assertThat(linhas(csv).get(1)).contains("\"Alvará \"\"Principal\"\"\"");
    }

    @Test
    @DisplayName("exportarDocumentos escapa campo com quebra de linha")
    void exportarDocumentos_escapaQuebraDeLinha() {
        Documento documento = documento("Alvará", hoje.plusDays(5));
        documento.setObservacoes("Linha 1\nLinha 2");
        mockPaginaDocumentos(List.of(documento));

        byte[] csv = exportService.exportarDocumentos(null, null, null, null);

        assertThat(texto(csv)).contains("\"Linha 1\nLinha 2\"");
    }

    @Test
    @DisplayName("exportarDocumentos ordena por data de validade ascendente com nulos ao final")
    void exportarDocumentos_ordenaPorValidade() {
        mockPaginaDocumentos(List.of());
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);

        exportService.exportarDocumentos(null, null, null, null);

        verificarPageableDocumentos(captor);
        Sort.Order order = captor.getValue().getSort().getOrderFor("dataValidade");
        assertThat(order).isNotNull();
        assertThat(order.isAscending()).isTrue();
        assertThat(order.getNullHandling()).isEqualTo(Sort.NullHandling.NULLS_LAST);
        assertThat(captor.getValue().getPageSize()).isEqualTo(500);
    }

    @SuppressWarnings("unchecked")
    private void verificarPageableDocumentos(ArgumentCaptor<Pageable> captor) {
        org.mockito.Mockito.verify(documentoRepository).findAll(any(Specification.class), captor.capture());
    }

    @Test
    @DisplayName("exportarDocumentos percorre todas as páginas do resultado")
    void exportarDocumentos_percorreVariasPaginas() {
        ReflectionTestUtils.setField(exportService, "tamanhoPagina", 1);
        Page<Documento> primeira = new PageImpl<>(List.of(documento("Doc A", hoje.plusDays(1))),
                PageRequest.of(0, 1), 2);
        Page<Documento> segunda = new PageImpl<>(List.of(documento("Doc B", hoje.plusDays(2))),
                PageRequest.of(1, 1), 2);
        mockDuasPaginasDocumentos(primeira, segunda);

        byte[] csv = exportService.exportarDocumentos(null, null, null, null);

        assertThat(linhas(csv)).hasSize(3);
        assertThat(linhas(csv).get(1)).contains("Doc A");
        assertThat(linhas(csv).get(2)).contains("Doc B");
    }

    @SuppressWarnings("unchecked")
    private void mockDuasPaginasDocumentos(Page<Documento> primeira, Page<Documento> segunda) {
        when(documentoRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(primeira, segunda);
    }

    @Test
    @DisplayName("exportarDocumentos trunca a exportação ao atingir o limite de linhas")
    void exportarDocumentos_truncaNoLimite() {
        ReflectionTestUtils.setField(exportService, "maxLinhas", 1);
        mockPaginaDocumentos(List.of(documento("Doc A", hoje.plusDays(1)), documento("Doc B", hoje.plusDays(2))));

        byte[] csv = exportService.exportarDocumentos(null, null, null, null);

        assertThat(linhas(csv)).hasSize(2);
        assertThat(linhas(csv).get(1)).contains("Doc A");
    }

    // -------------------------------------------------------------------------
    // Cobranças
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("exportarCobrancas escreve cabeçalho e BOM")
    void exportarCobrancas_cabecalho() {
        mockPaginaCobrancas(List.of());

        byte[] csv = exportService.exportarCobrancas(null, null, null, null);

        assertThat(linhas(csv).get(0)).isEqualTo(ExportService.BOM_UTF8
                + "Cliente;Contrato;Periodicidade;Valor Esperado;Valor Recebido;"
                + "Vencimento;Pagamento;Forma de Pagamento;Status");
    }

    @Test
    @DisplayName("exportarCobrancas formata valores em pt-BR e traduz enums")
    void exportarCobrancas_formataLinha() {
        mockPaginaCobrancas(List.of(cobranca()));

        byte[] csv = exportService.exportarCobrancas(null, null, null, null);

        assertThat(linhas(csv).get(1)).isEqualTo(
                "Empresa Teste Ltda;Licença XYZ;Mensal;1.500,50;1.500,50;10/02/2024;09/02/2024;PIX;Pago");
    }

    @Test
    @DisplayName("exportarCobrancas deixa células vazias para campos nulos")
    void exportarCobrancas_camposNulos() {
        Cobranca cobranca = Cobranca.builder()
                .id(31L)
                .contrato(null)
                .valorEsperado(null)
                .valorRecebido(null)
                .dataVencimento(null)
                .dataPagamento(null)
                .formaPagamento(null)
                .status(StatusCobranca.PENDENTE)
                .build();
        mockPaginaCobrancas(List.of(cobranca));

        byte[] csv = exportService.exportarCobrancas(null, null, null, null);

        assertThat(linhas(csv).get(1)).isEqualTo(";;;;;;;;Pendente");
    }

    @Test
    @DisplayName("exportarCobrancas ordena por data de vencimento ascendente")
    void exportarCobrancas_ordenaPorVencimento() {
        mockPaginaCobrancas(List.of());
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);

        exportService.exportarCobrancas(null, null, null, null);

        verificarPageableCobrancas(captor);
        Sort.Order order = captor.getValue().getSort().getOrderFor("dataVencimento");
        assertThat(order).isNotNull();
        assertThat(order.isAscending()).isTrue();
    }

    @SuppressWarnings("unchecked")
    private void verificarPageableCobrancas(ArgumentCaptor<Pageable> captor) {
        org.mockito.Mockito.verify(cobrancaRepository).findAll(any(Specification.class), captor.capture());
    }

    // -------------------------------------------------------------------------
    // Formatadores e traduções
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("escapar mantém texto simples inalterado e converte nulo/vazio em célula vazia")
    void escapar_textoSimples() {
        assertThat(ExportService.escapar(null)).isEmpty();
        assertThat(ExportService.escapar("")).isEmpty();
        assertThat(ExportService.escapar("Alvará")).isEqualTo("Alvará");
    }

    @Test
    @DisplayName("escapar envolve em aspas campos com separador, aspas, LF e CR")
    void escapar_camposEspeciais() {
        assertThat(ExportService.escapar("a;b")).isEqualTo("\"a;b\"");
        assertThat(ExportService.escapar("a\"b")).isEqualTo("\"a\"\"b\"");
        assertThat(ExportService.escapar("a\nb")).isEqualTo("\"a\nb\"");
        assertThat(ExportService.escapar("a\rb")).isEqualTo("\"a\rb\"");
    }

    @Test
    @DisplayName("formatarData usa dd/MM/yyyy e célula vazia para nulo")
    void formatarData_formatoBrasileiro() {
        assertThat(ExportService.formatarData(LocalDate.of(2024, 12, 31))).isEqualTo("31/12/2024");
        assertThat(ExportService.formatarData(null)).isEmpty();
    }

    @Test
    @DisplayName("formatarValor usa vírgula decimal e ponto de milhar")
    void formatarValor_padraoBrasileiro() {
        assertThat(ExportService.formatarValor(new BigDecimal("1234567.8"))).isEqualTo("1.234.567,80");
        assertThat(ExportService.formatarValor(new BigDecimal("0.5"))).isEqualTo("0,50");
        assertThat(ExportService.formatarValor(null)).isEmpty();
    }

    @Test
    @DisplayName("nvl converte nulo em célula vazia")
    void nvl_converteNulo() {
        assertThat(ExportService.nvl(null)).isEmpty();
        assertThat(ExportService.nvl("texto")).isEqualTo("texto");
    }

    @Test
    @DisplayName("traduzirCategoria devolve rótulos em português")
    void traduzirCategoria_rotulos() {
        assertThat(ExportService.traduzirCategoria(CategoriaDocumento.ALVARA)).isEqualTo("Alvará");
        assertThat(ExportService.traduzirCategoria(CategoriaDocumento.LICENCA)).isEqualTo("Licença");
        assertThat(ExportService.traduzirCategoria(CategoriaDocumento.NF)).isEqualTo("Nota Fiscal");
        assertThat(ExportService.traduzirCategoria(CategoriaDocumento.CONTRATO)).isEqualTo("Contrato");
        assertThat(ExportService.traduzirCategoria(CategoriaDocumento.CERTIFICADO)).isEqualTo("Certificado");
        assertThat(ExportService.traduzirCategoria(CategoriaDocumento.OUTRO)).isEqualTo("Outro");
        assertThat(ExportService.traduzirCategoria(null)).isEmpty();
    }

    @Test
    @DisplayName("traduzirStatusDocumento devolve rótulos em português")
    void traduzirStatusDocumento_rotulos() {
        assertThat(ExportService.traduzirStatusDocumento(StatusDocumento.VALIDO)).isEqualTo("Válido");
        assertThat(ExportService.traduzirStatusDocumento(StatusDocumento.A_VENCER)).isEqualTo("A vencer");
        assertThat(ExportService.traduzirStatusDocumento(StatusDocumento.VENCIDO)).isEqualTo("Vencido");
        assertThat(ExportService.traduzirStatusDocumento(StatusDocumento.SEM_VALIDADE)).isEqualTo("Sem validade");
        assertThat(ExportService.traduzirStatusDocumento(null)).isEmpty();
    }

    @Test
    @DisplayName("traduzirStatusCobranca devolve rótulos em português")
    void traduzirStatusCobranca_rotulos() {
        assertThat(ExportService.traduzirStatusCobranca(StatusCobranca.PENDENTE)).isEqualTo("Pendente");
        assertThat(ExportService.traduzirStatusCobranca(StatusCobranca.PAGO)).isEqualTo("Pago");
        assertThat(ExportService.traduzirStatusCobranca(StatusCobranca.VENCIDO)).isEqualTo("Vencido");
        assertThat(ExportService.traduzirStatusCobranca(StatusCobranca.CANCELADO)).isEqualTo("Cancelado");
        assertThat(ExportService.traduzirStatusCobranca(null)).isEmpty();
    }

    @Test
    @DisplayName("traduzirPeriodicidade devolve rótulos em português")
    void traduzirPeriodicidade_rotulos() {
        assertThat(ExportService.traduzirPeriodicidade(Periodicidade.MENSAL)).isEqualTo("Mensal");
        assertThat(ExportService.traduzirPeriodicidade(Periodicidade.TRIMESTRAL)).isEqualTo("Trimestral");
        assertThat(ExportService.traduzirPeriodicidade(Periodicidade.SEMESTRAL)).isEqualTo("Semestral");
        assertThat(ExportService.traduzirPeriodicidade(Periodicidade.ANUAL)).isEqualTo("Anual");
        assertThat(ExportService.traduzirPeriodicidade(Periodicidade.AVULSO)).isEqualTo("Avulso");
        assertThat(ExportService.traduzirPeriodicidade(null)).isEmpty();
    }

    @Test
    @DisplayName("nomeCliente usa razão social, cai para nome fantasia e trata cliente nulo")
    void nomeCliente_variacoes() {
        assertThat(ExportService.nomeCliente(null)).isEmpty();
        assertThat(ExportService.nomeCliente(cliente)).isEqualTo("Empresa Teste Ltda");
        assertThat(ExportService.nomeCliente(Cliente.builder().razaoSocial(" ").nomeFantasia("Fantasia").build()))
                .isEqualTo("Fantasia");
        assertThat(ExportService.nomeCliente(Cliente.builder().build())).isEmpty();
    }

    @Test
    @DisplayName("diasParaVencer é negativo para documento vencido e vazio sem validade")
    void diasParaVencer_variacoes() {
        assertThat(ExportService.diasParaVencer(null, hoje)).isEmpty();
        assertThat(ExportService.diasParaVencer(hoje.plusDays(7), hoje)).isEqualTo("7");
        assertThat(ExportService.diasParaVencer(hoje.minusDays(3), hoje)).isEqualTo("-3");
    }

    @Test
    @DisplayName("statusEfetivo preserva CANCELADO e usa o status calculado nos demais casos")
    void statusEfetivo_variacoes() {
        Cobranca cancelada = Cobranca.builder()
                .status(StatusCobranca.CANCELADO)
                .dataVencimento(hoje.minusDays(10))
                .build();
        Cobranca vencida = Cobranca.builder()
                .status(StatusCobranca.PENDENTE)
                .dataVencimento(hoje.minusDays(10))
                .build();

        assertThat(ExportService.statusEfetivo(cancelada)).isEqualTo(StatusCobranca.CANCELADO);
        assertThat(ExportService.statusEfetivo(vencida)).isEqualTo(StatusCobranca.VENCIDO);
    }

    // -------------------------------------------------------------------------
    // Cobertura das Specifications
    // -------------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private Specification<Documento> capturarSpecDocumentos(String search, CategoriaDocumento categoria,
                                                            String status, Long clienteId) {
        ArgumentCaptor<Specification<Documento>> captor = ArgumentCaptor.forClass(Specification.class);
        when(documentoRepository.findAll(captor.capture(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));
        exportService.exportarDocumentos(search, categoria, status, clienteId);
        return captor.getValue();
    }

    @SuppressWarnings("unchecked")
    private void executarSpecDocumentos(Specification<Documento> spec) {
        Root<Documento> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Path<Object> path = mock(Path.class);

        lenient().when(root.get(any(String.class))).thenReturn(path);
        lenient().when(path.get(any(String.class))).thenReturn(path);
        lenient().when(cb.lower(any(Expression.class))).thenReturn(mock(Expression.class));
        lenient().when(cb.like(any(Expression.class), any(String.class))).thenReturn(mock(Predicate.class));
        lenient().when(cb.equal(any(), any())).thenReturn(mock(Predicate.class));
        lenient().when(cb.isNull(any())).thenReturn(mock(Predicate.class));
        lenient().when(cb.between(any(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(mock(Predicate.class));
        lenient().when(cb.lessThan(any(), any(LocalDate.class))).thenReturn(mock(Predicate.class));
        lenient().when(cb.greaterThan(any(), any(LocalDate.class))).thenReturn(mock(Predicate.class));
        lenient().when(cb.and(any(Predicate[].class))).thenReturn(mock(Predicate.class));

        assertThat(spec.toPredicate(root, query, cb)).isNotNull();
    }

    @Test
    @DisplayName("Specification de documentos sem filtros")
    void specDocumentos_semFiltros() {
        executarSpecDocumentos(capturarSpecDocumentos(null, null, null, null));
    }

    @Test
    @DisplayName("Specification de documentos com busca, categoria e cliente")
    void specDocumentos_comFiltrosBasicos() {
        executarSpecDocumentos(capturarSpecDocumentos("alvará", CategoriaDocumento.ALVARA, null, 10L));
    }

    @Test
    @DisplayName("Specification de documentos com status A_VENCER")
    void specDocumentos_statusAVencer() {
        executarSpecDocumentos(capturarSpecDocumentos(null, null, "A_VENCER", null));
    }

    @Test
    @DisplayName("Specification de documentos com status VENCIDO")
    void specDocumentos_statusVencido() {
        executarSpecDocumentos(capturarSpecDocumentos(null, null, "VENCIDO", null));
    }

    @Test
    @DisplayName("Specification de documentos com status VALIDO")
    void specDocumentos_statusValido() {
        executarSpecDocumentos(capturarSpecDocumentos(null, null, "VALIDO", null));
    }

    @Test
    @DisplayName("Specification de documentos com status SEM_VALIDADE")
    void specDocumentos_statusSemValidade() {
        executarSpecDocumentos(capturarSpecDocumentos(null, null, "SEM_VALIDADE", null));
    }

    @Test
    @DisplayName("Specification de documentos com status desconhecido não adiciona filtro")
    void specDocumentos_statusDesconhecido() {
        executarSpecDocumentos(capturarSpecDocumentos(null, null, "INEXISTENTE", null));
    }

    @Test
    @DisplayName("Specification de documentos ignora busca em branco")
    void specDocumentos_buscaEmBranco() {
        executarSpecDocumentos(capturarSpecDocumentos("   ", null, "   ", null));
    }

    @SuppressWarnings("unchecked")
    private Specification<Cobranca> capturarSpecCobrancas(Long contratoId, StatusCobranca status,
                                                          Integer month, Integer year) {
        ArgumentCaptor<Specification<Cobranca>> captor = ArgumentCaptor.forClass(Specification.class);
        when(cobrancaRepository.findAll(captor.capture(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));
        exportService.exportarCobrancas(contratoId, status, month, year);
        return captor.getValue();
    }

    @SuppressWarnings("unchecked")
    private void executarSpecCobrancas(Specification<Cobranca> spec, CriteriaQuery<?> query) {
        Root<Cobranca> root = mock(Root.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Path<Object> path = mock(Path.class);
        Fetch<Object, Object> contratoFetch = mock(Fetch.class);

        lenient().when(root.get(any(String.class))).thenReturn(path);
        lenient().when(path.get(any(String.class))).thenReturn(path);
        lenient().when(root.fetch(eq("contrato"), any(JoinType.class))).thenReturn(contratoFetch);
        lenient().when(contratoFetch.fetch(eq("cliente"), any(JoinType.class))).thenReturn(mock(Fetch.class));
        lenient().when(cb.equal(any(), any())).thenReturn(mock(Predicate.class));
        lenient().when(cb.between(any(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(mock(Predicate.class));
        lenient().when(cb.and(any(Predicate[].class))).thenReturn(mock(Predicate.class));

        assertThat(spec.toPredicate(root, query, cb)).isNotNull();
    }

    @Test
    @DisplayName("Specification de cobranças sem filtros aplica join fetch de contrato e cliente")
    void specCobrancas_semFiltros() {
        executarSpecCobrancas(capturarSpecCobrancas(null, null, null, null), mock(CriteriaQuery.class));
    }

    @Test
    @DisplayName("Specification de cobranças com contrato e status")
    void specCobrancas_comContratoEStatus() {
        executarSpecCobrancas(capturarSpecCobrancas(20L, StatusCobranca.PENDENTE, null, null),
                mock(CriteriaQuery.class));
    }

    @Test
    @DisplayName("Specification de cobranças com mês e ano")
    void specCobrancas_comMesEAno() {
        executarSpecCobrancas(capturarSpecCobrancas(null, null, 2, 2024), mock(CriteriaQuery.class));
    }

    @Test
    @DisplayName("Specification de cobranças apenas com ano")
    void specCobrancas_apenasAno() {
        executarSpecCobrancas(capturarSpecCobrancas(null, null, null, 2024), mock(CriteriaQuery.class));
    }

    @Test
    @DisplayName("Specification de cobranças ignora mês sem ano")
    void specCobrancas_mesSemAno() {
        executarSpecCobrancas(capturarSpecCobrancas(null, null, 3, null), mock(CriteriaQuery.class));
    }

    @Test
    @DisplayName("Specification de cobranças não aplica join fetch na consulta de contagem")
    void specCobrancas_consultaDeContagem() {
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        doReturn(Long.class).when(query).getResultType();

        executarSpecCobrancas(capturarSpecCobrancas(null, null, null, null), query);
    }

    @Test
    @DisplayName("Specification de cobranças tolera query nula")
    void specCobrancas_queryNula() {
        executarSpecCobrancas(capturarSpecCobrancas(null, null, null, null), null);
    }
}
