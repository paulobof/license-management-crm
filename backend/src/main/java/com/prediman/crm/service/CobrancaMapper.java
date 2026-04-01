package com.prediman.crm.service;

import com.prediman.crm.dto.CobrancaResponse;
import com.prediman.crm.model.Cobranca;
import org.springframework.stereotype.Component;

@Component
public class CobrancaMapper {

    public CobrancaResponse toResponse(Cobranca cobranca) {
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
