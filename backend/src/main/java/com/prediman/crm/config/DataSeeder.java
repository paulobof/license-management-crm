package com.prediman.crm.config;

import com.prediman.crm.model.Usuario;
import com.prediman.crm.model.enums.Perfil;
import com.prediman.crm.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.initial-password:}")
    private String adminInitialPassword;

    @Override
    public void run(String... args) {
        long adminsAtivos = usuarioRepository.countByPerfilAndAtivoTrue(Perfil.ADMIN);

        if (adminsAtivos > 0) {
            log.info("Sistema possui {} administrador(es) ativo(s)", adminsAtivos);
            return;
        }

        if (adminInitialPassword == null || adminInitialPassword.isBlank()) {
            log.error("Nenhum admin ativo e ADMIN_INITIAL_PASSWORD nao definida. Defina a variavel de ambiente para criar o admin inicial.");
            return;
        }

        log.warn("Nenhum administrador ativo encontrado! Criando admin padrao...");
        String adminEmail = "admin@prediman.com.br";

        usuarioRepository.findByEmail(adminEmail).ifPresentOrElse(admin -> {
            admin.setAtivo(true);
            admin.setPerfil(Perfil.ADMIN);
            admin.setSenhaHash(passwordEncoder.encode(adminInitialPassword));
            usuarioRepository.save(admin);
            log.warn("Administrador padrao reativado: {}", adminEmail);
        }, () -> {
            Usuario admin = Usuario.builder()
                    .nome("Administrador")
                    .email(adminEmail)
                    .senhaHash(passwordEncoder.encode(adminInitialPassword))
                    .perfil(Perfil.ADMIN)
                    .ativo(true)
                    .build();
            usuarioRepository.save(admin);
            log.warn("Administrador padrao criado: {}", adminEmail);
        });
    }
}
