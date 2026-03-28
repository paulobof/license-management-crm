package com.prediman.crm.dto;

import com.prediman.crm.model.enums.Perfil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioResponse {

    private Long id;
    private String nome;
    private String email;
    private Perfil perfil;
    private Boolean ativo;
    private LocalDateTime createdAt;
    private LocalDateTime ultimoLogin;
}
