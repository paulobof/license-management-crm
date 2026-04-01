package com.prediman.crm.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("RestTemplateConfig")
class RestTemplateConfigTest {

    private final RestTemplateConfig restTemplateConfig = new RestTemplateConfig();

    @Test
    @DisplayName("restTemplate retorna instancia nao nula")
    void restTemplate_retornaInstanciaNaoNula() {
        RestTemplateBuilder builder = new RestTemplateBuilder();

        RestTemplate restTemplate = restTemplateConfig.restTemplate(builder);

        assertThat(restTemplate).isNotNull();
    }

    @Test
    @DisplayName("restTemplate e do tipo RestTemplate")
    void restTemplate_tipoCorreto() {
        RestTemplateBuilder builder = new RestTemplateBuilder();

        Object result = restTemplateConfig.restTemplate(builder);

        assertThat(result).isInstanceOf(RestTemplate.class);
    }
}
