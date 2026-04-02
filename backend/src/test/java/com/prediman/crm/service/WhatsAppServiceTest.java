package com.prediman.crm.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WhatsAppService — testes de unidade")
class WhatsAppServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private WhatsAppService whatsAppService;

    private void configureService(String url, String key, String instance) {
        ReflectionTestUtils.setField(whatsAppService, "apiUrl", url);
        ReflectionTestUtils.setField(whatsAppService, "apiKey", key);
        ReflectionTestUtils.setField(whatsAppService, "instanceName", instance);
    }

    @Test
    @DisplayName("enviar com sucesso retorna true")
    void enviar_sucesso() {
        configureService("https://evo.api.com", "key123", "prediman");
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>("{}", HttpStatus.OK));

        boolean result = whatsAppService.enviar("11999887766", "Olá teste");

        assertTrue(result);
        verify(restTemplate).postForEntity(
                eq("https://evo.api.com/message/sendText/prediman"),
                any(HttpEntity.class),
                eq(String.class));
    }

    @Test
    @DisplayName("enviar normaliza numero sem codigo pais")
    void enviar_normalizaNumero() {
        configureService("https://evo.api.com", "key123", "inst");
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>("{}", HttpStatus.OK));

        whatsAppService.enviar("(11) 99988-7766", "Teste");

        @SuppressWarnings("unchecked")
        ArgumentCaptor<HttpEntity<Map<String, String>>> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForEntity(anyString(), captor.capture(), eq(String.class));
        assertEquals("5511999887766", captor.getValue().getBody().get("number"));
    }

    @Test
    @DisplayName("enviar com falha retorna false")
    void enviar_falha() {
        configureService("https://evo.api.com", "key123", "inst");
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new RestClientException("Connection refused"));

        boolean result = whatsAppService.enviar("11999887766", "Teste");

        assertFalse(result);
    }

    @Test
    @DisplayName("enviar com numero vazio retorna false")
    void enviar_numeroVazio() {
        assertFalse(whatsAppService.enviar(null, "Teste"));
        assertFalse(whatsAppService.enviar("", "Teste"));
        assertFalse(whatsAppService.enviar("  ", "Teste"));
    }

    @Test
    @DisplayName("enviar sem api configurada retorna false")
    void enviar_semApi() {
        configureService("", "", "");

        boolean result = whatsAppService.enviar("11999887766", "Teste");

        assertFalse(result);
        verifyNoInteractions(restTemplate);
    }

    @Test
    @DisplayName("enviar sem apiUrl retorna false")
    void enviar_semUrl() {
        configureService(null, "key123", "inst");

        boolean result = whatsAppService.enviar("11999887766", "Teste");

        assertFalse(result);
    }

    @Test
    @DisplayName("enviar remove trailing slash da URL")
    void enviar_trailingSlash() {
        configureService("https://evo.api.com///", "key123", "inst");
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>("{}", HttpStatus.OK));

        whatsAppService.enviar("5511999887766", "Teste");

        verify(restTemplate).postForEntity(
                eq("https://evo.api.com/message/sendText/inst"),
                any(HttpEntity.class),
                eq(String.class));
    }
}
