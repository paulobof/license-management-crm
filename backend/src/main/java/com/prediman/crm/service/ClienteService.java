package com.prediman.crm.service;

import com.prediman.crm.dto.ClienteRequest;
import com.prediman.crm.dto.ClienteResponse;
import com.prediman.crm.exception.BusinessException;
import com.prediman.crm.exception.ResourceNotFoundException;
import com.prediman.crm.model.Cliente;
import com.prediman.crm.model.Contato;
import com.prediman.crm.model.Endereco;
import com.prediman.crm.model.enums.StatusCliente;
import com.prediman.crm.repository.ClienteRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final ClienteMapper clienteMapper;

    @Transactional
    public ClienteResponse create(ClienteRequest request) {
        if (StringUtils.hasText(request.getCnpj())
                && clienteRepository.existsByCnpj(request.getCnpj())) {
            throw new BusinessException("CNPJ já cadastrado: " + request.getCnpj());
        }

        Cliente cliente = clienteMapper.toEntity(request);
        Cliente saved = clienteRepository.save(cliente);
        log.info("Cliente criado com id: {}", saved.getId());
        return clienteMapper.toResponse(saved);
    }

    @Transactional
    public ClienteResponse update(Long id, ClienteRequest request) {
        Cliente cliente = findClienteById(id);

        if (StringUtils.hasText(request.getCnpj())
                && clienteRepository.existsByCnpjAndIdNot(request.getCnpj(), id)) {
            throw new BusinessException("CNPJ já cadastrado: " + request.getCnpj());
        }

        cliente.setRazaoSocial(request.getRazaoSocial());
        cliente.setNomeFantasia(request.getNomeFantasia());
        cliente.setCnpj(request.getCnpj());
        cliente.setIe(request.getIe());
        cliente.setSegmento(request.getSegmento());
        cliente.setDataFundacao(request.getDataFundacao());
        cliente.setDataInicioCliente(request.getDataInicioCliente());
        cliente.setGoogleDriveFolderId(request.getGoogleDriveFolderId());

        // Clear and re-add: orphanRemoval handles cleanup
        cliente.getContatos().clear();
        cliente.getEnderecos().clear();

        if (request.getContatos() != null) {
            request.getContatos().forEach(dto -> {
                Contato contato = clienteMapper.toContatoEntity(dto);
                contato.setCliente(cliente);
                cliente.getContatos().add(contato);
            });
        }

        if (request.getEnderecos() != null) {
            request.getEnderecos().forEach(dto -> {
                Endereco endereco = clienteMapper.toEnderecoEntity(dto);
                endereco.setCliente(cliente);
                cliente.getEnderecos().add(endereco);
            });
        }

        Cliente saved = clienteRepository.save(cliente);
        log.info("Cliente atualizado com id: {}", saved.getId());
        return clienteMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public ClienteResponse findById(Long id) {
        return clienteMapper.toResponse(findClienteById(id));
    }

    @Transactional(readOnly = true)
    public Page<ClienteResponse> findAll(String search, StatusCliente status, Pageable pageable) {
        Specification<Cliente> spec = buildSpecification(search, status);
        return clienteRepository.findAll(spec, pageable).map(clienteMapper::toResponse);
    }

    @Transactional
    public void delete(Long id) {
        Cliente cliente = findClienteById(id);
        clienteRepository.delete(cliente);
        log.info("Cliente excluído com id: {}", id);
    }

    @Transactional
    public ClienteResponse toggleStatus(Long id) {
        Cliente cliente = findClienteById(id);
        StatusCliente novoStatus = cliente.getStatus() == StatusCliente.ATIVO
                ? StatusCliente.INATIVO
                : StatusCliente.ATIVO;
        cliente.setStatus(novoStatus);
        Cliente saved = clienteRepository.save(cliente);
        log.info("Status do cliente {} alterado para {}", id, novoStatus);
        return clienteMapper.toResponse(saved);
    }

    private Cliente findClienteById(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", id));
    }

    private Specification<Cliente> buildSpecification(String search, StatusCliente status) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(search)) {
                String pattern = "%" + search.toLowerCase() + "%";
                Predicate byRazaoSocial = cb.like(cb.lower(root.get("razaoSocial")), pattern);
                Predicate byNomeFantasia = cb.like(cb.lower(root.get("nomeFantasia")), pattern);
                Predicate byCnpj = cb.like(cb.lower(root.get("cnpj")), pattern);
                predicates.add(cb.or(byRazaoSocial, byNomeFantasia, byCnpj));
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
