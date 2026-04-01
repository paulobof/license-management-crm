package com.prediman.crm.service;

import com.prediman.crm.dto.ClienteRequest;
import com.prediman.crm.dto.ClienteResponse;
import com.prediman.crm.dto.ContatoDTO;
import com.prediman.crm.dto.EnderecoDTO;
import com.prediman.crm.exception.BusinessException;
import com.prediman.crm.exception.ResourceNotFoundException;
import com.prediman.crm.model.Cliente;
import com.prediman.crm.model.Cobranca;
import com.prediman.crm.model.Contato;
import com.prediman.crm.model.Contrato;
import com.prediman.crm.model.Endereco;
import com.prediman.crm.model.enums.StatusCliente;
import com.prediman.crm.model.enums.StatusCobranca;
import com.prediman.crm.model.enums.StatusContrato;
import com.prediman.crm.model.enums.TipoPessoa;
import com.prediman.crm.repository.ClienteRepository;
import com.prediman.crm.repository.CobrancaRepository;
import com.prediman.crm.repository.ContratoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
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
@DisplayName("ClienteService — testes unitários")
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ContratoRepository contratoRepository;

    @Mock
    private CobrancaRepository cobrancaRepository;

    @Mock
    private ClienteMapper clienteMapper;

    @InjectMocks
    private ClienteService clienteService;

    // ---------------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------------

    private ClienteRequest buildRequest(String cnpj, String cpf) {
        return ClienteRequest.builder()
                .tipoPessoa("JURIDICA")
                .razaoSocial("Empresa Teste LTDA")
                .nomeFantasia("Empresa Teste")
                .cnpj(cnpj)
                .cpf(cpf)
                .build();
    }

    private Cliente buildCliente(Long id, StatusCliente status) {
        return Cliente.builder()
                .id(id)
                .razaoSocial("Empresa Teste LTDA")
                .nomeFantasia("Empresa Teste")
                .cnpj("12.345.678/0001-00")
                .tipoPessoa(TipoPessoa.JURIDICA)
                .status(status)
                .build();
    }

    private ClienteResponse buildResponse(Long id, StatusCliente status) {
        return ClienteResponse.builder()
                .id(id)
                .razaoSocial("Empresa Teste LTDA")
                .status(status)
                .tipoPessoa(TipoPessoa.JURIDICA)
                .build();
    }

    // ---------------------------------------------------------------------------
    // create
    // ---------------------------------------------------------------------------

    @Test
    @DisplayName("create com dados válidos persiste e retorna ClienteResponse")
    void create_dadosValidos_persisteERetornaResponse() {
        ClienteRequest request = buildRequest("12.345.678/0001-00", null);
        Cliente entity = buildCliente(1L, StatusCliente.ATIVO);
        ClienteResponse response = buildResponse(1L, StatusCliente.ATIVO);

        when(clienteRepository.existsByCnpj("12.345.678/0001-00")).thenReturn(false);
        when(clienteMapper.toEntity(request)).thenReturn(entity);
        when(clienteRepository.save(entity)).thenReturn(entity);
        when(clienteMapper.toResponse(entity)).thenReturn(response);

        ClienteResponse result = clienteService.create(request);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getStatus()).isEqualTo(StatusCliente.ATIVO);
        verify(clienteRepository).save(entity);
        verify(clienteMapper).toResponse(entity);
    }

    @Test
    @DisplayName("create com contatos e endereços delega mapeamento ao ClienteMapper")
    void create_comContatosEEnderecos_delegaMapeamento() {
        ContatoDTO contatoDTO = ContatoDTO.builder().nome("João").email("joao@empresa.com").build();
        EnderecoDTO enderecoDTO = EnderecoDTO.builder().cidade("São Paulo").build();

        ClienteRequest request = ClienteRequest.builder()
                .razaoSocial("Empresa Teste LTDA")
                .cnpj("12.345.678/0001-00")
                .contatos(List.of(contatoDTO))
                .enderecos(List.of(enderecoDTO))
                .build();

        Cliente entity = buildCliente(1L, StatusCliente.ATIVO);
        ClienteResponse response = buildResponse(1L, StatusCliente.ATIVO);

        when(clienteRepository.existsByCnpj("12.345.678/0001-00")).thenReturn(false);
        when(clienteMapper.toEntity(request)).thenReturn(entity);
        when(clienteRepository.save(entity)).thenReturn(entity);
        when(clienteMapper.toResponse(entity)).thenReturn(response);

        ClienteResponse result = clienteService.create(request);

        assertThat(result).isNotNull();
        verify(clienteMapper).toEntity(request);
    }

    @Test
    @DisplayName("create com CNPJ duplicado lança BusinessException")
    void create_cnpjDuplicado_lancaBusinessException() {
        ClienteRequest request = buildRequest("12.345.678/0001-00", null);
        when(clienteRepository.existsByCnpj("12.345.678/0001-00")).thenReturn(true);

        assertThatThrownBy(() -> clienteService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("CNPJ já cadastrado");

        verify(clienteRepository, never()).save(any());
    }

    @Test
    @DisplayName("create com CPF duplicado lança BusinessException")
    void create_cpfDuplicado_lancaBusinessException() {
        ClienteRequest request = buildRequest(null, "123.456.789-00");
        when(clienteRepository.existsByCpf("123.456.789-00")).thenReturn(true);

        assertThatThrownBy(() -> clienteService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("CPF já cadastrado");

        verify(clienteRepository, never()).save(any());
    }

    // ---------------------------------------------------------------------------
    // findById
    // ---------------------------------------------------------------------------

    @Test
    @DisplayName("findById com id existente retorna ClienteResponse")
    void findById_idExistente_retornaClienteResponse() {
        Cliente entity = buildCliente(10L, StatusCliente.ATIVO);
        ClienteResponse response = buildResponse(10L, StatusCliente.ATIVO);

        when(clienteRepository.findById(10L)).thenReturn(Optional.of(entity));
        when(clienteMapper.toResponse(entity)).thenReturn(response);

        ClienteResponse result = clienteService.findById(10L);

        assertThat(result.getId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("findById com id inexistente lança ResourceNotFoundException")
    void findById_idInexistente_lancaResourceNotFoundException() {
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clienteService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    // ---------------------------------------------------------------------------
    // update
    // ---------------------------------------------------------------------------

    @Test
    @DisplayName("update com dados válidos atualiza e retorna ClienteResponse")
    void update_dadosValidos_atualizaERetornaResponse() {
        Long id = 5L;
        ClienteRequest request = buildRequest("12.345.678/0001-00", null);

        Cliente entity = buildCliente(id, StatusCliente.ATIVO);
        entity.setContatos(new ArrayList<>());
        entity.setEnderecos(new ArrayList<>());
        ClienteResponse response = buildResponse(id, StatusCliente.ATIVO);

        when(clienteRepository.findById(id)).thenReturn(Optional.of(entity));
        when(clienteRepository.existsByCnpjAndIdNot("12.345.678/0001-00", id)).thenReturn(false);
        when(clienteRepository.save(entity)).thenReturn(entity);
        when(clienteMapper.toResponse(entity)).thenReturn(response);

        ClienteResponse result = clienteService.update(id, request);

        assertThat(result.getId()).isEqualTo(id);
        verify(clienteRepository).save(entity);
    }

    @Test
    @DisplayName("update com CNPJ já usado por outro cliente lança BusinessException")
    void update_cnpjDuplicadoOutroCliente_lancaBusinessException() {
        Long id = 5L;
        ClienteRequest request = buildRequest("99.999.999/0001-99", null);

        Cliente entity = buildCliente(id, StatusCliente.ATIVO);
        entity.setContatos(new ArrayList<>());
        entity.setEnderecos(new ArrayList<>());

        when(clienteRepository.findById(id)).thenReturn(Optional.of(entity));
        when(clienteRepository.existsByCnpjAndIdNot("99.999.999/0001-99", id)).thenReturn(true);

        assertThatThrownBy(() -> clienteService.update(id, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("CNPJ já cadastrado");

        verify(clienteRepository, never()).save(any());
    }

    // ---------------------------------------------------------------------------
    // delete
    // ---------------------------------------------------------------------------

    @Test
    @DisplayName("delete sem contratos ativos nem cobranças pagas remove o cliente")
    void delete_semContratoAtivoNemCobrancaPaga_removeCliente() {
        Long id = 7L;
        Cliente entity = buildCliente(id, StatusCliente.INATIVO);
        // sem contratos
        entity.getContratos().clear();

        when(clienteRepository.findById(id)).thenReturn(Optional.of(entity));

        clienteService.delete(id);

        verify(clienteRepository).delete(entity);
    }

    @Test
    @DisplayName("delete com contrato ativo lança BusinessException")
    void delete_comContratoAtivo_lancaBusinessException() {
        Long id = 7L;
        Cliente entity = buildCliente(id, StatusCliente.ATIVO);

        Contrato contratoAtivo = Contrato.builder()
                .id(1L)
                .status(StatusContrato.ATIVO)
                .descricao("Contrato ativo")
                .valor(BigDecimal.TEN)
                .cobrancas(new ArrayList<>())
                .build();
        entity.getContratos().add(contratoAtivo);

        when(clienteRepository.findById(id)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> clienteService.delete(id))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("contratos ativos");

        verify(clienteRepository, never()).delete(any(Cliente.class));
    }

    @Test
    @DisplayName("delete com cobranças pagas lança BusinessException")
    void delete_comCobrancasPagas_lancaBusinessException() {
        Long id = 8L;
        Cliente entity = buildCliente(id, StatusCliente.INATIVO);

        Cobranca cobrancaPaga = Cobranca.builder()
                .id(1L)
                .status(StatusCobranca.PAGO)
                .valorEsperado(BigDecimal.TEN)
                .build();

        Contrato contratoEncerrado = Contrato.builder()
                .id(2L)
                .status(StatusContrato.ENCERRADO)
                .descricao("Contrato encerrado")
                .valor(BigDecimal.TEN)
                .cobrancas(new ArrayList<>(List.of(cobrancaPaga)))
                .build();
        entity.getContratos().add(contratoEncerrado);

        when(clienteRepository.findById(id)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> clienteService.delete(id))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("cobranças pagas");

        verify(clienteRepository, never()).delete(any(Cliente.class));
    }

    // ---------------------------------------------------------------------------
    // toggleStatus
    // ---------------------------------------------------------------------------

    @Test
    @DisplayName("toggleStatus muda status ATIVO para INATIVO")
    void toggleStatus_ativo_mudaParaInativo() {
        Long id = 3L;
        Cliente entity = buildCliente(id, StatusCliente.ATIVO);
        Cliente saved = buildCliente(id, StatusCliente.INATIVO);
        ClienteResponse response = buildResponse(id, StatusCliente.INATIVO);

        when(clienteRepository.findById(id)).thenReturn(Optional.of(entity));
        when(clienteRepository.save(entity)).thenReturn(saved);
        when(clienteMapper.toResponse(saved)).thenReturn(response);

        ClienteResponse result = clienteService.toggleStatus(id);

        assertThat(entity.getStatus()).isEqualTo(StatusCliente.INATIVO);
        assertThat(result.getStatus()).isEqualTo(StatusCliente.INATIVO);
        verify(clienteRepository).save(entity);
    }

    @Test
    @DisplayName("toggleStatus muda status INATIVO para ATIVO")
    void toggleStatus_inativo_mudaParaAtivo() {
        Long id = 4L;
        Cliente entity = buildCliente(id, StatusCliente.INATIVO);
        Cliente saved = buildCliente(id, StatusCliente.ATIVO);
        ClienteResponse response = buildResponse(id, StatusCliente.ATIVO);

        when(clienteRepository.findById(id)).thenReturn(Optional.of(entity));
        when(clienteRepository.save(entity)).thenReturn(saved);
        when(clienteMapper.toResponse(saved)).thenReturn(response);

        ClienteResponse result = clienteService.toggleStatus(id);

        assertThat(entity.getStatus()).isEqualTo(StatusCliente.ATIVO);
        assertThat(result.getStatus()).isEqualTo(StatusCliente.ATIVO);
    }

    // ---------------------------------------------------------------------------
    // findAll
    // ---------------------------------------------------------------------------

    @Test
    @DisplayName("findAll retorna página de ClienteResponse")
    void findAll_retornaPaginaDeRespostas() {
        Pageable pageable = PageRequest.of(0, 10);
        Cliente c1 = buildCliente(1L, StatusCliente.ATIVO);
        Cliente c2 = buildCliente(2L, StatusCliente.ATIVO);
        Page<Cliente> page = new PageImpl<>(List.of(c1, c2), pageable, 2);

        ClienteResponse r1 = buildResponse(1L, StatusCliente.ATIVO);
        ClienteResponse r2 = buildResponse(2L, StatusCliente.ATIVO);

        when(clienteRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(clienteMapper.toResponse(c1)).thenReturn(r1);
        when(clienteMapper.toResponse(c2)).thenReturn(r2);

        Page<ClienteResponse> result = clienteService.findAll(null, null, pageable);

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).containsExactly(r1, r2);
    }

    @Test
    @DisplayName("findAll com filtros retorna apenas clientes que correspondem")
    void findAll_comFiltros_retornaClientesFiltrados() {
        Pageable pageable = PageRequest.of(0, 5);
        Cliente c1 = buildCliente(1L, StatusCliente.ATIVO);
        Page<Cliente> page = new PageImpl<>(List.of(c1), pageable, 1);
        ClienteResponse r1 = buildResponse(1L, StatusCliente.ATIVO);

        when(clienteRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(clienteMapper.toResponse(c1)).thenReturn(r1);

        Page<ClienteResponse> result = clienteService.findAll("Empresa", StatusCliente.ATIVO, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getStatus()).isEqualTo(StatusCliente.ATIVO);
    }

    // ---------------------------------------------------------------------------
    // update — additional branches
    // ---------------------------------------------------------------------------

    @Test
    @DisplayName("update com CPF duplicado por outro cliente lança BusinessException")
    void update_cpfDuplicadoOutroCliente_lancaBusinessException() {
        Long id = 5L;
        ClienteRequest request = buildRequest(null, "123.456.789-00");

        Cliente entity = buildCliente(id, StatusCliente.ATIVO);
        entity.setContatos(new ArrayList<>());
        entity.setEnderecos(new ArrayList<>());

        when(clienteRepository.findById(id)).thenReturn(Optional.of(entity));
        when(clienteRepository.existsByCpfAndIdNot("123.456.789-00", id)).thenReturn(true);

        assertThatThrownBy(() -> clienteService.update(id, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("CPF já cadastrado");

        verify(clienteRepository, never()).save(any());
    }

    @Test
    @DisplayName("update com tipoPessoa nulo não altera tipoPessoa")
    void update_tipoPessoaNulo_naoAlteraTipoPessoa() {
        Long id = 5L;
        ClienteRequest request = ClienteRequest.builder()
                .tipoPessoa(null)
                .razaoSocial("Empresa Teste LTDA")
                .cnpj("12.345.678/0001-00")
                .build();

        Cliente entity = buildCliente(id, StatusCliente.ATIVO);
        entity.setContatos(new ArrayList<>());
        entity.setEnderecos(new ArrayList<>());
        TipoPessoa tipoPessoaOriginal = entity.getTipoPessoa();

        ClienteResponse response = buildResponse(id, StatusCliente.ATIVO);

        when(clienteRepository.findById(id)).thenReturn(Optional.of(entity));
        when(clienteRepository.existsByCnpjAndIdNot("12.345.678/0001-00", id)).thenReturn(false);
        when(clienteRepository.save(entity)).thenReturn(entity);
        when(clienteMapper.toResponse(entity)).thenReturn(response);

        clienteService.update(id, request);

        assertThat(entity.getTipoPessoa()).isEqualTo(tipoPessoaOriginal);
    }

    @Test
    @DisplayName("update com contatos e enderecos substitui as listas")
    void update_comContatosEEnderecos_substituiListas() {
        Long id = 5L;

        Contato contatoExistente = new Contato();
        contatoExistente.setNome("Antigo");

        Endereco enderecoExistente = new Endereco();

        Cliente entity = buildCliente(id, StatusCliente.ATIVO);
        entity.setContatos(new ArrayList<>(List.of(contatoExistente)));
        entity.setEnderecos(new ArrayList<>(List.of(enderecoExistente)));

        ContatoDTO novoContatoDTO = ContatoDTO.builder().nome("Novo").email("novo@empresa.com").build();
        EnderecoDTO novoEnderecoDTO = EnderecoDTO.builder().cidade("Rio de Janeiro").build();

        ClienteRequest request = ClienteRequest.builder()
                .tipoPessoa("JURIDICA")
                .razaoSocial("Empresa Teste LTDA")
                .cnpj("12.345.678/0001-00")
                .contatos(List.of(novoContatoDTO))
                .enderecos(List.of(novoEnderecoDTO))
                .build();

        Contato novoContato = new Contato();
        novoContato.setNome("Novo");

        Endereco novoEndereco = new Endereco();

        ClienteResponse response = buildResponse(id, StatusCliente.ATIVO);

        when(clienteRepository.findById(id)).thenReturn(Optional.of(entity));
        when(clienteRepository.existsByCnpjAndIdNot("12.345.678/0001-00", id)).thenReturn(false);
        when(clienteMapper.toContatoEntity(novoContatoDTO)).thenReturn(novoContato);
        when(clienteMapper.toEnderecoEntity(novoEnderecoDTO)).thenReturn(novoEndereco);
        when(clienteRepository.save(entity)).thenReturn(entity);
        when(clienteMapper.toResponse(entity)).thenReturn(response);

        clienteService.update(id, request);

        assertThat(entity.getContatos()).hasSize(1);
        assertThat(entity.getContatos().get(0).getNome()).isEqualTo("Novo");
        assertThat(entity.getEnderecos()).hasSize(1);
    }

    @Test
    @DisplayName("update com contatos e enderecos nulos limpa as listas")
    void update_comContatosEEnderecoNulos_mantemListasVazias() {
        Long id = 5L;

        Cliente entity = buildCliente(id, StatusCliente.ATIVO);
        entity.setContatos(new ArrayList<>());
        entity.setEnderecos(new ArrayList<>());

        ClienteRequest request = ClienteRequest.builder()
                .tipoPessoa("JURIDICA")
                .razaoSocial("Empresa Teste LTDA")
                .cnpj("12.345.678/0001-00")
                .contatos(null)
                .enderecos(null)
                .build();

        ClienteResponse response = buildResponse(id, StatusCliente.ATIVO);

        when(clienteRepository.findById(id)).thenReturn(Optional.of(entity));
        when(clienteRepository.existsByCnpjAndIdNot("12.345.678/0001-00", id)).thenReturn(false);
        when(clienteRepository.save(entity)).thenReturn(entity);
        when(clienteMapper.toResponse(entity)).thenReturn(response);

        clienteService.update(id, request);

        assertThat(entity.getContatos()).isEmpty();
        assertThat(entity.getEnderecos()).isEmpty();
    }

    @Test
    @DisplayName("update com id inexistente lança ResourceNotFoundException")
    void update_idInexistente_lancaResourceNotFoundException() {
        when(clienteRepository.findById(999L)).thenReturn(Optional.empty());

        ClienteRequest request = buildRequest("12.345.678/0001-00", null);

        assertThatThrownBy(() -> clienteService.update(999L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");

        verify(clienteRepository, never()).save(any());
    }

    // ---------------------------------------------------------------------------
    // delete — not found
    // ---------------------------------------------------------------------------

    @Test
    @DisplayName("delete com id inexistente lança ResourceNotFoundException")
    void delete_idInexistente_lancaResourceNotFoundException() {
        when(clienteRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clienteService.delete(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");

        verify(clienteRepository, never()).delete(any(Cliente.class));
    }

    // ---------------------------------------------------------------------------
    // toggleStatus — not found
    // ---------------------------------------------------------------------------

    @Test
    @DisplayName("toggleStatus com id inexistente lança ResourceNotFoundException")
    void toggleStatus_idInexistente_lancaResourceNotFoundException() {
        when(clienteRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clienteService.toggleStatus(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    // ---------------------------------------------------------------------------
    // findAll — sem resultados
    // ---------------------------------------------------------------------------

    @Test
    @DisplayName("findAll sem resultados retorna página vazia")
    void findAll_semResultados_retornaPaginaVazia() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Cliente> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(clienteRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(emptyPage);

        Page<ClienteResponse> result = clienteService.findAll(null, null, pageable);

        assertThat(result.getTotalElements()).isEqualTo(0);
        assertThat(result.getContent()).isEmpty();
    }

    // ---------------------------------------------------------------------------
    // Specification lambda coverage
    // ---------------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private Specification<Cliente> captureSpec(String search, StatusCliente status) {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Cliente> page = new PageImpl<>(List.of());
        ArgumentCaptor<Specification<Cliente>> captor = ArgumentCaptor.forClass(Specification.class);
        when(clienteRepository.findAll(captor.capture(), eq(pageable))).thenReturn(page);
        clienteService.findAll(search, status, pageable);
        return captor.getValue();
    }

    @SuppressWarnings("unchecked")
    private void invokeSpec(Specification<Cliente> spec) {
        Root<Cliente> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Path<Object> razaoSocialPath = mock(Path.class);
        Path<Object> nomeFantasiaPath = mock(Path.class);
        Path<Object> cnpjPath = mock(Path.class);
        Path<Object> cpfPath = mock(Path.class);
        Path<Object> statusPath = mock(Path.class);
        Expression<String> lowerExpr = mock(Expression.class);

        lenient().when(root.get("razaoSocial")).thenReturn(razaoSocialPath);
        lenient().when(root.get("nomeFantasia")).thenReturn(nomeFantasiaPath);
        lenient().when(root.get("cnpj")).thenReturn(cnpjPath);
        lenient().when(root.get("cpf")).thenReturn(cpfPath);
        lenient().when(root.get("status")).thenReturn(statusPath);
        lenient().when(cb.lower(any())).thenReturn(lowerExpr);
        lenient().when(cb.like(any(Expression.class), anyString())).thenReturn(mock(Predicate.class));
        lenient().when(cb.or(any(Predicate[].class))).thenReturn(mock(Predicate.class));
        lenient().when(cb.equal(any(), any())).thenReturn(mock(Predicate.class));
        lenient().when(cb.and(any(Predicate[].class))).thenReturn(mock(Predicate.class));

        spec.toPredicate(root, query, cb);
    }

    @Test
    @DisplayName("specificationLambda sem filtros executa sem erro")
    void specificationLambda_semFiltros_executa() {
        invokeSpec(captureSpec(null, null));
    }

    @Test
    @DisplayName("specificationLambda com search executa predicado de busca")
    void specificationLambda_comSearch_executa() {
        invokeSpec(captureSpec("empresa", null));
    }

    @Test
    @DisplayName("specificationLambda com status executa predicado de status")
    void specificationLambda_comStatus_executa() {
        invokeSpec(captureSpec(null, StatusCliente.ATIVO));
    }

    @Test
    @DisplayName("specificationLambda com search e status executa ambos predicados")
    void specificationLambda_comSearchEStatus_executa() {
        invokeSpec(captureSpec("teste", StatusCliente.INATIVO));
    }
}
