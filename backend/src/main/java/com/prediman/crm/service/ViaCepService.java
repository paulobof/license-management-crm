package com.prediman.crm.service;

import com.prediman.crm.dto.ViaCepResponse;
import com.prediman.crm.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class ViaCepService {

    private static final String VIA_CEP_URL = "https://viacep.com.br/ws/{cep}/json/";

    private final RestTemplate restTemplate;

    public ViaCepResponse buscarCep(String cep) {
        String cepNumerico = cep.replaceAll("[^0-9]", "");

        if (cepNumerico.length() != 8) {
            throw new IllegalArgumentException("CEP inválido: " + cep);
        }

        log.debug("Consultando ViaCEP para o CEP: {}", cepNumerico);

        ViaCepResponse response = restTemplate.getForObject(VIA_CEP_URL, ViaCepResponse.class, cepNumerico);

        if (response == null || response.isErro()) {
            throw new ResourceNotFoundException("CEP não encontrado: " + cep);
        }

        return response;
    }
}
