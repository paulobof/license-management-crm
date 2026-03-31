package com.prediman.crm.service;

import com.prediman.crm.dto.LoginRequest;
import com.prediman.crm.dto.LoginResponse;
import com.prediman.crm.dto.RefreshRequest;
import com.prediman.crm.exception.ResourceNotFoundException;
import com.prediman.crm.model.Usuario;
import com.prediman.crm.repository.UsuarioRepository;
import com.prediman.crm.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UsuarioRepository usuarioRepository;

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
}
