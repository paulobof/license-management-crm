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
    private final EmailService emailService;

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

        enviarEmailBoasVindas(saved, request.getSenha());

        return usuarioMapper.toResponse(saved);
    }

    /**
     * Envia ao novo usuário as credenciais iniciais de acesso.
     *
     * <p>Falha no envio nunca aborta a criação do usuário: o erro é apenas registrado
     * em log (sem expor a senha).</p>
     */
    private void enviarEmailBoasVindas(Usuario usuario, String senhaInicial) {
        try {
            boolean enviado = emailService.enviar(
                    usuario.getEmail(),
                    "Prediman CRM - Bem-vindo(a) ao sistema",
                    montarMensagemBoasVindas(usuario, senhaInicial));

            if (!enviado) {
                log.warn("E-mail de boas-vindas não enviado para o usuário id={}", usuario.getId());
            }
        } catch (RuntimeException e) {
            log.warn("Falha ao enviar e-mail de boas-vindas para o usuário id={}: {}",
                    usuario.getId(), e.getMessage());
        }
    }

    private String montarMensagemBoasVindas(Usuario usuario, String senhaInicial) {
        return "Olá, " + usuario.getNome() + "!\n\n"
                + "Sua conta no Prediman CRM foi criada com sucesso.\n\n"
                + "Dados de acesso:\n"
                + "E-mail: " + usuario.getEmail() + "\n"
                + "Senha inicial: " + senhaInicial + "\n\n"
                + "Por segurança, altere esta senha no seu primeiro acesso utilizando a opção "
                + "\"Esqueci minha senha\" na tela de login.\n\n"
                + "Nunca compartilhe suas credenciais com terceiros.\n\n"
                + "Prediman Engenharia";
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
