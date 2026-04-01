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
public class ContratoService {

    private final ContratoRepository contratoRepository;
    private final ClienteRepository clienteRepository;
    private final CobrancaRepository cobrancaRepository;
    private final ContratoMapper contratoMapper;

    @Transactional
    public ContratoResponse create(ContratoRequest request) {
        Cliente cliente = clienteRepository.findById(request.getClienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", request.getClienteId()));

        Contrato contrato = Contrato.builder()
                .cliente(cliente)
                .descricao(request.getDescricao())
                .valor(request.getValor())
                .periodicidade(request.getPeriodicidade() != null ? request.getPeriodicidade() : Periodicidade.MENSAL)
                .dataInicio(request.getDataInicio())
                .dataFim(request.getDataFim())
                .status(request.getStatus() != null ? request.getStatus() : StatusContrato.ATIVO)
                .observacoes(request.getObservacoes())
                .build();

        Contrato saved = contratoRepository.save(contrato);
        log.info("Contrato criado com id: {}", saved.getId());
        return contratoMapper.toResponse(saved);
    }

    @Transactional
    public ContratoResponse update(Long id, ContratoRequest request) {
        Contrato contrato = findContratoById(id);

        if (!contrato.getCliente().getId().equals(request.getClienteId())) {
            Cliente novoCliente = clienteRepository.findById(request.getClienteId())
                    .orElseThrow(() -> new ResourceNotFoundException("Cliente", request.getClienteId()));
            contrato.setCliente(novoCliente);
        }

        contrato.setDescricao(request.getDescricao());
        contrato.setValor(request.getValor());
        contrato.setPeriodicidade(request.getPeriodicidade() != null ? request.getPeriodicidade() : Periodicidade.MENSAL);
        contrato.setDataInicio(request.getDataInicio());
        contrato.setDataFim(request.getDataFim());
        contrato.setStatus(request.getStatus() != null ? request.getStatus() : StatusContrato.ATIVO);
        contrato.setObservacoes(request.getObservacoes());

        Contrato saved = contratoRepository.save(contrato);
        log.info("Contrato atualizado com id: {}", saved.getId());
        return contratoMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public ContratoResponse findById(Long id) {
        return contratoMapper.toResponse(findContratoById(id));
    }

    @Transactional(readOnly = true)
    public List<ContratoResponse> findByClienteId(Long clienteId) {
        return contratoRepository.findTop500ByClienteId(clienteId)
                .stream()
                .map(contratoMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<ContratoResponse> findAll(String search, Long clienteId,
                                          StatusContrato status, Periodicidade periodicidade,
                                          Pageable pageable) {
        Specification<Contrato> spec = buildSpecification(search, clienteId, status, periodicidade);
        return contratoRepository.findAll(spec, pageable).map(contratoMapper::toResponse);
    }

    @Transactional
    public void delete(Long id) {
        Contrato contrato = findContratoById(id);
        contratoRepository.delete(contrato);
        log.info("Contrato excluido com id: {}", id);
    }

    @Transactional
    public ContratoResponse gerarCobrancasMensais(Long contratoId) {
        Contrato contrato = findContratoById(contratoId);

        if (contrato.getPeriodicidade() != Periodicidade.MENSAL) {
            throw new BusinessException("Geração automática de cobranças disponível apenas para contratos mensais");
        }

        if (contrato.getStatus() != StatusContrato.ATIVO) {
            throw new BusinessException("Contrato não está ativo");
        }

        LocalDate today = LocalDate.now();
        LocalDate mesInicio = today.withDayOfMonth(1);
        LocalDate mesFim = today.withDayOfMonth(today.lengthOfMonth());

        boolean jaExiste = cobrancaRepository.existsByContratoIdAndDataVencimentoBetween(
                contratoId, mesInicio, mesFim);

        if (jaExiste) {
            throw new BusinessException("Cobrança para o mês atual já foi gerada para este contrato");
        }

        LocalDate dataVencimento = contrato.getDataInicio().withMonth(today.getMonthValue())
                .withYear(today.getYear());

        Cobranca cobranca = Cobranca.builder()
                .contrato(contrato)
                .valorEsperado(contrato.getValor())
                .dataVencimento(dataVencimento)
                .status(StatusCobranca.PENDENTE)
                .build();

        cobrancaRepository.save(cobranca);
        log.info("Cobrança mensal gerada para contrato id: {}, vencimento: {}", contratoId, dataVencimento);

        return contratoMapper.toResponse(findContratoById(contratoId));
    }

    private Contrato findContratoById(Long id) {
        return contratoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contrato", id));
    }

    private Specification<Contrato> buildSpecification(String search, Long clienteId,
                                                        StatusContrato status, Periodicidade periodicidade) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(search)) {
                String pattern = "%" + search.toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("descricao")), pattern));
            }

            if (clienteId != null) {
                predicates.add(cb.equal(root.get("cliente").get("id"), clienteId));
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (periodicidade != null) {
                predicates.add(cb.equal(root.get("periodicidade"), periodicidade));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

}
