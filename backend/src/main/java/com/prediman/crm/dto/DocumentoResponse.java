package com.prediman.crm.dto;

import com.prediman.crm.model.enums.CategoriaDocumento;
import com.prediman.crm.model.enums.StatusDocumento;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentoResponse {

    private Long id;
    private String nome;
    private CategoriaDocumento categoria;
    private LocalDate dataEmissao;
    private LocalDate dataValidade;
    private String revisao;
    private String observacoes;
    private String googleDriveFileId;
    private String googleDriveUrl;
    private Long tamanhoBytes;
    private String mimeType;
    private StatusDocumento statusCalculado;
    private Long clienteId;
    private String clienteNome;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
