package com.prediman.crm.service;

import com.prediman.crm.dto.ForgotPasswordRequest;
import com.prediman.crm.dto.LoginRequest;
import com.prediman.crm.dto.LoginResponse;
import com.prediman.crm.dto.RefreshRequest;
import com.prediman.crm.dto.ResetPasswordRequest;
import com.prediman.crm.exception.BusinessException;
import com.prediman.crm.exception.ResourceNotFoundException;
import com.prediman.crm.model.PasswordResetToken;
import com.prediman.crm.model.Usuario;
import com.prediman.crm.repository.PasswordResetTokenRepository;
import com.prediman.crm.repository.UsuarioRepository;
import com.prediman.crm.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    /** Validade do link de redefinicao de senha, conforme o PRD. */
    public static final long RESET_TOKEN_HORAS_VALIDADE = 2;

    private static final int RESET_TOKEN_BYTES = 32;

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UsuarioRepository usuarioRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.base-url:http://localhost:5173}")
    private String baseUrl;

    @Transactional
    public LoginResponse authenticate(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        String email = authentication.getName();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        usuario.setUltimoLogin(LocalDateTime.now());
        usuarioRepository.save(usuario);

        String role = usuario.getPerfil().name();
        String token = jwtTokenProvider.generateToken(email, role);
        String refreshToken = jwtTokenProvider.generateRefreshToken(email, role);

        return LoginResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .nome(usuario.getNome())
                .perfil(role)
                .build();
    }

    public LoginResponse refresh(RefreshRequest request) {
        String refreshToken = request.getRefreshToken();

        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("Refresh token inválido ou expirado");
        }

        if (!jwtTokenProvider.isRefreshToken(refreshToken)) {
            throw new IllegalArgumentException("Token fornecido nao e um refresh token");
        }

        String email = jwtTokenProvider.getEmailFromToken(refreshToken);
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        if (!usuario.getAtivo()) {
            throw new IllegalArgumentException("Usuário inativo");
        }

        String role = usuario.getPerfil().name();
        String newToken = jwtTokenProvider.generateToken(email, role);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(email, role);

        return LoginResponse.builder()
                .token(newToken)
                .refreshToken(newRefreshToken)
                .nome(usuario.getNome())
                .perfil(role)
                .build();
    }

    // -------------------------------------------------------------------------
    // Recuperação de senha
    // -------------------------------------------------------------------------

    /**
     * Inicia o fluxo de recuperação de senha.
     *
     * <p>Este método nunca sinaliza se o e-mail existe ou não: o controller sempre
     * devolve a mesma mensagem genérica, evitando enumeração de contas.</p>
     */
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        String email = request.getEmail() == null ? "" : request.getEmail().trim();
        Optional<Usuario> encontrado = usuarioRepository.findByEmail(email);

        if (encontrado.isEmpty()) {
            log.info("Solicitação de recuperação de senha para e-mail não cadastrado; nenhuma ação realizada");
            return;
        }

        Usuario usuario = encontrado.get();
        if (!Boolean.TRUE.equals(usuario.getAtivo())) {
            log.info("Solicitação de recuperação de senha para usuário inativo id={}; nenhuma ação realizada",
                    usuario.getId());
            return;
        }

        LocalDateTime agora = LocalDateTime.now();
        passwordResetTokenRepository.invalidarTokensPendentes(usuario.getId(), agora);

        String tokenPuro = gerarTokenAleatorio();
        PasswordResetToken token = PasswordResetToken.builder()
                .usuario(usuario)
                .tokenHash(hashToken(tokenPuro))
                .expiresAt(agora.plusHours(RESET_TOKEN_HORAS_VALIDADE))
                .createdAt(agora)
                .build();
        passwordResetTokenRepository.save(token);

        String link = montarLinkRedefinicao(tokenPuro);
        boolean enviado = emailService.enviar(
                usuario.getEmail(),
                "Prediman CRM - Redefinição de senha",
                montarMensagemRecuperacao(usuario.getNome(), link));

        if (enviado) {
            log.info("E-mail de redefinição de senha enviado para o usuário id={}", usuario.getId());
        } else {
            log.warn("Não foi possível enviar o e-mail de redefinição de senha para o usuário id={}",
                    usuario.getId());
        }
    }

    /**
     * Conclui a redefinição de senha a partir de um token existente, não usado e não expirado.
     */
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken token = passwordResetTokenRepository
                .findByTokenHash(hashToken(request.getToken()))
                .orElseThrow(() -> new BusinessException(
                        "Link de redefinição inválido. Solicite uma nova recuperação de senha."));

        if (token.getUsedAt() != null) {
            throw new BusinessException(
                    "Este link de redefinição já foi utilizado. Solicite uma nova recuperação de senha.");
        }

        LocalDateTime agora = LocalDateTime.now();
        if (token.getExpiresAt() == null || !token.getExpiresAt().isAfter(agora)) {
            throw new BusinessException(
                    "Link de redefinição expirado. Solicite uma nova recuperação de senha.");
        }

        Usuario usuario = token.getUsuario();
        if (usuario == null || !Boolean.TRUE.equals(usuario.getAtivo())) {
            throw new BusinessException("Usuário inativo. Entre em contato com o administrador do sistema.");
        }

        usuario.setSenhaHash(passwordEncoder.encode(request.getNovaSenha()));
        usuarioRepository.save(usuario);

        token.setUsedAt(agora);
        passwordResetTokenRepository.save(token);

        log.info("Senha redefinida com sucesso para o usuário id={}", usuario.getId());
    }

    /**
     * Gera um token opaco de 256 bits usando {@link SecureRandom} (não previsível).
     */
    private String gerarTokenAleatorio() {
        byte[] bytes = new byte[RESET_TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * Calcula o SHA-256 do token; apenas o hash é persistido no banco.
     */
    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Algoritmo SHA-256 indisponível na JVM", e);
        }
    }

    private String montarLinkRedefinicao(String tokenPuro) {
        String base = baseUrl == null ? "" : baseUrl.trim();
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        return base + "/redefinir-senha?token=" + tokenPuro;
    }

    private String montarMensagemRecuperacao(String nome, String link) {
        return "Olá, " + nome + "!\n\n"
                + "Recebemos uma solicitação para redefinir a senha da sua conta no Prediman CRM.\n\n"
                + "Para cadastrar uma nova senha, acesse o link abaixo:\n"
                + link + "\n\n"
                + "Este link é válido por " + RESET_TOKEN_HORAS_VALIDADE
                + " horas e pode ser utilizado apenas uma vez.\n\n"
                + "Se você não solicitou a redefinição, ignore este e-mail: sua senha atual continua valendo.\n\n"
                + "Prediman Engenharia";
    }
}
