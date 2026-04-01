package com.prediman.crm.service;

import com.prediman.crm.dto.UsuarioRequest;
import com.prediman.crm.dto.UsuarioResponse;
import com.prediman.crm.dto.UsuarioUpdateRequest;
import com.prediman.crm.exception.BusinessException;
import com.prediman.crm.exception.ResourceNotFoundException;
import com.prediman.crm.model.Usuario;
import com.prediman.crm.model.enums.Perfil;
import com.prediman.crm.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioMapper usuarioMapper;

    @Transactional(readOnly = true)
    public List<UsuarioResponse> findAll() {
        return usuarioRepository.findAll()
                .stream()
                .map(usuarioMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<UsuarioResponse> findAll(Pageable pageable) {
        return usuarioRepository.findAll(pageable).map(usuarioMapper::toResponse);
    }

    @Transactional
    public UsuarioResponse create(UsuarioRequest request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("E-mail já cadastrado: " + request.getEmail());
        }

        Usuario usuario = Usuario.builder()
                .nome(request.getNome())
                .email(request.getEmail())
                .senhaHash(passwordEncoder.encode(request.getSenha()))
                .perfil(request.getPerfil())
                .ativo(true)
                .build();

        Usuario saved = usuarioRepository.save(usuario);
        log.info("Usuário criado com id: {}", saved.getId());
        return usuarioMapper.toResponse(saved);
    }

    @Transactional
    public UsuarioResponse update(Long id, UsuarioUpdateRequest request) {
        Usuario usuario = findUsuarioById(id);

        if (!usuario.getEmail().equals(request.getEmail())
                && usuarioRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("E-mail já cadastrado: " + request.getEmail());
        }

        if (usuario.getPerfil() == Perfil.ADMIN && request.getPerfil() != Perfil.ADMIN) {
            verificarUltimoAdmin(usuario.getId());
        }

        usuario.setNome(request.getNome());
        usuario.setEmail(request.getEmail());
        usuario.setPerfil(request.getPerfil());

        Usuario saved = usuarioRepository.save(usuario);
        log.info("Usuário atualizado com id: {}", saved.getId());
        return usuarioMapper.toResponse(saved);
    }

    @Transactional
    public UsuarioResponse toggleStatus(Long id) {
        Usuario usuario = findUsuarioById(id);

        if (usuario.getAtivo() && usuario.getPerfil() == Perfil.ADMIN) {
            verificarUltimoAdmin(usuario.getId());
        }

        usuario.setAtivo(!usuario.getAtivo());
        Usuario saved = usuarioRepository.save(usuario);
        log.info("Status do usuário {} alterado para ativo={}", id, saved.getAtivo());
        return usuarioMapper.toResponse(saved);
    }

    private void verificarUltimoAdmin(Long idUsuario) {
        long adminsAtivos = usuarioRepository.countByPerfilAndAtivoTrue(Perfil.ADMIN);
        if (adminsAtivos <= 1) {
            throw new BusinessException("Não é possível desativar ou rebaixar o único administrador ativo do sistema.");
        }
    }

    private Usuario findUsuarioById(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", id));
    }

}
