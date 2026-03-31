package com.prediman.crm.service;

import com.prediman.crm.dto.ClienteRequest;
import com.prediman.crm.dto.ClienteResponse;
import com.prediman.crm.dto.ContatoDTO;
import com.prediman.crm.dto.EnderecoDTO;
import com.prediman.crm.model.Cliente;
import com.prediman.crm.model.Contato;
import com.prediman.crm.model.Endereco;
import com.prediman.crm.model.enums.TipoEndereco;
import com.prediman.crm.model.enums.TipoPessoa;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ClienteMapper {

    public Cliente toEntity(ClienteRequest request) {
        Cliente cliente = Cliente.builder()
                .tipoPessoa(StringUtils.hasText(request.getTipoPessoa())
                        ? TipoPessoa.valueOf(request.getTipoPessoa()) : TipoPessoa.JURIDICA)
                .razaoSocial(request.getRazaoSocial())
                .nomeFantasia(request.getNomeFantasia())
                .cnpj(request.getCnpj())
                .cpf(request.getCpf())
                .ie(request.getIe())
                .segmento(request.getSegmento())
                .dataFundacao(request.getDataFundacao())
                .dataInicioCliente(request.getDataInicioCliente())
                .googleDriveFolderId(request.getGoogleDriveFolderId())
                .build();

        if (request.getContatos() != null) {
            request.getContatos().forEach(dto -> {
                Contato contato = toContatoEntity(dto);
                contato.setCliente(cliente);
                cliente.getContatos().add(contato);
            });
        }

        if (request.getEnderecos() != null) {
            request.getEnderecos().forEach(dto -> {
                Endereco endereco = toEnderecoEntity(dto);
                endereco.setCliente(cliente);
                cliente.getEnderecos().add(endereco);
            });
        }

        return cliente;
    }

    public ClienteResponse toResponse(Cliente cliente) {
        return ClienteResponse.builder()
                .id(cliente.getId())
                .tipoPessoa(cliente.getTipoPessoa())
                .razaoSocial(cliente.getRazaoSocial())
                .nomeFantasia(cliente.getNomeFantasia())
                .cnpj(cliente.getCnpj())
                .cpf(cliente.getCpf())
                .ie(cliente.getIe())
                .segmento(cliente.getSegmento())
                .dataFundacao(cliente.getDataFundacao())
                .dataInicioCliente(cliente.getDataInicioCliente())
                .status(cliente.getStatus())
                .googleDriveFolderId(cliente.getGoogleDriveFolderId())
                .createdAt(cliente.getCreatedAt())
                .updatedAt(cliente.getUpdatedAt())
                .contatos(toContatoDTOList(cliente.getContatos()))
                .enderecos(toEnderecoDTOList(cliente.getEnderecos()))
                .build();
    }

    public Contato toContatoEntity(ContatoDTO dto) {
        return Contato.builder()
                .nome(dto.getNome())
                .cargo(dto.getCargo())
                .email(dto.getEmail())
                .telefone(dto.getTelefone())
                .whatsapp(dto.getWhatsapp())
                .principal(dto.getPrincipal() != null ? dto.getPrincipal() : false)
                .build();
    }

    public Endereco toEnderecoEntity(EnderecoDTO dto) {
        return Endereco.builder()
                .tipo(dto.getTipo() != null ? dto.getTipo() : TipoEndereco.COBRANCA)
                .cep(dto.getCep())
                .logradouro(dto.getLogradouro())
                .numero(dto.getNumero())
                .complemento(dto.getComplemento())
                .bairro(dto.getBairro())
                .cidade(dto.getCidade())
                .estado(dto.getEstado())
                .build();
    }

    public ContatoDTO toContatoDTO(Contato contato) {
        return ContatoDTO.builder()
                .id(contato.getId())
                .nome(contato.getNome())
                .cargo(contato.getCargo())
                .email(contato.getEmail())
                .telefone(contato.getTelefone())
                .whatsapp(contato.getWhatsapp())
                .principal(contato.getPrincipal())
                .build();
    }

    public EnderecoDTO toEnderecoDTO(Endereco endereco) {
        return EnderecoDTO.builder()
                .id(endereco.getId())
                .tipo(endereco.getTipo())
                .cep(endereco.getCep())
                .logradouro(endereco.getLogradouro())
                .numero(endereco.getNumero())
                .complemento(endereco.getComplemento())
                .bairro(endereco.getBairro())
                .cidade(endereco.getCidade())
                .estado(endereco.getEstado())
                .build();
    }

    private List<ContatoDTO> toContatoDTOList(List<Contato> contatos) {
        if (contatos == null) return List.of();
        return contatos.stream().map(this::toContatoDTO).collect(Collectors.toList());
    }

    private List<EnderecoDTO> toEnderecoDTOList(List<Endereco> enderecos) {
        if (enderecos == null) return List.of();
        return enderecos.stream().map(this::toEnderecoDTO).collect(Collectors.toList());
    }
}
