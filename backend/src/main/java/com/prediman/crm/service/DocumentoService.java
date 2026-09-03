package com.prediman.crm.service;

import com.prediman.crm.dto.DashboardSummaryResponse;
import com.prediman.crm.dto.DocumentoRequest;
import com.prediman.crm.dto.DocumentoResponse;
import com.prediman.crm.exception.ResourceNotFoundException;
import com.prediman.crm.model.Cliente;
import com.prediman.crm.model.Documento;
import com.prediman.crm.model.DocumentoConstants;
import com.prediman.crm.model.enums.CategoriaDocumento;
import com.prediman.crm.model.enums.StatusCliente;
import com.prediman.crm.repository.ClienteRepository;
import com.prediman.crm.repository.DocumentoRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentoService {

    private final DocumentoRepository documentoRepository;
    private final ClienteRepository clienteRepository;
    private final DocumentoMapper documentoMapper;
    private final ClienteService clienteService;
    private final GoogleDriveService googleDriveService;

    private static final DateTimeFormatter FORMATO_DATA_ARQUIVO = DateTimeFormatter.ofPattern("yyyy.MM.dd");
    private static final String PREFIXO_REVISAO = "Rev";
    private static final String NOME_ARQUIVO_PADRAO = "documento";
    private static final String CARACTERES_INVALIDOS = "[\\\\/:*?\"<>|\\p{Cntrl}]+";

    /**
     * Envia o arquivo ao Google Drive (dentro da pasta do cliente) e cadastra o documento.
     * Quando o Drive está desabilitado ou falha, o documento é criado apenas com os metadados.
     */
    @Transactional
    public DocumentoResponse upload(DocumentoRequest request, MultipartFile file) throws IOException {
        request.setMimeType(file.getContentType());
        request.setTamanhoBytes(file.getSize());

        if (googleDriveService.isEnabled()) {
            String pastaClienteId = clienteService.obterOuCriarPastaDrive(request.getClienteId());
            String nomeArquivo = montarNomeArquivo(request, file.getOriginalFilename());

            GoogleDriveService.GoogleDriveResult result = googleDriveService.upload(
                    nomeArquivo,
                    file.getContentType(),
                    file.getBytes(),
                    pastaClienteId);

            if (result != null) {
                request.setGoogleDriveFileId(result.getFileId());
                request.setGoogleDriveUrl(result.getWebViewLink());
            } else {
                log.warn("Falha no envio ao Google Drive; documento '{}' sera criado apenas com metadados",
                        request.getNome());
            }
        } else {
            log.warn("Google Drive desabilitado; documento criado apenas com metadados");
        }

        return create(request);
    }

    @Transactional
    public DocumentoResponse create(DocumentoRequest request) {
        Cliente cliente = clienteRepository.findById(request.getClienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", request.getClienteId()));

        Documento documento = Documento.builder()
                .cliente(cliente)
                .nome(request.getNome())
                .categoria(request.getCategoria() != null ? request.getCategoria() : CategoriaDocumento.OUTRO)
                .dataEmissao(request.getDataEmissao())
                .dataValidade(request.getDataValidade())
                .revisao(request.getRevisao())
                .observacoes(request.getObservacoes())
                .googleDriveFileId(request.getGoogleDriveFileId())
                .googleDriveUrl(request.getGoogleDriveUrl())
                .tamanhoBytes(request.getTamanhoBytes())
                .mimeType(request.getMimeType())
                .build();

        Documento saved = documentoRepository.save(documento);
        log.info("Documento criado com id: {}", saved.getId());
        return documentoMapper.toResponse(saved);
    }

    @Transactional
    public DocumentoResponse update(Long id, DocumentoRequest request) {
        Documento documento = findDocumentoById(id);

        if (!documento.getCliente().getId().equals(request.getClienteId())) {
            Cliente novoCliente = clienteRepository.findById(request.getClienteId())
                    .orElseThrow(() -> new ResourceNotFoundException("Cliente", request.getClienteId()));
            documento.setCliente(novoCliente);
        }

        documento.setNome(request.getNome());
        documento.setCategoria(request.getCategoria() != null ? request.getCategoria() : CategoriaDocumento.OUTRO);
        documento.setDataEmissao(request.getDataEmissao());
        documento.setDataValidade(request.getDataValidade());
        documento.setRevisao(request.getRevisao());
        documento.setObservacoes(request.getObservacoes());
        documento.setGoogleDriveFileId(request.getGoogleDriveFileId());
        documento.setGoogleDriveUrl(request.getGoogleDriveUrl());
        documento.setTamanhoBytes(request.getTamanhoBytes());
        documento.setMimeType(request.getMimeType());

        Documento saved = documentoRepository.save(documento);
        log.info("Documento atualizado com id: {}", saved.getId());
        return documentoMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public DocumentoResponse findById(Long id) {
        return documentoMapper.toResponse(findDocumentoById(id));
    }

    @Transactional(readOnly = true)
    public List<DocumentoResponse> findByClienteId(Long clienteId) {
        return documentoRepository.findTop500ByClienteIdOrderByDataValidadeAsc(clienteId)
                .stream()
                .map(documentoMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<DocumentoResponse> findAll(String search, CategoriaDocumento categoria,
                                           String status, Long clienteId, Pageable pageable) {
        Specification<Documento> spec = buildSpecification(search, categoria, status, clienteId);
        return documentoRepository.findAll(spec, pageable).map(documentoMapper::toResponse);
    }

    @Transactional
    public void delete(Long id) {
        Documento documento = findDocumentoById(id);
        documentoRepository.delete(documento);
        log.info("Documento excluido com id: {}", id);
    }

    @Transactional(readOnly = true)
    public DashboardSummaryResponse getDashboardSummary() {
        long totalClientes = clienteRepository.count();
        long clientesAtivos = clienteRepository.countByStatus(StatusCliente.ATIVO);
        LocalDate today = LocalDate.now();
        long documentosAVencer = documentoRepository.countAVencer(today, today.plusDays(DocumentoConstants.DIAS_ALERTA_VENCIMENTO));
        long documentosVencidos = documentoRepository.countVencidos(today);

        return DashboardSummaryResponse.builder()
                .totalClientes(totalClientes)
                .clientesAtivos(clientesAtivos)
                .documentosAVencer(documentosAVencer)
                .documentosVencidos(documentosVencidos)
                .build();
    }

    /**
     * Monta o nome do arquivo no padrao AAAA.MM.DD_NomeArquivo_RevXX, preservando a extensao original.
     * A data vem de dataEmissao (ou a data atual, se ausente) e o sufixo de revisao so e aplicado
     * quando o campo estiver preenchido — o valor e texto livre e nao sofre normalizacao.
     */
    public static String montarNomeArquivo(DocumentoRequest request, String nomeOriginal) {
        LocalDate data = request.getDataEmissao() != null ? request.getDataEmissao() : LocalDate.now();

        String nomeDocumento = sanitizarNomeArquivo(request.getNome());
        if (nomeDocumento.isEmpty()) {
            nomeDocumento = NOME_ARQUIVO_PADRAO;
        }

        StringBuilder nome = new StringBuilder()
                .append(FORMATO_DATA_ARQUIVO.format(data))
                .append('_')
                .append(nomeDocumento);

        String revisao = sanitizarNomeArquivo(request.getRevisao());
        if (StringUtils.hasText(revisao)) {
            nome.append('_');
            if (!revisao.toUpperCase().startsWith(PREFIXO_REVISAO.toUpperCase())) {
                nome.append(PREFIXO_REVISAO);
            }
            nome.append(revisao);
        }

        return nome.append(extrairExtensao(nomeOriginal)).toString();
    }

    /**
     * Remove separadores de caminho e caracteres invalidos em nomes de arquivo.
     * Retorna string vazia quando nada resta apos a limpeza.
     */
    private static String sanitizarNomeArquivo(String valor) {
        if (!StringUtils.hasText(valor)) {
            return "";
        }
        return valor.trim()
                .replaceAll(CARACTERES_INVALIDOS, "_")
                .replaceAll("_{2,}", "_")
                .replaceAll("^[_.\\s]+|[_.\\s]+$", "")
                .trim();
    }

    /**
     * Extrai a extensao (com o ponto) do arquivo enviado; retorna vazio quando nao houver.
     */
    private static String extrairExtensao(String nomeOriginal) {
        if (!StringUtils.hasText(nomeOriginal)) {
            return "";
        }
        int posicaoSeparador = Math.max(nomeOriginal.lastIndexOf('/'), nomeOriginal.lastIndexOf('\\'));
        String apenasNome = nomeOriginal.substring(posicaoSeparador + 1);
        int posicaoPonto = apenasNome.lastIndexOf('.');
        if (posicaoPonto <= 0 || posicaoPonto == apenasNome.length() - 1) {
            return "";
        }
        return apenasNome.substring(posicaoPonto);
    }

    private Documento findDocumentoById(Long id) {
        return documentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Documento", id));
    }

    private Specification<Documento> buildSpecification(String search, CategoriaDocumento categoria,
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
                LocalDate today = LocalDate.now();
                switch (status.toUpperCase()) {
                    case "A_VENCER" -> predicates.add(
                            cb.between(root.get("dataValidade"), today, today.plusDays(DocumentoConstants.DIAS_ALERTA_VENCIMENTO))
                    );
                    case "VENCIDO" -> predicates.add(
                            cb.lessThan(root.get("dataValidade"), today)
                    );
                    case "VALIDO" -> predicates.add(
                            cb.greaterThan(root.get("dataValidade"), today.plusDays(DocumentoConstants.DIAS_ALERTA_VENCIMENTO))
                    );
                    case "SEM_VALIDADE" -> predicates.add(
                            cb.isNull(root.get("dataValidade"))
                    );
                    default -> { /* status desconhecido, sem filtro adicional */ }
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

}
