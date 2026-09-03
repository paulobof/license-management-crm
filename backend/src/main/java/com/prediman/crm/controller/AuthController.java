package com.prediman.crm.controller;

import com.prediman.crm.dto.ForgotPasswordRequest;
import com.prediman.crm.dto.LoginRequest;
import com.prediman.crm.dto.LoginResponse;
import com.prediman.crm.dto.PasswordResetResponse;
import com.prediman.crm.dto.RefreshRequest;
import com.prediman.crm.dto.ResetPasswordRequest;
import com.prediman.crm.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    /**
     * Mensagem devolvida sempre pelo forgot-password, exista ou não o e-mail informado.
     * Evita enumeração de contas cadastradas.
     */
    static final String MENSAGEM_FORGOT_PASSWORD =
            "Se o e-mail informado estiver cadastrado, enviaremos as instruções para redefinição de senha.";

    static final String MENSAGEM_RESET_PASSWORD =
            "Senha redefinida com sucesso. Faça login com a nova senha.";

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.authenticate(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        LoginResponse response = authService.refresh(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<PasswordResetResponse> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok(new PasswordResetResponse(MENSAGEM_FORGOT_PASSWORD));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<PasswordResetResponse> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(new PasswordResetResponse(MENSAGEM_RESET_PASSWORD));
    }
}
