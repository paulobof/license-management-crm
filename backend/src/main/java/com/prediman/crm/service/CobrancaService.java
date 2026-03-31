package com.prediman.crm.service;

import com.prediman.crm.dto.CobrancaRequest;
import com.prediman.crm.dto.CobrancaResponse;
import com.prediman.crm.dto.FinanceiroSummaryResponse;
import com.prediman.crm.exception.BusinessException;
import com.prediman.crm.exception.ResourceNotFoundException;
import com.prediman.crm.model.Cobranca;
import com.prediman.crm.model.Contrato;
import com.prediman.crm.model.enums.StatusCobranca;
import com.prediman.crm.repository.CobrancaRepository;
import com.prediman.crm.repository.ContratoRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CobrancaService {

    private final CobrancaRepository cobrancaRepository;
    private final ContratoRepository contratoRepository;

    @Transactional
    public CobrancaResponse create(CobrancaRequest request) {
        Contrato contrato = contratoRepository.findById(request.getContratoId())
                .orElseThrow(() -> new ResourceNotFoundException("Contrato", request.getContratoId()));

        Cobranca cobranca = Cobranca.builder()
                .contrato(contrato)
                .valorEsperado(request.getValorEsperado())
                .valorRecebido(request.getValorRecebido())
                .dataVencimento(request.getDataVencimento())
                .dataPagamento(request.getDataPagamento())
                .formaPagamento(request.getFormaPagamento())
                .comprovanteDriveId(request.getComprovanteDriveId())
                .status(request.getStatus() != null ? request.getStatus() : StatusCobranca.PENDENTE)
                .build();

        Cobranca saved = cobrancaRepository.save(cobranca);
        log.info("Cobrança criada com id: {}", saved.getId());
        return toResponse(saved);
    }

    @Transactional
    public CobrancaResponse update(Long id, CobrancaRequest request) {
        Cobranca cobranca = findCobrancaById(id);

        if (!cobranca.getContrato().getId().equals(request.getContratoId())) {
            Contrato novoContrato = contratoRepository.findById(request.getContratoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Contrato", request.getContratoId()));
            cobranca.setContrato(novoContrato);
        }

        cobranca.setValorEsperado(request.getValorEsperado());
        cobranca.setValorRecebido(request.getValorRecebido());
        cobranca.setDataVencimento(request.getDataVencimento());
        cobranca.setDataPagamento(request.getDataPagamento());
        cobranca.setFormaPagamento(request.getFormaPagamento());
        cobranca.setComprovanteDriveId(request.getComprovanteDriveId());
        if (request.getStatus() != null) {
            cobranca.setStatus(request.getStatus());
        }

        Cobranca saved = cobrancaRepository.save(cobranca);
        log.info("Cobrança atualizada com id: {}", saved.getId());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public CobrancaResponse findById(Long id) {
        return toResponse(findCobrancaById(id));
    }

    @Transactional(readOnly = true)
    public List<CobrancaResponse> findByContratoId(Long contratoId) {
        return cobrancaRepository.findByContratoId(contratoId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<CobrancaResponse> findAll(Long contratoId, StatusCobranca status,
                                          Integer month, Integer year, Pageable pageable) {
        Specification<Cobranca> spec = buildSpecification(contratoId, status, month, year);
        return cobrancaRepository.findAll(spec, pageable).map(this::toResponse);
    }

    @Transactional
    public CobrancaResponse registrarPagamento(Long id, CobrancaRequest request) {
        Cobranca cobranca = findCobrancaById(id);

        if (cobranca.getStatus() == StatusCobranca.CANCELADO) {
            throw new BusinessException("Não é possível registrar pagamento para cobrança cancelada");
        }

        if (request.getValorRecebido() == null) {
            throw new BusinessException("Valor recebido é obrigatório para registrar pagamento");
        }

        cobranca.setValorRecebido(request.getValorRecebido());
        cobranca.setDataPagamento(request.getDataPagamento() != null ? request.getDataPagamento() : LocalDate.now());
        cobranca.setFormaPagamento(request.getFormaPagamento());
        cobranca.setComprovanteDriveId(request.getComprovanteDriveId());
        cobranca.setStatus(StatusCobranca.PAGO);

        Cobranca saved = cobrancaRepository.save(cobranca);
        log.info("Pagamento registrado para cobrança id: {}", saved.getId());
        return toResponse(saved);
    }

    @Transactional
    public void delete(Long id) {
        Cobranca cobranca = findCobrancaById(id);
        cobrancaRepository.delete(cobranca);
        log.info("Cobrança excluida com id: {}", id);
    }

    @Transactional(readOnly = true)
    public FinanceiroSummaryResponse getFinanceiroSummary() {
        LocalDate today = LocalDate.now();
        LocalDate mesInicio = today.withDayOfMonth(1);
        LocalDate mesFim = today.withDayOfMonth(today.lengthOfMonth());
        LocalDate sete = today.plusDays(7);

        BigDecimal aReceber = cobrancaRepository.sumValorEsperadoByStatusAndVencimentoBetween(
                StatusCobranca.PENDENTE, mesInicio, mesFim);

        BigDecimal recebido = cobrancaRepository.sumValorRecebidoByStatusAndPagamentoBetween(
                StatusCobranca.PAGO, mesInicio, mesFim);

        BigDecimal emAtraso = cobrancaRepository.sumValorEsperadoByStatusAndVencimentoBefore(
                StatusCobranca.PENDENTE, today);

        BigDecimal vencendo7dias = cobrancaRepository.sumValorEsperadoByVencimentoBetween(
                StatusCobranca.PENDENTE, today, sete);

        return FinanceiroSummaryResponse.builder()
                .aReceber(aReceber != null ? aReceber : BigDecimal.ZERO)
                .recebido(recebido != null ? recebido : BigDecimal.ZERO)
                .emAtraso(emAtraso != null ? emAtraso : BigDecimal.ZERO)
                .vencendo7dias(vencendo7dias != null ? vencendo7dias : BigDecimal.ZERO)
                .build();
    }

    private Cobranca findCobrancaById(Long id) {
        return cobrancaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cobrança", id));
    }

    private Specification<Cobranca> buildSpecification(Long contratoId, StatusCobranca status,
                                                        Integer month, Integer year) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (contratoId != null) {
                predicates.add(cb.equal(root.get("contrato").get("id"), contratoId));
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (month != null && year != null) {
                LocalDate start = LocalDate.of(year, month, 1);
                LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
                predicates.add(cb.between(root.get("dataVencimento"), start, end));
            } else if (year != null) {
                LocalDate start = LocalDate.of(year, 1, 1);
                LocalDate end = LocalDate.of(year, 12, 31);
                predicates.add(cb.between(root.get("dataVencimento"), start, end));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    CobrancaResponse toResponse(Cobranca cobranca) {
        return CobrancaResponse.builder()
                .id(cobranca.getId())
                .valorEsperado(cobranca.getValorEsperado())
                .valorRecebido(cobranca.getValorRecebido())
                .dataVencimento(cobranca.getDataVencimento())
                .dataPagamento(cobranca.getDataPagamento())
                .formaPagamento(cobranca.getFormaPagamento())
                .comprovanteDriveId(cobranca.getComprovanteDriveId())
                .status(cobranca.getStatus())
                .statusCalculado(cobranca.getStatusCalculado())
                .contratoId(cobranca.getContrato().getId())
                .createdAt(cobranca.getCreatedAt())
                .updatedAt(cobranca.getUpdatedAt())
                .build();
    }
}
