package com.prediman.crm.dto;

import com.prediman.crm.model.enums.StatusCliente;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClienteResponse {

    private Long id;
    private String razaoSocial;
    private String nomeFantasia;
    private String cnpj;
    private String ie;
    private String segmento;
    private LocalDate dataFundacao;
    private LocalDate dataInicioCliente;
    private StatusCliente status;
    private String googleDriveFolderId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ContatoDTO> contatos;
    private List<EnderecoDTO> enderecos;
}
