package com.prediman.crm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${mail.from:alertas@prediman.com.br}")
    private String fromAddress;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    public boolean enviar(String destinatario, String assunto, String mensagem) {
        if (destinatario == null || destinatario.isBlank()) {
            log.warn("Destinatário de e-mail vazio; envio ignorado");
            return false;
        }
        if (mailUsername == null || mailUsername.isBlank()) {
            log.warn("MAIL_USERNAME não configurado; envio de e-mail desabilitado");
            return false;
        }

        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setFrom(fromAddress);
            mail.setTo(destinatario);
            mail.setSubject(assunto);
            mail.setText(mensagem);
            mailSender.send(mail);
            log.info("E-mail enviado para {}: {}", destinatario, assunto);
            return true;
        } catch (MailException e) {
            log.error("Falha ao enviar e-mail para {}: {}", destinatario, e.getMessage());
            return false;
        }
    }
}
