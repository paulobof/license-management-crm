package com.prediman.crm.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClienteRequest {

    @NotBlank(message = "Razão social é obrigatória")
    private String razaoSocial;

    private String nomeFantasia;
    private String cnpj;
    private String ie;
    private String segmento;
    private LocalDate dataFundacao;
    private LocalDate dataInicioCliente;
    private String googleDriveFolderId;

    @Valid
    @Builder.Default
    private List<ContatoDTO> contatos = new ArrayList<>();

    @Valid
    @Builder.Default
    private List<EnderecoDTO> enderecos = new ArrayList<>();
}
