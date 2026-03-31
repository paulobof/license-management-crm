package com.prediman.crm.config;

import com.prediman.crm.model.Usuario;
import com.prediman.crm.model.enums.Perfil;
import com.prediman.crm.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        long adminsAtivos = usuarioRepository.countByPerfilAndAtivoTrue(Perfil.ADMIN);

        if (adminsAtivos > 0) {
            log.info("Sistema possui {} administrador(es) ativo(s)", adminsAtivos);
            return;
        }

        log.warn("Nenhum administrador ativo encontrado! Verificando admin padrão...");
        String adminEmail = "admin@prediman.com.br";

        usuarioRepository.findByEmail(adminEmail).ifPresentOrElse(admin -> {
            admin.setAtivo(true);
            admin.setPerfil(Perfil.ADMIN);
            usuarioRepository.save(admin);
            log.warn("Administrador padrão reativado: {}", adminEmail);
        }, () -> {
            Usuario admin = Usuario.builder()
                    .nome("Administrador")
                    .email(adminEmail)
                    .senhaHash(passwordEncoder.encode("admin123"))
                    .perfil(Perfil.ADMIN)
                    .ativo(true)
                    .build();
            usuarioRepository.save(admin);
            log.warn("Administrador padrão criado: {}", adminEmail);
        });
    }
}
