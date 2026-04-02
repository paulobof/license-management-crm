package com.prediman.crm.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmailService — testes de unidade")
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @Test
    @DisplayName("enviar com sucesso retorna true")
    void enviar_sucesso() {
        ReflectionTestUtils.setField(emailService, "fromAddress", "alertas@prediman.com.br");
        ReflectionTestUtils.setField(emailService, "mailUsername", "user@gmail.com");

        boolean result = emailService.enviar("dest@email.com", "Assunto", "Corpo");

        assertTrue(result);
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        assertEquals("alertas@prediman.com.br", captor.getValue().getFrom());
        assertEquals("dest@email.com", captor.getValue().getTo()[0]);
        assertEquals("Assunto", captor.getValue().getSubject());
        assertEquals("Corpo", captor.getValue().getText());
    }

    @Test
    @DisplayName("enviar com falha retorna false")
    void enviar_falha() {
        ReflectionTestUtils.setField(emailService, "fromAddress", "alertas@prediman.com.br");
        ReflectionTestUtils.setField(emailService, "mailUsername", "user@gmail.com");
        doThrow(new MailSendException("SMTP error")).when(mailSender).send(any(SimpleMailMessage.class));

        boolean result = emailService.enviar("dest@email.com", "Assunto", "Corpo");

        assertFalse(result);
    }

    @Test
    @DisplayName("enviar com destinatario vazio retorna false")
    void enviar_destinatarioVazio() {
        assertFalse(emailService.enviar(null, "Assunto", "Corpo"));
        assertFalse(emailService.enviar("", "Assunto", "Corpo"));
        assertFalse(emailService.enviar("  ", "Assunto", "Corpo"));
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("enviar sem MAIL_USERNAME configurado retorna false")
    void enviar_semUsername() {
        ReflectionTestUtils.setField(emailService, "mailUsername", "");

        boolean result = emailService.enviar("dest@email.com", "Assunto", "Corpo");

        assertFalse(result);
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("enviar sem MAIL_USERNAME null retorna false")
    void enviar_usernameNull() {
        ReflectionTestUtils.setField(emailService, "mailUsername", null);

        boolean result = emailService.enviar("dest@email.com", "Assunto", "Corpo");

        assertFalse(result);
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }
}
