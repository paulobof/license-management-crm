package com.prediman.crm.service;

import com.prediman.crm.dto.UsuarioResponse;
import com.prediman.crm.model.Usuario;
import org.springframework.stereotype.Component;

@Component
public class UsuarioMapper {

    public UsuarioResponse toResponse(Usuario usuario) {
        return UsuarioResponse.builder()
                .id(usuario.getId())
                .nome(usuario.getNome())
                .email(usuario.getEmail())
                .perfil(usuario.getPerfil())
                .ativo(usuario.getAtivo())
                .createdAt(usuario.getCreatedAt())
                .ultimoLogin(usuario.getUltimoLogin())
                .build();
    }
}
