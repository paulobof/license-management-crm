package com.prediman.crm.dto;

import com.prediman.crm.model.enums.CategoriaDocumento;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentoRequest {

    @NotBlank(message = "Nome do documento é obrigatório")
    private String nome;

    private CategoriaDocumento categoria;

    private LocalDate dataEmissao;

    private LocalDate dataValidade;

    private String revisao;

    private String observacoes;

    @NotNull(message = "Cliente é obrigatório")
    private Long clienteId;

    private String googleDriveFileId;

    private String googleDriveUrl;

    private Long tamanhoBytes;

    private String mimeType;
}
