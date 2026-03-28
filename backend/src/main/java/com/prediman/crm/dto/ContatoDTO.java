package com.prediman.crm.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContatoDTO {

    private Long id;

    @NotBlank(message = "Nome do contato é obrigatório")
    private String nome;

    private String cargo;
    private String email;
    private String telefone;
    private String whatsapp;
    private Boolean principal;
}
