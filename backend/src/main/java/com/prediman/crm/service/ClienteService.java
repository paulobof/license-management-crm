package com.prediman.crm.service;

import com.prediman.crm.dto.ClienteRequest;
import com.prediman.crm.dto.ClienteResponse;
import com.prediman.crm.exception.BusinessException;
import com.prediman.crm.exception.ResourceNotFoundException;
import com.prediman.crm.model.Cliente;
import com.prediman.crm.model.Contato;
import com.prediman.crm.model.Endereco;
import com.prediman.crm.model.enums.StatusCliente;
import com.prediman.crm.model.enums.StatusCobranca;
import com.prediman.crm.model.enums.StatusContrato;
import com.prediman.crm.model.enums.TipoPessoa;
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

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final ContratoRepository contratoRepository;
    private final CobrancaRepository cobrancaRepository;
    private final ClienteMapper clienteMapper;
    private final GoogleDriveService googleDriveService;

    @Transactional
    public ClienteResponse create(ClienteRequest request) {
        if (StringUtils.hasText(request.getCnpj())
                && clienteRepository.existsByCnpj(request.getCnpj())) {
            throw new BusinessException("CNPJ já cadastrado: " + request.getCnpj());
        }
        if (StringUtils.hasText(request.getCpf())
                && clienteRepository.existsByCpf(request.getCpf())) {
            throw new BusinessException("CPF já cadastrado: " + request.getCpf());
        }

        Cliente cliente = clienteMapper.toEntity(request);
        Cliente saved = clienteRepository.save(cliente);
        log.info("Cliente criado com id: {}", saved.getId());

        // A pasta do Drive so pode ser nomeada apos o save, pois depende do id gerado.
        if (!StringUtils.hasText(saved.getGoogleDriveFolderId())) {
            criarPastaDrive(saved);
        }

        return clienteMapper.toResponse(saved);
    }

    @Transactional
    public ClienteResponse update(Long id, ClienteRequest request) {
        Cliente cliente = findClienteById(id);

        if (StringUtils.hasText(request.getCnpj())
                && clienteRepository.existsByCnpjAndIdNot(request.getCnpj(), id)) {
            throw new BusinessException("CNPJ já cadastrado: " + request.getCnpj());
        }
        if (StringUtils.hasText(request.getCpf())
                && clienteRepository.existsByCpfAndIdNot(request.getCpf(), id)) {
            throw new BusinessException("CPF já cadastrado: " + request.getCpf());
        }

        if (StringUtils.hasText(request.getTipoPessoa())) {
            cliente.setTipoPessoa(TipoPessoa.valueOf(request.getTipoPessoa()));
        }
        cliente.setRazaoSocial(request.getRazaoSocial());
        cliente.setNomeFantasia(request.getNomeFantasia());
        cliente.setCnpj(request.getCnpj());
        cliente.setCpf(request.getCpf());
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

        boolean temContratosAtivos = cliente.getContratos().stream()
                .anyMatch(c -> c.getStatus() == StatusContrato.ATIVO);
        if (temContratosAtivos) {
            throw new BusinessException("Não é possível excluir cliente com contratos ativos");
        }

        boolean temCobrancasPagas = cliente.getContratos().stream()
                .flatMap(c -> c.getCobrancas().stream())
                .anyMatch(cob -> cob.getStatus() == StatusCobranca.PAGO);
        if (temCobrancasPagas) {
            throw new BusinessException("Não é possível excluir cliente com cobranças pagas");
        }

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

    /**
     * Retorna o id da pasta do cliente no Google Drive, criando-a sob demanda quando ainda nao existir.
     * Devolve {@code null} quando o Drive esta desabilitado ou a criacao falha.
     */
    @Transactional
    public String obterOuCriarPastaDrive(Long clienteId) {
        Cliente cliente = findClienteById(clienteId);
        if (StringUtils.hasText(cliente.getGoogleDriveFolderId())) {
            return cliente.getGoogleDriveFolderId();
        }
        return criarPastaDrive(cliente);
    }

    /**
     * Cria a pasta do cliente no Google Drive e persiste o id.
     * Qualquer falha e apenas registrada em log — o cadastro do cliente nunca e abortado por causa do Drive.
     */
    private String criarPastaDrive(Cliente cliente) {
        if (!googleDriveService.isEnabled()) {
            return null;
        }

        try {
            String folderId = googleDriveService.createFolder(montarNomePasta(cliente), null);
            if (!StringUtils.hasText(folderId)) {
                log.warn("Google Drive nao retornou id de pasta para o cliente {}", cliente.getId());
                return null;
            }
            cliente.setGoogleDriveFolderId(folderId);
            clienteRepository.save(cliente);
            log.info("Pasta do cliente {} criada no Google Drive: {}", cliente.getId(), folderId);
            return folderId;
        } catch (RuntimeException e) {
            log.warn("Falha ao criar pasta no Google Drive para o cliente {}: {}", cliente.getId(), e.getMessage());
            return null;
        }
    }

    /**
     * Nome padrao da pasta do cliente no Drive: "0001 - Nome Fantasia" (ou razao social, se nao houver fantasia).
     */
    static String montarNomePasta(Cliente cliente) {
        String nome = StringUtils.hasText(cliente.getNomeFantasia())
                ? cliente.getNomeFantasia()
                : cliente.getRazaoSocial();
        return String.format("%04d - %s", cliente.getId(), nome);
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
                Predicate byCpf = cb.like(cb.lower(root.get("cpf")), pattern);
                predicates.add(cb.or(byRazaoSocial, byNomeFantasia, byCnpj, byCpf));
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
