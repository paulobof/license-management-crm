package com.prediman.crm.service;

import com.prediman.crm.model.Cliente;
import com.prediman.crm.model.Cobranca;
import com.prediman.crm.model.Contrato;
import com.prediman.crm.model.Documento;
import com.prediman.crm.model.DocumentoConstants;
import com.prediman.crm.model.enums.CategoriaDocumento;
import com.prediman.crm.model.enums.Periodicidade;
import com.prediman.crm.model.enums.StatusCobranca;
import com.prediman.crm.model.enums.StatusDocumento;
import com.prediman.crm.repository.CobrancaRepository;
import com.prediman.crm.repository.DocumentoRepository;
import jakarta.persistence.criteria.Fetch;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Geração de arquivos CSV para exportação em planilha (Excel pt-BR).
 *
 * <p>O CSV é escrito manualmente: separador ponto e vírgula, BOM UTF-8 no início
 * e quebra de linha CRLF — combinação reconhecida pelo Excel em português.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExportService {

    /** Marca de ordem de byte (BOM) UTF-8; sem ela o Excel pt-BR ignora a acentuação. */
    static final String BOM_UTF8 = "\uFEFF";

    /** Separador de colunas esperado pelo Excel em português. */
    static final char SEPARADOR = ';';

    /** Quebra de linha CRLF, conforme RFC 4180. */
    static final String QUEBRA_LINHA = "\r\n";

    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");
    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final List<String> CABECALHO_DOCUMENTOS = List.of(
            "Cliente", "Documento", "Categoria", "Revisão", "Data de Emissão",
            "Data de Validade", "Status", "Dias para Vencer", "Observações");

    private static final List<String> CABECALHO_COBRANCAS = List.of(
            "Cliente", "Contrato", "Periodicidade", "Valor Esperado", "Valor Recebido",
            "Vencimento", "Pagamento", "Forma de Pagamento", "Status");

    private static final Map<CategoriaDocumento, String> ROTULOS_CATEGORIA = Map.of(
            CategoriaDocumento.CONTRATO, "Contrato",
            CategoriaDocumento.ALVARA, "Alvará",
            CategoriaDocumento.CERTIFICADO, "Certificado",
            CategoriaDocumento.LICENCA, "Licença",
            CategoriaDocumento.NF, "Nota Fiscal",
            CategoriaDocumento.OUTRO, "Outro");

    private static final Map<StatusDocumento, String> ROTULOS_STATUS_DOCUMENTO = Map.of(
            StatusDocumento.VALIDO, "Válido",
            StatusDocumento.A_VENCER, "A vencer",
            StatusDocumento.VENCIDO, "Vencido",
            StatusDocumento.SEM_VALIDADE, "Sem validade");

    private static final Map<StatusCobranca, String> ROTULOS_STATUS_COBRANCA = Map.of(
            StatusCobranca.PENDENTE, "Pendente",
            StatusCobranca.PAGO, "Pago",
            StatusCobranca.VENCIDO, "Vencido",
            StatusCobranca.CANCELADO, "Cancelado");

    private static final Map<Periodicidade, String> ROTULOS_PERIODICIDADE = Map.of(
            Periodicidade.MENSAL, "Mensal",
            Periodicidade.TRIMESTRAL, "Trimestral",
            Periodicidade.SEMESTRAL, "Semestral",
            Periodicidade.ANUAL, "Anual",
            Periodicidade.AVULSO, "Avulso");

    private final DocumentoRepository documentoRepository;
    private final CobrancaRepository cobrancaRepository;

    /** Quantidade de registros lidos por página; evita carregar toda a base de uma vez. */
    @Value("${export.tamanho-pagina:500}")
    private int tamanhoPagina = 500;

    /** Teto de linhas exportadas; protege a memória do servidor. */
    @Value("${export.max-linhas:50000}")
    private int maxLinhas = 50000;

    // -------------------------------------------------------------------------
    // Exportações
    // -------------------------------------------------------------------------

    /**
     * Relatório geral de documentos, ordenado pela data de validade ascendente
     * (os mais urgentes aparecem primeiro).
     */
    @Transactional(readOnly = true)
    public byte[] exportarDocumentos(String search, CategoriaDocumento categoria,
                                     String status, Long clienteId) {
        StringBuilder csv = new StringBuilder(BOM_UTF8);
        escreverLinha(csv, CABECALHO_DOCUMENTOS);

        Specification<Documento> spec = specDocumentos(search, categoria, status, clienteId);
        // Sem nullsLast(): o Hibernate lanca UnsupportedOperationException ("Applying Null
        // Precedence using Criteria Queries is not yet supported") ao combinar precedencia de
        // nulos com Specification. No PostgreSQL a ordenacao ASC ja coloca NULL por ultimo,
        // que e o comportamento desejado — documentos sem validade vao para o fim do relatorio.
        Sort ordenacao = Sort.by(Sort.Order.asc("dataValidade"));
        LocalDate hoje = LocalDate.now();

        Function<Pageable, Page<Documento>> consulta = pageable -> documentoRepository.findAll(spec, pageable);
        Consumer<Documento> escritor = documento -> escreverLinha(csv, linhaDocumento(documento, hoje));

        int total = paginar(consulta, ordenacao, escritor);

        log.info("Exportação de documentos gerada com {} linha(s)", total);
        return finalizar(csv);
    }

    /**
     * Relatório financeiro de cobranças, ordenado pela data de vencimento ascendente.
     */
    @Transactional(readOnly = true)
    public byte[] exportarCobrancas(Long contratoId, StatusCobranca status,
                                    Integer month, Integer year) {
        StringBuilder csv = new StringBuilder(BOM_UTF8);
        escreverLinha(csv, CABECALHO_COBRANCAS);

        Specification<Cobranca> spec = specCobrancas(contratoId, status, month, year);
        Sort ordenacao = Sort.by(Sort.Order.asc("dataVencimento"));

        Function<Pageable, Page<Cobranca>> consulta = pageable -> cobrancaRepository.findAll(spec, pageable);
        Consumer<Cobranca> escritor = cobranca -> escreverLinha(csv, linhaCobranca(cobranca));

        int total = paginar(consulta, ordenacao, escritor);

        log.info("Exportação de cobranças gerada com {} linha(s)", total);
        return finalizar(csv);
    }

    // -------------------------------------------------------------------------
    // Leitura paginada
    // -------------------------------------------------------------------------

    /**
     * Percorre o resultado em páginas, repassando cada registro ao escritor.
     * Não mantém a base inteira em memória e respeita o teto de linhas.
     *
     * @return quantidade de linhas escritas
     */
    private <T> int paginar(Function<Pageable, Page<T>> consulta, Sort ordenacao, Consumer<T> escritor) {
        int total = 0;
        int pagina = 0;

        while (true) {
            Page<T> resultado = consulta.apply(PageRequest.of(pagina, tamanhoPagina, ordenacao));

            for (T item : resultado.getContent()) {
                if (total >= maxLinhas) {
                    log.warn("Exportação truncada: limite de {} linhas atingido", maxLinhas);
                    return total;
                }
                escritor.accept(item);
                total++;
            }

            if (!resultado.hasNext()) {
                return total;
            }
            pagina++;
        }
    }

    // -------------------------------------------------------------------------
    // Specifications (replicam os filtros das listagens)
    // -------------------------------------------------------------------------

    private Specification<Documento> specDocumentos(String search, CategoriaDocumento categoria,
                                                     String status, Long clienteId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(search)) {
                String pattern = "%" + search.toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("nome")), pattern));
            }

            if (categoria != null) {
                predicates.add(cb.equal(root.get("categoria"), categoria));
            }

            if (clienteId != null) {
                predicates.add(cb.equal(root.get("cliente").get("id"), clienteId));
            }

            if (StringUtils.hasText(status)) {
                LocalDate hoje = LocalDate.now();
                switch (status.toUpperCase()) {
                    case "A_VENCER" -> predicates.add(
                            cb.between(root.get("dataValidade"), hoje,
                                    hoje.plusDays(DocumentoConstants.DIAS_ALERTA_VENCIMENTO)));
                    case "VENCIDO" -> predicates.add(
                            cb.lessThan(root.get("dataValidade"), hoje));
                    case "VALIDO" -> predicates.add(
                            cb.greaterThan(root.get("dataValidade"),
                                    hoje.plusDays(DocumentoConstants.DIAS_ALERTA_VENCIMENTO)));
                    case "SEM_VALIDADE" -> predicates.add(
                            cb.isNull(root.get("dataValidade")));
                    default -> { /* status desconhecido, sem filtro adicional */ }
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Specification<Cobranca> specCobrancas(Long contratoId, StatusCobranca status,
                                                   Integer month, Integer year) {
        return (root, query, cb) -> {
            // Traz contrato e cliente na mesma consulta (evita N+1 ao montar cada linha).
            // A consulta de contagem não aceita join fetch, daí a checagem do tipo do resultado.
            if (query != null && !Long.class.equals(query.getResultType())) {
                Fetch<?, ?> contratoFetch = root.fetch("contrato", JoinType.LEFT);
                contratoFetch.fetch("cliente", JoinType.LEFT);
            }

            List<Predicate> predicates = new ArrayList<>();

            if (contratoId != null) {
                predicates.add(cb.equal(root.get("contrato").get("id"), contratoId));
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (month != null && year != null) {
                LocalDate inicio = LocalDate.of(year, month, 1);
                LocalDate fim = inicio.withDayOfMonth(inicio.lengthOfMonth());
                predicates.add(cb.between(root.get("dataVencimento"), inicio, fim));
            } else if (year != null) {
                LocalDate inicio = LocalDate.of(year, 1, 1);
                LocalDate fim = LocalDate.of(year, 12, 31);
                predicates.add(cb.between(root.get("dataVencimento"), inicio, fim));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    // -------------------------------------------------------------------------
    // Linhas
    // -------------------------------------------------------------------------

    private List<String> linhaDocumento(Documento documento, LocalDate hoje) {
        return List.of(
                nomeCliente(documento.getCliente()),
                nvl(documento.getNome()),
                traduzirCategoria(documento.getCategoria()),
                nvl(documento.getRevisao()),
                formatarData(documento.getDataEmissao()),
                formatarData(documento.getDataValidade()),
                traduzirStatusDocumento(documento.getStatusCalculado()),
                diasParaVencer(documento.getDataValidade(), hoje),
                nvl(documento.getObservacoes()));
    }

    private List<String> linhaCobranca(Cobranca cobranca) {
        Contrato contrato = cobranca.getContrato();
        return List.of(
                nomeCliente(contrato != null ? contrato.getCliente() : null),
                contrato != null ? nvl(contrato.getDescricao()) : "",
                contrato != null ? traduzirPeriodicidade(contrato.getPeriodicidade()) : "",
                formatarValor(cobranca.getValorEsperado()),
                formatarValor(cobranca.getValorRecebido()),
                formatarData(cobranca.getDataVencimento()),
                formatarData(cobranca.getDataPagamento()),
                nvl(cobranca.getFormaPagamento()),
                traduzirStatusCobranca(statusEfetivo(cobranca)));
    }

    /** Cobrança cancelada mantém o status gravado; as demais usam o status calculado. */
    static StatusCobranca statusEfetivo(Cobranca cobranca) {
        if (cobranca.getStatus() == StatusCobranca.CANCELADO) {
            return StatusCobranca.CANCELADO;
        }
        return cobranca.getStatusCalculado();
    }

    static String nomeCliente(Cliente cliente) {
        if (cliente == null) {
            return "";
        }
        if (StringUtils.hasText(cliente.getRazaoSocial())) {
            return cliente.getRazaoSocial();
        }
        return nvl(cliente.getNomeFantasia());
    }

    /** Dias restantes até o vencimento; negativo quando o documento já venceu. */
    static String diasParaVencer(LocalDate dataValidade, LocalDate hoje) {
        if (dataValidade == null) {
            return "";
        }
        return String.valueOf(ChronoUnit.DAYS.between(hoje, dataValidade));
    }

    // -------------------------------------------------------------------------
    // Escrita e formatação do CSV
    // -------------------------------------------------------------------------

    private void escreverLinha(StringBuilder csv, List<String> campos) {
        for (int i = 0; i < campos.size(); i++) {
            if (i > 0) {
                csv.append(SEPARADOR);
            }
            csv.append(escapar(campos.get(i)));
        }
        csv.append(QUEBRA_LINHA);
    }

    private byte[] finalizar(StringBuilder csv) {
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Envolve o campo em aspas duplas quando ele contém separador, aspas ou quebra
     * de linha, duplicando as aspas internas (RFC 4180).
     */
    static String escapar(String valor) {
        if (valor == null || valor.isEmpty()) {
            return "";
        }
        if (valor.indexOf(SEPARADOR) >= 0 || valor.indexOf('"') >= 0
                || valor.indexOf('\n') >= 0 || valor.indexOf('\r') >= 0) {
            return '"' + valor.replace("\"", "\"\"") + '"';
        }
        return valor;
    }

    static String formatarData(LocalDate data) {
        return data == null ? "" : data.format(FORMATO_DATA);
    }

    /** Valor monetário no padrão pt-BR: milhar com ponto e decimal com vírgula. */
    static String formatarValor(BigDecimal valor) {
        return valor == null ? "" : String.format(PT_BR, "%,.2f", valor);
    }

    static String nvl(String valor) {
        return valor == null ? "" : valor;
    }

    static String traduzirCategoria(CategoriaDocumento categoria) {
        return categoria == null ? "" : ROTULOS_CATEGORIA.getOrDefault(categoria, categoria.name());
    }

    static String traduzirStatusDocumento(StatusDocumento status) {
        return status == null ? "" : ROTULOS_STATUS_DOCUMENTO.getOrDefault(status, status.name());
    }

    static String traduzirStatusCobranca(StatusCobranca status) {
        return status == null ? "" : ROTULOS_STATUS_COBRANCA.getOrDefault(status, status.name());
    }

    static String traduzirPeriodicidade(Periodicidade periodicidade) {
        return periodicidade == null ? "" : ROTULOS_PERIODICIDADE.getOrDefault(periodicidade, periodicidade.name());
    }
}
