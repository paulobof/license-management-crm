package com.prediman.crm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class WhatsAppService {

    private final RestTemplate restTemplate;

    @Value("${evolution.api.url:}")
    private String apiUrl;

    @Value("${evolution.api.key:}")
    private String apiKey;

    @Value("${evolution.api.instance:}")
    private String instanceName;

    public boolean enviar(String numero, String mensagem) {
        if (numero == null || numero.isBlank()) {
            log.warn("Número de WhatsApp vazio; envio ignorado");
            return false;
        }
        if (apiUrl == null || apiUrl.isBlank() || apiKey == null || apiKey.isBlank()) {
            log.warn("Evolution API não configurada; envio de WhatsApp desabilitado");
            return false;
        }

        String numeroNormalizado = normalizarNumero(numero);

        try {
            String url = apiUrl.replaceAll("/+$", "") + "/message/sendText/" + instanceName;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("apikey", apiKey);

            Map<String, String> body = Map.of(
                    "number", numeroNormalizado,
                    "text", mensagem
            );

            HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);
            restTemplate.postForEntity(url, request, String.class);

            log.info("WhatsApp enviado para {}", numeroNormalizado);
            return true;
        } catch (RestClientException e) {
            log.error("Falha ao enviar WhatsApp para {}: {}", numeroNormalizado, e.getMessage());
            return false;
        }
    }

    private String normalizarNumero(String numero) {
        String digits = numero.replaceAll("[^0-9]", "");
        // Se não tem código do país (BR), adiciona 55
        if (digits.length() <= 11) {
            digits = "55" + digits;
        }
        return digits;
    }
}
