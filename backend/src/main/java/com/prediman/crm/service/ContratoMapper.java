package com.prediman.crm.service;

import com.prediman.crm.dto.ContratoResponse;
import com.prediman.crm.model.Contrato;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class ContratoMapper {

    private final CobrancaMapper cobrancaMapper;

    public ContratoMapper(CobrancaMapper cobrancaMapper) {
        this.cobrancaMapper = cobrancaMapper;
    }

    public ContratoResponse toResponse(Contrato contrato) {
        return ContratoResponse.builder()
                .id(contrato.getId())
                .descricao(contrato.getDescricao())
                .valor(contrato.getValor())
                .periodicidade(contrato.getPeriodicidade())
                .dataInicio(contrato.getDataInicio())
                .dataFim(contrato.getDataFim())
                .status(contrato.getStatus())
                .observacoes(contrato.getObservacoes())
                .clienteId(contrato.getCliente().getId())
                .clienteNome(contrato.getCliente().getRazaoSocial())
                .cobrancas(contrato.getCobrancas().stream()
                        .map(cobrancaMapper::toResponse)
                        .collect(Collectors.toList()))
                .createdAt(contrato.getCreatedAt())
                .updatedAt(contrato.getUpdatedAt())
                .build();
    }
}
