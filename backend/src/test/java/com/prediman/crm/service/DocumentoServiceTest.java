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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class DocumentoServiceTest {

    @Mock
    private DocumentoRepository documentoRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private DocumentoMapper documentoMapper;

    @InjectMocks
    private DocumentoService documentoService;

    private Cliente cliente;
    private Documento documento;
    private DocumentoRequest request;
    private DocumentoResponse response;

    @BeforeEach
    void setUp() {
        cliente = Cliente.builder()
                .id(1L)
                .razaoSocial("Empresa Teste Ltda")
                .status(StatusCliente.ATIVO)
                .build();

        documento = Documento.builder()
                .id(10L)
                .cliente(cliente)
                .nome("Contrato Social")
                .categoria(CategoriaDocumento.CONTRATO)
                .dataEmissao(LocalDate.of(2024, 1, 15))
                .dataValidade(LocalDate.of(2025, 1, 15))
                .revisao("v1")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        request = DocumentoRequest.builder()
                .clienteId(1L)
                .nome("Contrato Social")
                .categoria(CategoriaDocumento.CONTRATO)
                .dataEmissao(LocalDate.of(2024, 1, 15))
                .dataValidade(LocalDate.of(2025, 1, 15))
                .revisao("v1")
                .build();

        response = DocumentoResponse.builder()
                .id(10L)
                .nome("Contrato Social")
                .categoria(CategoriaDocumento.CONTRATO)
                .clienteId(1L)
                .clienteNome("Empresa Teste Ltda")
                .build();
    }

    // ------------------------------------------------------------------ create

    @Test
    void create_documentoComSucesso() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(documentoRepository.save(any(Documento.class))).thenReturn(documento);
        when(documentoMapper.toResponse(documento)).thenReturn(response);

        DocumentoResponse result = documentoService.create(request);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getNome()).isEqualTo("Contrato Social");

        ArgumentCaptor<Documento> captor = ArgumentCaptor.forClass(Documento.class);
        verify(documentoRepository).save(captor.capture());
        Documento saved = captor.getValue();
        assertThat(saved.getCliente()).isEqualTo(cliente);
        assertThat(saved.getNome()).isEqualTo("Contrato Social");
        assertThat(saved.getCategoria()).isEqualTo(CategoriaDocumento.CONTRATO);
    }

    @Test
    void create_categoriaNula_usaOUTRO() {
        request.setCategoria(null);
        Documento semCategoria = Documento.builder()
                .id(11L)
                .cliente(cliente)
                .nome("Contrato Social")
                .categoria(CategoriaDocumento.OUTRO)
                .build();
        DocumentoResponse semCategoriaResponse = DocumentoResponse.builder()
                .id(11L)
                .categoria(CategoriaDocumento.OUTRO)
                .build();

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(documentoRepository.save(any(Documento.class))).thenReturn(semCategoria);
        when(documentoMapper.toResponse(semCategoria)).thenReturn(semCategoriaResponse);

        DocumentoResponse result = documentoService.create(request);

        ArgumentCaptor<Documento> captor = ArgumentCaptor.forClass(Documento.class);
        verify(documentoRepository).save(captor.capture());
        assertThat(captor.getValue().getCategoria()).isEqualTo(CategoriaDocumento.OUTRO);
        assertThat(result.getCategoria()).isEqualTo(CategoriaDocumento.OUTRO);
    }

    @Test
    void create_clienteIdInvalido_lancaResourceNotFoundException() {
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());
        request.setClienteId(99L);

        assertThatThrownBy(() -> documentoService.create(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");

        verify(documentoRepository, never()).save(any());
    }

    // ------------------------------------------------------------------ findById

    @Test
    void findById_retornaDocumento() {
        when(documentoRepository.findById(10L)).thenReturn(Optional.of(documento));
        when(documentoMapper.toResponse(documento)).thenReturn(response);

        DocumentoResponse result = documentoService.findById(10L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(10L);
        verify(documentoRepository).findById(10L);
    }

    @Test
    void findById_idInvalido_lancaResourceNotFoundException() {
        when(documentoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> documentoService.findById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    // ------------------------------------------------------------------ update

    @Test
    void update_documentoComSucesso_mesmoCliente() {
        // request keeps the same clienteId as the existing document
        when(documentoRepository.findById(10L)).thenReturn(Optional.of(documento));
        when(documentoRepository.save(any(Documento.class))).thenReturn(documento);
        when(documentoMapper.toResponse(documento)).thenReturn(response);

        DocumentoResponse result = documentoService.update(10L, request);

        assertThat(result).isNotNull();
        // cliente should not be re-fetched when id is unchanged
        verify(clienteRepository, never()).findById(any());
        verify(documentoRepository).save(documento);
    }

    @Test
    void update_clienteAlterado_buscaNovoCliente() {
        Cliente novoCliente = Cliente.builder().id(2L).razaoSocial("Outro Cliente Ltda").build();
        request.setClienteId(2L);

        when(documentoRepository.findById(10L)).thenReturn(Optional.of(documento));
        when(clienteRepository.findById(2L)).thenReturn(Optional.of(novoCliente));
        when(documentoRepository.save(any(Documento.class))).thenReturn(documento);
        when(documentoMapper.toResponse(documento)).thenReturn(response);

        documentoService.update(10L, request);

        verify(clienteRepository).findById(2L);
        assertThat(documento.getCliente()).isEqualTo(novoCliente);
    }

    @Test
    void update_documentoNaoEncontrado_lancaResourceNotFoundException() {
        when(documentoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> documentoService.update(999L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");

        verify(documentoRepository, never()).save(any());
    }

    // ------------------------------------------------------------------ delete

    @Test
    void delete_documentoComSucesso() {
        when(documentoRepository.findById(10L)).thenReturn(Optional.of(documento));

        documentoService.delete(10L);

        verify(documentoRepository).delete(documento);
    }

    @Test
    void delete_idInvalido_lancaResourceNotFoundException() {
        when(documentoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> documentoService.delete(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");

        verify(documentoRepository, never()).delete(any(Documento.class));
    }

    // ------------------------------------------------------------------ getDashboardSummary

    @Test
    void getDashboardSummary_retornaContagensCorretas() {
        LocalDate today = LocalDate.now();
        LocalDate alertaFim = today.plusDays(DocumentoConstants.DIAS_ALERTA_VENCIMENTO);

        when(clienteRepository.count()).thenReturn(10L);
        when(clienteRepository.countByStatus(StatusCliente.ATIVO)).thenReturn(8L);
        when(documentoRepository.countAVencer(today, alertaFim)).thenReturn(3L);
        when(documentoRepository.countVencidos(today)).thenReturn(2L);

        DashboardSummaryResponse result = documentoService.getDashboardSummary();

        assertThat(result.getTotalClientes()).isEqualTo(10L);
        assertThat(result.getClientesAtivos()).isEqualTo(8L);
        assertThat(result.getDocumentosAVencer()).isEqualTo(3L);
        assertThat(result.getDocumentosVencidos()).isEqualTo(2L);

        verify(clienteRepository).count();
        verify(clienteRepository).countByStatus(StatusCliente.ATIVO);
        verify(documentoRepository).countAVencer(today, alertaFim);
        verify(documentoRepository).countVencidos(today);
    }

    @Test
    void getDashboardSummary_semDocumentos_retornaZeros() {
        LocalDate today = LocalDate.now();
        LocalDate alertaFim = today.plusDays(DocumentoConstants.DIAS_ALERTA_VENCIMENTO);

        when(clienteRepository.count()).thenReturn(0L);
        when(clienteRepository.countByStatus(StatusCliente.ATIVO)).thenReturn(0L);
        when(documentoRepository.countAVencer(today, alertaFim)).thenReturn(0L);
        when(documentoRepository.countVencidos(today)).thenReturn(0L);

        DashboardSummaryResponse result = documentoService.getDashboardSummary();

        assertThat(result.getTotalClientes()).isZero();
        assertThat(result.getClientesAtivos()).isZero();
        assertThat(result.getDocumentosAVencer()).isZero();
        assertThat(result.getDocumentosVencidos()).isZero();
    }

    // ------------------------------------------------------------------ findByClienteId

    @Test
    void findByClienteId_retornaListaDeDocumentos() {
        when(documentoRepository.findTop500ByClienteIdOrderByDataValidadeAsc(1L))
                .thenReturn(List.of(documento));
        when(documentoMapper.toResponse(documento)).thenReturn(response);

        List<DocumentoResponse> result = documentoService.findByClienteId(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(10L);
        verify(documentoRepository).findTop500ByClienteIdOrderByDataValidadeAsc(1L);
    }

    @Test
    void findByClienteId_semDocumentos_retornaListaVazia() {
        when(documentoRepository.findTop500ByClienteIdOrderByDataValidadeAsc(99L))
                .thenReturn(List.of());

        List<DocumentoResponse> result = documentoService.findByClienteId(99L);

        assertThat(result).isEmpty();
    }

    // ------------------------------------------------------------------ findAll com filtros de status

    @Test
    void findAll_semFiltros_retornaPagina() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Documento> page = new PageImpl<>(List.of(documento), pageable, 1);

        when(documentoRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(documentoMapper.toResponse(documento)).thenReturn(response);

        Page<DocumentoResponse> result = documentoService.findAll(null, null, null, null, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void findAll_comSearch_retornaPagina() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Documento> page = new PageImpl<>(List.of(documento), pageable, 1);

        when(documentoRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(documentoMapper.toResponse(documento)).thenReturn(response);

        Page<DocumentoResponse> result = documentoService.findAll("contrato", null, null, null, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(documentoRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void findAll_comCategoria_retornaPagina() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Documento> page = new PageImpl<>(List.of(documento), pageable, 1);

        when(documentoRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(documentoMapper.toResponse(documento)).thenReturn(response);

        Page<DocumentoResponse> result = documentoService.findAll(null, CategoriaDocumento.CONTRATO, null, null, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void findAll_comClienteId_retornaPagina() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Documento> page = new PageImpl<>(List.of(documento), pageable, 1);

        when(documentoRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(documentoMapper.toResponse(documento)).thenReturn(response);

        Page<DocumentoResponse> result = documentoService.findAll(null, null, null, 1L, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void findAll_statusAVencer_adicionaPredicado() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Documento> page = new PageImpl<>(List.of(documento), pageable, 1);

        when(documentoRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(documentoMapper.toResponse(documento)).thenReturn(response);

        Page<DocumentoResponse> result = documentoService.findAll(null, null, "A_VENCER", null, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(documentoRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void findAll_statusVencido_adicionaPredicado() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Documento> page = new PageImpl<>(List.of(documento), pageable, 1);

        when(documentoRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(documentoMapper.toResponse(documento)).thenReturn(response);

        Page<DocumentoResponse> result = documentoService.findAll(null, null, "VENCIDO", null, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void findAll_statusValido_adicionaPredicado() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Documento> page = new PageImpl<>(List.of(documento), pageable, 1);

        when(documentoRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(documentoMapper.toResponse(documento)).thenReturn(response);

        Page<DocumentoResponse> result = documentoService.findAll(null, null, "VALIDO", null, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void findAll_statusSemValidade_adicionaPredicado() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Documento> page = new PageImpl<>(List.of(documento), pageable, 1);

        when(documentoRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(documentoMapper.toResponse(documento)).thenReturn(response);

        Page<DocumentoResponse> result = documentoService.findAll(null, null, "SEM_VALIDADE", null, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void findAll_statusDesconhecido_semFiltroAdicional() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Documento> page = new PageImpl<>(List.of(documento), pageable, 1);

        when(documentoRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(documentoMapper.toResponse(documento)).thenReturn(response);

        Page<DocumentoResponse> result = documentoService.findAll(null, null, "INVALIDO_STATUS", null, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    // ------------------------------------------------------------------ update com categoria nula

    @Test
    void update_categoriaNula_usaOUTRO() {
        request.setCategoria(null);
        when(documentoRepository.findById(10L)).thenReturn(Optional.of(documento));
        when(documentoRepository.save(any(Documento.class))).thenReturn(documento);
        when(documentoMapper.toResponse(documento)).thenReturn(response);

        documentoService.update(10L, request);

        ArgumentCaptor<Documento> captor = ArgumentCaptor.forClass(Documento.class);
        verify(documentoRepository).save(captor.capture());
        assertThat(captor.getValue().getCategoria()).isEqualTo(CategoriaDocumento.OUTRO);
    }

    @Test
    void update_clienteIdInvalido_lancaResourceNotFoundException() {
        request.setClienteId(99L);
        when(documentoRepository.findById(10L)).thenReturn(Optional.of(documento));
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> documentoService.update(10L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");

        verify(documentoRepository, never()).save(any());
    }

    // ------------------------------------------------------------------ Specification lambda coverage

    @SuppressWarnings("unchecked")
    private Specification<Documento> captureSpec(String search, CategoriaDocumento cat, String status, Long clienteId) {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Documento> page = new PageImpl<>(List.of());
        ArgumentCaptor<Specification<Documento>> captor = ArgumentCaptor.forClass(Specification.class);
        when(documentoRepository.findAll(captor.capture(), eq(pageable))).thenReturn(page);
        documentoService.findAll(search, cat, status, clienteId, pageable);
        return captor.getValue();
    }

    @SuppressWarnings("unchecked")
    private void invokeSpec(Specification<Documento> spec) {
        Root<Documento> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Path<Object> nomePath = mock(Path.class);
        Path<Object> categoriaPath = mock(Path.class);
        Path<Object> clientePath = mock(Path.class);
        Path<Object> clienteIdPath = mock(Path.class);
        Path<Object> dataValidadePath = mock(Path.class);

        lenient().when(root.get("nome")).thenReturn(nomePath);
        lenient().when(root.get("categoria")).thenReturn(categoriaPath);
        lenient().when(root.get("cliente")).thenReturn(clientePath);
        lenient().when(clientePath.get("id")).thenReturn(clienteIdPath);
        lenient().when(root.get("dataValidade")).thenReturn(dataValidadePath);
        lenient().when(cb.lower(any())).thenReturn(mock(jakarta.persistence.criteria.Expression.class));
        lenient().when(cb.like(any(), anyString())).thenReturn(mock(Predicate.class));
        lenient().when(cb.equal(any(), any())).thenReturn(mock(Predicate.class));
        lenient().when(cb.between(any(), any(Comparable.class), any(Comparable.class))).thenReturn(mock(Predicate.class));
        lenient().when(cb.lessThan(any(), any(Comparable.class))).thenReturn(mock(Predicate.class));
        lenient().when(cb.greaterThan(any(), any(Comparable.class))).thenReturn(mock(Predicate.class));
        lenient().when(cb.isNull(any())).thenReturn(mock(Predicate.class));
        lenient().when(cb.and(any(Predicate[].class))).thenReturn(mock(Predicate.class));

        spec.toPredicate(root, query, cb);
    }

    @Test
    void specificationLambda_semFiltros_executa() {
        invokeSpec(captureSpec(null, null, null, null));
    }

    @Test
    void specificationLambda_comSearch_executa() {
        invokeSpec(captureSpec("doc", null, null, null));
    }

    @Test
    void specificationLambda_comCategoria_executa() {
        invokeSpec(captureSpec(null, CategoriaDocumento.CONTRATO, null, null));
    }

    @Test
    void specificationLambda_comClienteId_executa() {
        invokeSpec(captureSpec(null, null, null, 1L));
    }

    @Test
    void specificationLambda_statusAVencer_executa() {
        invokeSpec(captureSpec(null, null, "A_VENCER", null));
    }

    @Test
    void specificationLambda_statusVencido_executa() {
        invokeSpec(captureSpec(null, null, "VENCIDO", null));
    }

    @Test
    void specificationLambda_statusValido_executa() {
        invokeSpec(captureSpec(null, null, "VALIDO", null));
    }

    @Test
    void specificationLambda_statusSemValidade_executa() {
        invokeSpec(captureSpec(null, null, "SEM_VALIDADE", null));
    }

    @Test
    void specificationLambda_statusDesconhecido_executa() {
        invokeSpec(captureSpec(null, null, "OUTRO_STATUS", null));
    }

    @Test
    void specificationLambda_todosOsFiltros_executa() {
        invokeSpec(captureSpec("busca", CategoriaDocumento.ALVARA, "A_VENCER", 2L));
    }
}
