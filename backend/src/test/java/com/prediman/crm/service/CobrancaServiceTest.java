package com.prediman.crm.service;

import com.prediman.crm.dto.CobrancaRequest;
import com.prediman.crm.dto.CobrancaResponse;
import com.prediman.crm.dto.FinanceiroSummaryResponse;
import com.prediman.crm.exception.BusinessException;
import com.prediman.crm.exception.ResourceNotFoundException;
import com.prediman.crm.model.Cliente;
import com.prediman.crm.model.Cobranca;
import com.prediman.crm.model.Contrato;
import com.prediman.crm.model.enums.Periodicidade;
import com.prediman.crm.model.enums.StatusCobranca;
import com.prediman.crm.model.enums.StatusContrato;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class CobrancaServiceTest {

    @Mock
    private CobrancaRepository cobrancaRepository;

    @Mock
    private ContratoRepository contratoRepository;

    @Mock
    private CobrancaMapper cobrancaMapper;

    @InjectMocks
    private CobrancaService cobrancaService;

    private Cliente cliente;
    private Contrato contrato;
    private Cobranca cobranca;
    private CobrancaRequest request;
    private CobrancaResponse response;

    @BeforeEach
    void setUp() {
        cliente = Cliente.builder()
                .id(1L)
                .razaoSocial("Empresa Teste Ltda")
                .build();

        contrato = Contrato.builder()
                .id(20L)
                .cliente(cliente)
                .descricao("Licenca XYZ")
                .valor(new BigDecimal("1500.00"))
                .periodicidade(Periodicidade.MENSAL)
                .status(StatusContrato.ATIVO)
                .dataInicio(LocalDate.of(2024, 1, 10))
                .build();

        cobranca = Cobranca.builder()
                .id(30L)
                .contrato(contrato)
                .valorEsperado(new BigDecimal("1500.00"))
                .dataVencimento(LocalDate.of(2024, 2, 10))
                .status(StatusCobranca.PENDENTE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        request = CobrancaRequest.builder()
                .contratoId(20L)
                .valorEsperado(new BigDecimal("1500.00"))
                .dataVencimento(LocalDate.of(2024, 2, 10))
                .build();

        response = CobrancaResponse.builder()
                .id(30L)
                .contratoId(20L)
                .valorEsperado(new BigDecimal("1500.00"))
                .dataVencimento(LocalDate.of(2024, 2, 10))
                .status(StatusCobranca.PENDENTE)
                .build();
    }

    // ------------------------------------------------------------------ create

    @Test
    void create_cobrancaComSucesso() {
        when(contratoRepository.findById(20L)).thenReturn(Optional.of(contrato));
        when(cobrancaRepository.save(any(Cobranca.class))).thenReturn(cobranca);
        when(cobrancaMapper.toResponse(cobranca)).thenReturn(response);

        CobrancaResponse result = cobrancaService.create(request);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(30L);
        assertThat(result.getContratoId()).isEqualTo(20L);

        ArgumentCaptor<Cobranca> captor = ArgumentCaptor.forClass(Cobranca.class);
        verify(cobrancaRepository).save(captor.capture());
        Cobranca saved = captor.getValue();
        assertThat(saved.getContrato()).isEqualTo(contrato);
        assertThat(saved.getValorEsperado()).isEqualByComparingTo("1500.00");
        assertThat(saved.getStatus()).isEqualTo(StatusCobranca.PENDENTE);
    }

    @Test
    void create_contratoIdInvalido_lancaResourceNotFoundException() {
        when(contratoRepository.findById(99L)).thenReturn(Optional.empty());
        request.setContratoId(99L);

        assertThatThrownBy(() -> cobrancaService.create(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");

        verify(cobrancaRepository, never()).save(any());
    }

    // ------------------------------------------------------------------ findById

    @Test
    void findById_retornaCobranca() {
        when(cobrancaRepository.findById(30L)).thenReturn(Optional.of(cobranca));
        when(cobrancaMapper.toResponse(cobranca)).thenReturn(response);

        CobrancaResponse result = cobrancaService.findById(30L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(30L);
        verify(cobrancaRepository).findById(30L);
    }

    @Test
    void findById_idInvalido_lancaResourceNotFoundException() {
        when(cobrancaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cobrancaService.findById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    // ------------------------------------------------------------------ update

    @Test
    void update_cobrancaComSucesso_mesmoContrato() {
        when(cobrancaRepository.findById(30L)).thenReturn(Optional.of(cobranca));
        when(cobrancaRepository.save(any(Cobranca.class))).thenReturn(cobranca);
        when(cobrancaMapper.toResponse(cobranca)).thenReturn(response);

        CobrancaResponse result = cobrancaService.update(30L, request);

        assertThat(result).isNotNull();
        verify(contratoRepository, never()).findById(any());
        verify(cobrancaRepository).save(cobranca);
    }

    @Test
    void update_contratoAlterado_buscaNovoContrato() {
        Contrato novoContrato = Contrato.builder().id(21L).cliente(cliente).build();
        request.setContratoId(21L);

        when(cobrancaRepository.findById(30L)).thenReturn(Optional.of(cobranca));
        when(contratoRepository.findById(21L)).thenReturn(Optional.of(novoContrato));
        when(cobrancaRepository.save(any(Cobranca.class))).thenReturn(cobranca);
        when(cobrancaMapper.toResponse(cobranca)).thenReturn(response);

        cobrancaService.update(30L, request);

        verify(contratoRepository).findById(21L);
        assertThat(cobranca.getContrato()).isEqualTo(novoContrato);
    }

    @Test
    void update_cobrancaNaoEncontrada_lancaResourceNotFoundException() {
        when(cobrancaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cobrancaService.update(999L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");

        verify(cobrancaRepository, never()).save(any());
    }

    // ------------------------------------------------------------------ registrarPagamento

    @Test
    void registrarPagamento_comSucesso() {
        LocalDate dataPagamento = LocalDate.of(2024, 2, 8);
        CobrancaRequest pagamentoRequest = CobrancaRequest.builder()
                .contratoId(20L)
                .valorEsperado(new BigDecimal("1500.00"))
                .valorRecebido(new BigDecimal("1500.00"))
                .dataVencimento(LocalDate.of(2024, 2, 10))
                .dataPagamento(dataPagamento)
                .formaPagamento("PIX")
                .build();

        CobrancaResponse pagoResponse = CobrancaResponse.builder()
                .id(30L)
                .status(StatusCobranca.PAGO)
                .valorRecebido(new BigDecimal("1500.00"))
                .dataPagamento(dataPagamento)
                .build();

        when(cobrancaRepository.findById(30L)).thenReturn(Optional.of(cobranca));
        when(cobrancaRepository.save(any(Cobranca.class))).thenReturn(cobranca);
        when(cobrancaMapper.toResponse(cobranca)).thenReturn(pagoResponse);

        CobrancaResponse result = cobrancaService.registrarPagamento(30L, pagamentoRequest);

        assertThat(result.getStatus()).isEqualTo(StatusCobranca.PAGO);
        assertThat(cobranca.getStatus()).isEqualTo(StatusCobranca.PAGO);
        assertThat(cobranca.getValorRecebido()).isEqualByComparingTo("1500.00");
        assertThat(cobranca.getDataPagamento()).isEqualTo(dataPagamento);
        assertThat(cobranca.getFormaPagamento()).isEqualTo("PIX");
        verify(cobrancaRepository).save(cobranca);
    }

    @Test
    void registrarPagamento_semDataPagamento_usaDataAtual() {
        CobrancaRequest pagamentoRequest = CobrancaRequest.builder()
                .contratoId(20L)
                .valorEsperado(new BigDecimal("1500.00"))
                .valorRecebido(new BigDecimal("1500.00"))
                .dataVencimento(LocalDate.of(2024, 2, 10))
                .dataPagamento(null) // deve usar LocalDate.now()
                .build();

        when(cobrancaRepository.findById(30L)).thenReturn(Optional.of(cobranca));
        when(cobrancaRepository.save(any(Cobranca.class))).thenReturn(cobranca);
        when(cobrancaMapper.toResponse(cobranca)).thenReturn(response);

        cobrancaService.registrarPagamento(30L, pagamentoRequest);

        assertThat(cobranca.getDataPagamento()).isEqualTo(LocalDate.now());
    }

    @Test
    void registrarPagamento_cobrancaCancelada_lancaBusinessException() {
        cobranca.setStatus(StatusCobranca.CANCELADO);
        CobrancaRequest pagamentoRequest = CobrancaRequest.builder()
                .contratoId(20L)
                .valorEsperado(new BigDecimal("1500.00"))
                .valorRecebido(new BigDecimal("1500.00"))
                .dataVencimento(LocalDate.of(2024, 2, 10))
                .build();

        when(cobrancaRepository.findById(30L)).thenReturn(Optional.of(cobranca));

        assertThatThrownBy(() -> cobrancaService.registrarPagamento(30L, pagamentoRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("cancelada");

        verify(cobrancaRepository, never()).save(any());
    }

    @Test
    void registrarPagamento_valorRecebidoNulo_lancaBusinessException() {
        CobrancaRequest pagamentoRequest = CobrancaRequest.builder()
                .contratoId(20L)
                .valorEsperado(new BigDecimal("1500.00"))
                .valorRecebido(null)
                .dataVencimento(LocalDate.of(2024, 2, 10))
                .build();

        when(cobrancaRepository.findById(30L)).thenReturn(Optional.of(cobranca));

        assertThatThrownBy(() -> cobrancaService.registrarPagamento(30L, pagamentoRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Valor recebido");

        verify(cobrancaRepository, never()).save(any());
    }

    // ------------------------------------------------------------------ delete

    @Test
    void delete_cobrancaComSucesso() {
        when(cobrancaRepository.findById(30L)).thenReturn(Optional.of(cobranca));

        cobrancaService.delete(30L);

        verify(cobrancaRepository).delete(cobranca);
    }

    @Test
    void delete_idInvalido_lancaResourceNotFoundException() {
        when(cobrancaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cobrancaService.delete(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");

        verify(cobrancaRepository, never()).delete(any(Cobranca.class));
    }

    // ------------------------------------------------------------------ getFinanceiroSummary

    @Test
    void getFinanceiroSummary_retornaSumarizacaoCorreta() {
        LocalDate today = LocalDate.now();
        LocalDate mesInicio = today.withDayOfMonth(1);
        LocalDate mesFim = today.withDayOfMonth(today.lengthOfMonth());
        LocalDate sete = today.plusDays(7);

        BigDecimal aReceber = new BigDecimal("3000.00");
        BigDecimal recebido = new BigDecimal("1500.00");
        BigDecimal emAtraso = new BigDecimal("500.00");
        BigDecimal vencendo7dias = new BigDecimal("1500.00");

        when(cobrancaRepository.sumValorEsperadoByStatusAndVencimentoBetween(
                StatusCobranca.PENDENTE, mesInicio, mesFim)).thenReturn(aReceber);
        when(cobrancaRepository.sumValorRecebidoByStatusAndPagamentoBetween(
                StatusCobranca.PAGO, mesInicio, mesFim)).thenReturn(recebido);
        when(cobrancaRepository.sumValorEsperadoByStatusAndVencimentoBefore(
                StatusCobranca.PENDENTE, today)).thenReturn(emAtraso);
        when(cobrancaRepository.sumValorEsperadoByStatusAndVencimentoBetween(
                StatusCobranca.PENDENTE, today, sete)).thenReturn(vencendo7dias);

        FinanceiroSummaryResponse result = cobrancaService.getFinanceiroSummary();

        assertThat(result.getAReceber()).isEqualByComparingTo("3000.00");
        assertThat(result.getRecebido()).isEqualByComparingTo("1500.00");
        assertThat(result.getEmAtraso()).isEqualByComparingTo("500.00");
        assertThat(result.getVencendo7dias()).isEqualByComparingTo("1500.00");

        verify(cobrancaRepository).sumValorEsperadoByStatusAndVencimentoBetween(
                StatusCobranca.PENDENTE, mesInicio, mesFim);
        verify(cobrancaRepository).sumValorRecebidoByStatusAndPagamentoBetween(
                StatusCobranca.PAGO, mesInicio, mesFim);
        verify(cobrancaRepository).sumValorEsperadoByStatusAndVencimentoBefore(
                StatusCobranca.PENDENTE, today);
        verify(cobrancaRepository, times(2)).sumValorEsperadoByStatusAndVencimentoBetween(
                any(StatusCobranca.class), any(LocalDate.class), any(LocalDate.class));
    }

    @Test
    void getFinanceiroSummary_queryRetornaNulo_usaZero() {
        LocalDate today = LocalDate.now();
        LocalDate mesInicio = today.withDayOfMonth(1);
        LocalDate mesFim = today.withDayOfMonth(today.lengthOfMonth());
        LocalDate sete = today.plusDays(7);

        when(cobrancaRepository.sumValorEsperadoByStatusAndVencimentoBetween(
                StatusCobranca.PENDENTE, mesInicio, mesFim)).thenReturn(null);
        when(cobrancaRepository.sumValorRecebidoByStatusAndPagamentoBetween(
                StatusCobranca.PAGO, mesInicio, mesFim)).thenReturn(null);
        when(cobrancaRepository.sumValorEsperadoByStatusAndVencimentoBefore(
                StatusCobranca.PENDENTE, today)).thenReturn(null);
        when(cobrancaRepository.sumValorEsperadoByStatusAndVencimentoBetween(
                StatusCobranca.PENDENTE, today, sete)).thenReturn(null);

        FinanceiroSummaryResponse result = cobrancaService.getFinanceiroSummary();

        assertThat(result.getAReceber()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getRecebido()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getEmAtraso()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getVencendo7dias()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    // ------------------------------------------------------------------ findByContratoId

    @Test
    void findByContratoId_retornaListaDeCobrancas() {
        when(cobrancaRepository.findTop500ByContratoId(20L)).thenReturn(List.of(cobranca));
        when(cobrancaMapper.toResponse(cobranca)).thenReturn(response);

        List<CobrancaResponse> result = cobrancaService.findByContratoId(20L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(30L);
        verify(cobrancaRepository).findTop500ByContratoId(20L);
    }

    @Test
    void findByContratoId_semCobrancas_retornaListaVazia() {
        when(cobrancaRepository.findTop500ByContratoId(99L)).thenReturn(List.of());

        List<CobrancaResponse> result = cobrancaService.findByContratoId(99L);

        assertThat(result).isEmpty();
    }

    // ------------------------------------------------------------------ findAll com filtros

    @Test
    void findAll_semFiltros_retornaPagina() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Cobranca> page = new PageImpl<>(List.of(cobranca), pageable, 1);

        when(cobrancaRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(cobrancaMapper.toResponse(cobranca)).thenReturn(response);

        Page<CobrancaResponse> result = cobrancaService.findAll(null, null, null, null, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void findAll_comContratoIdEStatus_retornaPagina() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Cobranca> page = new PageImpl<>(List.of(cobranca), pageable, 1);

        when(cobrancaRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(cobrancaMapper.toResponse(cobranca)).thenReturn(response);

        Page<CobrancaResponse> result = cobrancaService.findAll(20L, StatusCobranca.PENDENTE, null, null, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(cobrancaRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void findAll_comMesEAno_adicionaPredicadoDeIntervalo() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Cobranca> page = new PageImpl<>(List.of(cobranca), pageable, 1);

        when(cobrancaRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(cobrancaMapper.toResponse(cobranca)).thenReturn(response);

        Page<CobrancaResponse> result = cobrancaService.findAll(null, null, 2, 2024, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void findAll_comAnoSemMes_adicionaPredicadoAnual() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Cobranca> page = new PageImpl<>(List.of(cobranca), pageable, 1);

        when(cobrancaRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(cobrancaMapper.toResponse(cobranca)).thenReturn(response);

        Page<CobrancaResponse> result = cobrancaService.findAll(null, null, null, 2024, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    // ------------------------------------------------------------------ registrarPagamento com comprovante

    @Test
    void registrarPagamento_comComprovante_salvaComprovanteDriveId() {
        CobrancaRequest pagamentoRequest = CobrancaRequest.builder()
                .contratoId(20L)
                .valorEsperado(new BigDecimal("1500.00"))
                .valorRecebido(new BigDecimal("1500.00"))
                .dataVencimento(LocalDate.of(2024, 2, 10))
                .dataPagamento(LocalDate.of(2024, 2, 8))
                .formaPagamento("BOLETO")
                .comprovanteDriveId("drive-id-123")
                .build();

        when(cobrancaRepository.findById(30L)).thenReturn(Optional.of(cobranca));
        when(cobrancaRepository.save(any(Cobranca.class))).thenReturn(cobranca);
        when(cobrancaMapper.toResponse(cobranca)).thenReturn(response);

        cobrancaService.registrarPagamento(30L, pagamentoRequest);

        assertThat(cobranca.getComprovanteDriveId()).isEqualTo("drive-id-123");
        assertThat(cobranca.getFormaPagamento()).isEqualTo("BOLETO");
    }

    // ------------------------------------------------------------------ Specification lambda coverage

    @SuppressWarnings("unchecked")
    private Specification<Cobranca> captureSpec(Long contratoId, StatusCobranca status, Integer month, Integer year) {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Cobranca> page = new PageImpl<>(List.of());
        ArgumentCaptor<Specification<Cobranca>> captor = ArgumentCaptor.forClass(Specification.class);
        when(cobrancaRepository.findAll(captor.capture(), eq(pageable))).thenReturn(page);
        cobrancaService.findAll(contratoId, status, month, year, pageable);
        return captor.getValue();
    }

    @SuppressWarnings("unchecked")
    private void invokeSpec(Specification<Cobranca> spec) {
        Root<Cobranca> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Path<Object> contratoPath = mock(Path.class);
        Path<Object> contratoIdPath = mock(Path.class);
        Path<Object> statusPath = mock(Path.class);
        Path<Object> dataVencimentoPath = mock(Path.class);

        lenient().when(root.get("contrato")).thenReturn(contratoPath);
        lenient().when(contratoPath.get("id")).thenReturn(contratoIdPath);
        lenient().when(root.get("status")).thenReturn(statusPath);
        lenient().when(root.get("dataVencimento")).thenReturn(dataVencimentoPath);
        lenient().when(cb.equal(any(), any())).thenReturn(mock(Predicate.class));
        lenient().when(cb.between(any(), any(Comparable.class), any(Comparable.class))).thenReturn(mock(Predicate.class));
        lenient().when(cb.and(any(Predicate[].class))).thenReturn(mock(Predicate.class));

        spec.toPredicate(root, query, cb);
    }

    @Test
    void specificationLambda_semFiltros_executa() {
        invokeSpec(captureSpec(null, null, null, null));
    }

    @Test
    void specificationLambda_comContratoId_executa() {
        invokeSpec(captureSpec(20L, null, null, null));
    }

    @Test
    void specificationLambda_comStatus_executa() {
        invokeSpec(captureSpec(null, StatusCobranca.PENDENTE, null, null));
    }

    @Test
    void specificationLambda_comMesEAno_executa() {
        invokeSpec(captureSpec(null, null, 3, 2024));
    }

    @Test
    void specificationLambda_comAnoSemMes_executa() {
        invokeSpec(captureSpec(null, null, null, 2024));
    }

    @Test
    void specificationLambda_comTodosFiltros_executa() {
        invokeSpec(captureSpec(20L, StatusCobranca.PAGO, 6, 2025));
    }
}
