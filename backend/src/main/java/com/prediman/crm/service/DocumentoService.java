package com.prediman.crm.service;

import com.prediman.crm.dto.DashboardSummaryResponse;
import com.prediman.crm.dto.DocumentoRequest;
import com.prediman.crm.dto.DocumentoResponse;
import com.prediman.crm.exception.ResourceNotFoundException;
import com.prediman.crm.model.Cliente;
import com.prediman.crm.model.Documento;
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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentoService {

    private final DocumentoRepository documentoRepository;
    private final ClienteRepository clienteRepository;

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
        return toResponse(saved);
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
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public DocumentoResponse findById(Long id) {
        return toResponse(findDocumentoById(id));
    }

    @Transactional(readOnly = true)
    public List<DocumentoResponse> findByClienteId(Long clienteId) {
        return documentoRepository.findByClienteIdOrderByDataValidadeAsc(clienteId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<DocumentoResponse> findAll(String search, CategoriaDocumento categoria,
                                           String status, Long clienteId, Pageable pageable) {
        Specification<Documento> spec = buildSpecification(search, categoria, status, clienteId);
        return documentoRepository.findAll(spec, pageable).map(this::toResponse);
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
        long documentosAVencer = documentoRepository.countAVencer(today, today.plusDays(30));
        long documentosVencidos = documentoRepository.countVencidos(today);

        return DashboardSummaryResponse.builder()
                .totalClientes(totalClientes)
                .clientesAtivos(clientesAtivos)
                .documentosAVencer(documentosAVencer)
                .documentosVencidos(documentosVencidos)
                .build();
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
                            cb.between(root.get("dataValidade"), today, today.plusDays(30))
                    );
                    case "VENCIDO" -> predicates.add(
                            cb.lessThan(root.get("dataValidade"), today)
                    );
                    case "VALIDO" -> predicates.add(
                            cb.greaterThan(root.get("dataValidade"), today.plusDays(30))
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

    private DocumentoResponse toResponse(Documento documento) {
        return DocumentoResponse.builder()
                .id(documento.getId())
                .nome(documento.getNome())
                .categoria(documento.getCategoria())
                .dataEmissao(documento.getDataEmissao())
                .dataValidade(documento.getDataValidade())
                .revisao(documento.getRevisao())
                .observacoes(documento.getObservacoes())
                .googleDriveFileId(documento.getGoogleDriveFileId())
                .googleDriveUrl(documento.getGoogleDriveUrl())
                .tamanhoBytes(documento.getTamanhoBytes())
                .mimeType(documento.getMimeType())
                .statusCalculado(documento.getStatusCalculado())
                .clienteId(documento.getCliente().getId())
                .clienteNome(documento.getCliente().getRazaoSocial())
                .createdAt(documento.getCreatedAt())
                .updatedAt(documento.getUpdatedAt())
                .build();
    }
}
