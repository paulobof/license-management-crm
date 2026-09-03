package com.prediman.crm.service;

import com.prediman.crm.dto.CobrancaResponse;
import com.prediman.crm.model.Cobranca;
import org.springframework.stereotype.Component;

@Component
public class CobrancaMapper {

    private static final String DRIVE_FILE_URL = "https://drive.google.com/file/d/%s/view";

    public CobrancaResponse toResponse(Cobranca cobranca) {
        return CobrancaResponse.builder()
                .id(cobranca.getId())
                .valorEsperado(cobranca.getValorEsperado())
                .valorRecebido(cobranca.getValorRecebido())
                .dataVencimento(cobranca.getDataVencimento())
                .dataPagamento(cobranca.getDataPagamento())
                .formaPagamento(cobranca.getFormaPagamento())
                .comprovanteDriveId(cobranca.getComprovanteDriveId())
                .comprovanteUrl(buildComprovanteUrl(cobranca.getComprovanteDriveId()))
                .status(cobranca.getStatus())
                .statusCalculado(cobranca.getStatusCalculado())
                .contratoId(cobranca.getContrato().getId())
                .createdAt(cobranca.getCreatedAt())
                .updatedAt(cobranca.getUpdatedAt())
                .build();
    }

    /**
     * Deriva a URL de visualização do comprovante a partir do id do arquivo no Google Drive.
     * Retorna null quando não há comprovante associado.
     */
    private String buildComprovanteUrl(String comprovanteDriveId) {
        if (comprovanteDriveId == null || comprovanteDriveId.isBlank()) {
            return null;
        }
        return String.format(DRIVE_FILE_URL, comprovanteDriveId);
    }
}
