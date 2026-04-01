package com.prediman.crm.service;

import com.prediman.crm.dto.DocumentoResponse;
import com.prediman.crm.model.Documento;
import org.springframework.stereotype.Component;

@Component
public class DocumentoMapper {

    public DocumentoResponse toResponse(Documento documento) {
        return DocumentoResponse.builder()
                .id(documento.getId())
                .nome(documento.getNome())
                .categoria(documento.getCategoria())
                .dataEmissao(documento.getDataEmissao())
                .dataValidade(documento.getDataValidade())
                .revisao(documento.getRevisao())
                .observacoes(documento.getObservacoes())
                .googleDriveFileId(documento.getGoogleDriveFileId())
                .googleDriveUrl(documento.getGoogleDriveUrl())
                .tamanhoBytes(documento.getTamanhoBytes())
                .mimeType(documento.getMimeType())
                .statusCalculado(documento.getStatusCalculado())
                .clienteId(documento.getCliente().getId())
                .clienteNome(documento.getCliente().getRazaoSocial())
                .createdAt(documento.getCreatedAt())
                .updatedAt(documento.getUpdatedAt())
                .build();
    }
}
