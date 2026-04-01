package com.prediman.crm.service;

import com.prediman.crm.dto.ContratoRequest;
import com.prediman.crm.dto.ContratoResponse;
import com.prediman.crm.exception.BusinessException;
import com.prediman.crm.exception.ResourceNotFoundException;
import com.prediman.crm.model.Cliente;
import com.prediman.crm.model.Cobranca;
import com.prediman.crm.model.Contrato;
import com.prediman.crm.model.enums.Periodicidade;
import com.prediman.crm.model.enums.StatusCobranca;
import com.prediman.crm.model.enums.StatusContrato;
import com.prediman.crm.repository.ClienteRepository;
import com.prediman.crm.repository.CobrancaRepository;
import com.prediman.crm.repository.ContratoRepository;
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

import java.math.BigDecimal;
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
class ContratoServiceTest {

    @Mock
    private ContratoRepository contratoRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private CobrancaRepository cobrancaRepository;

    @Mock
    private ContratoMapper contratoMapper;

    @InjectMocks
    private ContratoService contratoService;

    private Cliente cliente;
    private Contrato contrato;
    private ContratoRequest request;
    private ContratoResponse response;

    @BeforeEach
    void setUp() {
        cliente = Cliente.builder()
                .id(1L)
                .razaoSocial("Empresa Teste Ltda")
                .build();

        contrato = Contrato.builder()
                .id(20L)
                .cliente(cliente)
                .descricao("Licenca Software XYZ")
                .valor(new BigDecimal("1500.00"))
                .periodicidade(Periodicidade.MENSAL)
                .dataInicio(LocalDate.of(2024, 1, 10))
                .status(StatusContrato.ATIVO)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        request = ContratoRequest.builder()
                .clienteId(1L)
                .descricao("Licenca Software XYZ")
                .valor(new BigDecimal("1500.00"))
                .periodicidade(Periodicidade.MENSAL)
                .dataInicio(LocalDate.of(2024, 1, 10))
                .status(StatusContrato.ATIVO)
                .build();

        response = ContratoResponse.builder()
                .id(20L)
                .descricao("Licenca Software XYZ")
                .valor(new BigDecimal("1500.00"))
                .periodicidade(Periodicidade.MENSAL)
                .clienteId(1L)
                .clienteNome("Empresa Teste Ltda")
                .status(StatusContrato.ATIVO)
                .build();
    }

    // ------------------------------------------------------------------ create

    @Test
    void create_contratoComSucesso() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(contratoRepository.save(any(Contrato.class))).thenReturn(contrato);
        when(contratoMapper.toResponse(contrato)).thenReturn(response);

        ContratoResponse result = contratoService.create(request);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(20L);
        assertThat(result.getDescricao()).isEqualTo("Licenca Software XYZ");

        ArgumentCaptor<Contrato> captor = ArgumentCaptor.forClass(Contrato.class);
        verify(contratoRepository).save(captor.capture());
        Contrato saved = captor.getValue();
        assertThat(saved.getCliente()).isEqualTo(cliente);
        assertThat(saved.getValor()).isEqualByComparingTo("1500.00");
        assertThat(saved.getPeriodicidade()).isEqualTo(Periodicidade.MENSAL);
    }

    @Test
    void create_periodicidadeNula_usaMENSAL() {
        request.setPeriodicidade(null);
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(contratoRepository.save(any(Contrato.class))).thenReturn(contrato);
        when(contratoMapper.toResponse(contrato)).thenReturn(response);

        contratoService.create(request);

        ArgumentCaptor<Contrato> captor = ArgumentCaptor.forClass(Contrato.class);
        verify(contratoRepository).save(captor.capture());
        assertThat(captor.getValue().getPeriodicidade()).isEqualTo(Periodicidade.MENSAL);
    }

    @Test
    void create_clienteIdInvalido_lancaResourceNotFoundException() {
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());
        request.setClienteId(99L);

        assertThatThrownBy(() -> contratoService.create(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");

        verify(contratoRepository, never()).save(any());
    }

    // ------------------------------------------------------------------ findById

    @Test
    void findById_retornaContrato() {
        when(contratoRepository.findById(20L)).thenReturn(Optional.of(contrato));
        when(contratoMapper.toResponse(contrato)).thenReturn(response);

        ContratoResponse result = contratoService.findById(20L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(20L);
        verify(contratoRepository).findById(20L);
    }

    @Test
    void findById_idInvalido_lancaResourceNotFoundException() {
        when(contratoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> contratoService.findById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    // ------------------------------------------------------------------ update

    @Test
    void update_contratoComSucesso_mesmoCliente() {
        when(contratoRepository.findById(20L)).thenReturn(Optional.of(contrato));
        when(contratoRepository.save(any(Contrato.class))).thenReturn(contrato);
        when(contratoMapper.toResponse(contrato)).thenReturn(response);

        ContratoResponse result = contratoService.update(20L, request);

        assertThat(result).isNotNull();
        verify(clienteRepository, never()).findById(any());
        verify(contratoRepository).save(contrato);
    }

    @Test
    void update_clienteAlterado_buscaNovoCliente() {
        Cliente novoCliente = Cliente.builder().id(2L).razaoSocial("Outro Ltda").build();
        request.setClienteId(2L);

        when(contratoRepository.findById(20L)).thenReturn(Optional.of(contrato));
        when(clienteRepository.findById(2L)).thenReturn(Optional.of(novoCliente));
        when(contratoRepository.save(any(Contrato.class))).thenReturn(contrato);
        when(contratoMapper.toResponse(contrato)).thenReturn(response);

        contratoService.update(20L, request);

        verify(clienteRepository).findById(2L);
        assertThat(contrato.getCliente()).isEqualTo(novoCliente);
    }

    @Test
    void update_contratoNaoEncontrado_lancaResourceNotFoundException() {
        when(contratoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> contratoService.update(999L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");

        verify(contratoRepository, never()).save(any());
    }

    // ------------------------------------------------------------------ delete

    @Test
    void delete_contratoComSucesso() {
        when(contratoRepository.findById(20L)).thenReturn(Optional.of(contrato));

        contratoService.delete(20L);

        verify(contratoRepository).delete(contrato);
    }

    @Test
    void delete_idInvalido_lancaResourceNotFoundException() {
        when(contratoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> contratoService.delete(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");

        verify(contratoRepository, never()).delete(any(Contrato.class));
    }

    // ------------------------------------------------------------------ gerarCobrancasMensais

    @Test
    void gerarCobrancasMensais_geraCobrancaComSucesso() {
        LocalDate today = LocalDate.now();
        LocalDate mesInicio = today.withDayOfMonth(1);
        LocalDate mesFim = today.withDayOfMonth(today.lengthOfMonth());

        when(contratoRepository.findById(20L)).thenReturn(Optional.of(contrato));
        when(cobrancaRepository.existsByContratoIdAndDataVencimentoBetween(20L, mesInicio, mesFim))
                .thenReturn(false);
        when(cobrancaRepository.save(any(Cobranca.class))).thenAnswer(inv -> inv.getArgument(0));
        // gerarCobrancasMensais calls findContratoById a second time at the end
        when(contratoMapper.toResponse(contrato)).thenReturn(response);

        ContratoResponse result = contratoService.gerarCobrancasMensais(20L);

        assertThat(result).isNotNull();

        ArgumentCaptor<Cobranca> captor = ArgumentCaptor.forClass(Cobranca.class);
        verify(cobrancaRepository).save(captor.capture());
        Cobranca cobranca = captor.getValue();
        assertThat(cobranca.getContrato()).isEqualTo(contrato);
        assertThat(cobranca.getValorEsperado()).isEqualByComparingTo("1500.00");
        assertThat(cobranca.getStatus()).isEqualTo(StatusCobranca.PENDENTE);
        assertThat(cobranca.getDataVencimento().getMonthValue()).isEqualTo(today.getMonthValue());
        assertThat(cobranca.getDataVencimento().getYear()).isEqualTo(today.getYear());
    }

    @Test
    void gerarCobrancasMensais_cobrancaJaExiste_lancaBusinessException() {
        LocalDate today = LocalDate.now();
        LocalDate mesInicio = today.withDayOfMonth(1);
        LocalDate mesFim = today.withDayOfMonth(today.lengthOfMonth());

        when(contratoRepository.findById(20L)).thenReturn(Optional.of(contrato));
        when(cobrancaRepository.existsByContratoIdAndDataVencimentoBetween(20L, mesInicio, mesFim))
                .thenReturn(true);

        assertThatThrownBy(() -> contratoService.gerarCobrancasMensais(20L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("já foi gerada");

        verify(cobrancaRepository, never()).save(any());
    }

    @Test
    void gerarCobrancasMensais_periodicidadeNaoMensal_lancaBusinessException() {
        contrato.setPeriodicidade(Periodicidade.ANUAL);
        when(contratoRepository.findById(20L)).thenReturn(Optional.of(contrato));

        assertThatThrownBy(() -> contratoService.gerarCobrancasMensais(20L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("mensais");

        verify(cobrancaRepository, never()).save(any());
    }

    @Test
    void gerarCobrancasMensais_contratoInativo_lancaBusinessException() {
        contrato.setStatus(StatusContrato.ENCERRADO);
        when(contratoRepository.findById(20L)).thenReturn(Optional.of(contrato));

        assertThatThrownBy(() -> contratoService.gerarCobrancasMensais(20L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("ativo");

        verify(cobrancaRepository, never()).save(any());
    }

    // ------------------------------------------------------------------ findAll (paginado)

    @Test
    @SuppressWarnings("unchecked")
    void findAll_comFiltros_retornaPaginaCorreta() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Contrato> page = new PageImpl<>(List.of(contrato), pageable, 1);

        when(contratoRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(contratoMapper.toResponse(contrato)).thenReturn(response);

        Page<ContratoResponse> result = contratoService.findAll(
                "Licenca", 1L, StatusContrato.ATIVO, Periodicidade.MENSAL, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(20L);
        verify(contratoRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    @SuppressWarnings("unchecked")
    void findAll_semFiltros_retornaTodosContratos() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Contrato> page = new PageImpl<>(List.of(contrato, contrato), pageable, 2);

        when(contratoRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(contratoMapper.toResponse(any(Contrato.class))).thenReturn(response);

        Page<ContratoResponse> result = contratoService.findAll(null, null, null, null, pageable);

        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    // ------------------------------------------------------------------ findByClienteId

    @Test
    void findByClienteId_retornaListaDeContratos() {
        when(contratoRepository.findTop500ByClienteId(1L)).thenReturn(List.of(contrato));
        when(contratoMapper.toResponse(contrato)).thenReturn(response);

        List<ContratoResponse> result = contratoService.findByClienteId(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(20L);
        verify(contratoRepository).findTop500ByClienteId(1L);
    }

    @Test
    void findByClienteId_semContratos_retornaListaVazia() {
        when(contratoRepository.findTop500ByClienteId(99L)).thenReturn(List.of());

        List<ContratoResponse> result = contratoService.findByClienteId(99L);

        assertThat(result).isEmpty();
    }

    // ------------------------------------------------------------------ update — branches extras

    @Test
    void update_periodicidadeNula_usaMENSAL() {
        request.setPeriodicidade(null);

        when(contratoRepository.findById(20L)).thenReturn(Optional.of(contrato));
        when(contratoRepository.save(any(Contrato.class))).thenReturn(contrato);
        when(contratoMapper.toResponse(contrato)).thenReturn(response);

        contratoService.update(20L, request);

        assertThat(contrato.getPeriodicidade()).isEqualTo(Periodicidade.MENSAL);
    }

    @Test
    void update_statusNulo_usaATIVO() {
        request.setStatus(null);

        when(contratoRepository.findById(20L)).thenReturn(Optional.of(contrato));
        when(contratoRepository.save(any(Contrato.class))).thenReturn(contrato);
        when(contratoMapper.toResponse(contrato)).thenReturn(response);

        contratoService.update(20L, request);

        assertThat(contrato.getStatus()).isEqualTo(StatusContrato.ATIVO);
    }

    @Test
    void update_novoClienteNaoEncontrado_lancaResourceNotFoundException() {
        request.setClienteId(99L);

        when(contratoRepository.findById(20L)).thenReturn(Optional.of(contrato));
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> contratoService.update(20L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");

        verify(contratoRepository, never()).save(any());
    }

    // ------------------------------------------------------------------ create — status nulo

    @Test
    void create_statusNulo_usaATIVO() {
        request.setStatus(null);
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(contratoRepository.save(any(Contrato.class))).thenReturn(contrato);
        when(contratoMapper.toResponse(contrato)).thenReturn(response);

        contratoService.create(request);

        ArgumentCaptor<Contrato> captor = ArgumentCaptor.forClass(Contrato.class);
        verify(contratoRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(StatusContrato.ATIVO);
    }

    // ------------------------------------------------------------------ Specification lambda coverage

    @SuppressWarnings("unchecked")
    private Specification<Contrato> captureSpec(String search, Long clienteId,
                                                 StatusContrato status, Periodicidade periodicidade) {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Contrato> page = new PageImpl<>(List.of());
        ArgumentCaptor<Specification<Contrato>> captor = ArgumentCaptor.forClass(Specification.class);
        when(contratoRepository.findAll(captor.capture(), eq(pageable))).thenReturn(page);
        contratoService.findAll(search, clienteId, status, periodicidade, pageable);
        return captor.getValue();
    }

    @SuppressWarnings("unchecked")
    private void invokeSpec(Specification<Contrato> spec) {
        Root<Contrato> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Path<Object> descricaoPath = mock(Path.class);
        Path<Object> clientePath = mock(Path.class);
        Path<Object> clienteIdPath = mock(Path.class);
        Path<Object> statusPath = mock(Path.class);
        Path<Object> periodicidadePath = mock(Path.class);

        lenient().when(root.get("descricao")).thenReturn(descricaoPath);
        lenient().when(root.get("cliente")).thenReturn(clientePath);
        lenient().when(clientePath.get("id")).thenReturn(clienteIdPath);
        lenient().when(root.get("status")).thenReturn(statusPath);
        lenient().when(root.get("periodicidade")).thenReturn(periodicidadePath);
        lenient().when(cb.lower(any())).thenReturn(mock(jakarta.persistence.criteria.Expression.class));
        lenient().when(cb.like(any(), anyString())).thenReturn(mock(Predicate.class));
        lenient().when(cb.equal(any(), any())).thenReturn(mock(Predicate.class));
        lenient().when(cb.and(any(Predicate[].class))).thenReturn(mock(Predicate.class));

        spec.toPredicate(root, query, cb);
    }

    @Test
    void specificationLambda_semFiltros_executa() {
        invokeSpec(captureSpec(null, null, null, null));
    }

    @Test
    void specificationLambda_comSearch_executa() {
        invokeSpec(captureSpec("licenca", null, null, null));
    }

    @Test
    void specificationLambda_comClienteId_executa() {
        invokeSpec(captureSpec(null, 1L, null, null));
    }

    @Test
    void specificationLambda_comStatus_executa() {
        invokeSpec(captureSpec(null, null, StatusContrato.ATIVO, null));
    }

    @Test
    void specificationLambda_comPeriodicidade_executa() {
        invokeSpec(captureSpec(null, null, null, Periodicidade.MENSAL));
    }

    @Test
    void specificationLambda_comTodosFiltros_executa() {
        invokeSpec(captureSpec("teste", 1L, StatusContrato.ATIVO, Periodicidade.ANUAL));
    }
}
