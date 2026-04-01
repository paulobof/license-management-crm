package com.prediman.crm.service;

import com.prediman.crm.dto.ViaCepResponse;
import com.prediman.crm.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ViaCepServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private ViaCepService viaCepService;

    private static final String URL = "https://viacep.com.br/ws/{cep}/json/";

    // -------------------------------------------------------------------------
    // buscarCep — success
    // -------------------------------------------------------------------------

    @Test
    void buscarCep_cepValido_retornaViaCepResponse() {
        ViaCepResponse viaCepResponse = new ViaCepResponse(
                "01310-100", "Av. Paulista", "", "Bela Vista", "São Paulo", "SP", null);

        when(restTemplate.getForObject(URL, ViaCepResponse.class, "01310100"))
                .thenReturn(viaCepResponse);

        ViaCepResponse result = viaCepService.buscarCep("01310-100");

        assertThat(result).isNotNull();
        assertThat(result.getCep()).isEqualTo("01310-100");
        assertThat(result.getLogradouro()).isEqualTo("Av. Paulista");
        assertThat(result.getCidade()).isEqualTo("São Paulo");
        assertThat(result.getEstado()).isEqualTo("SP");
    }

    @Test
    void buscarCep_cepSemMascara_consultaCorretamente() {
        ViaCepResponse viaCepResponse = new ViaCepResponse(
                "01001000", "Praça da Sé", "", "Sé", "São Paulo", "SP", null);

        when(restTemplate.getForObject(URL, ViaCepResponse.class, "01001000"))
                .thenReturn(viaCepResponse);

        ViaCepResponse result = viaCepService.buscarCep("01001000");

        assertThat(result).isNotNull();
        assertThat(result.getCidade()).isEqualTo("São Paulo");
    }

    // -------------------------------------------------------------------------
    // buscarCep — CEP inválido (comprimento != 8)
    // -------------------------------------------------------------------------

    @Test
    void buscarCep_cepMenosDe8Digitos_lancaIllegalArgumentException() {
        assertThatThrownBy(() -> viaCepService.buscarCep("1234567"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CEP inválido");
    }

    @Test
    void buscarCep_cepMaisDe8Digitos_lancaIllegalArgumentException() {
        assertThatThrownBy(() -> viaCepService.buscarCep("123456789"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CEP inválido");
    }

    @Test
    void buscarCep_cepVazio_lancaIllegalArgumentException() {
        assertThatThrownBy(() -> viaCepService.buscarCep(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CEP inválido");
    }

    // -------------------------------------------------------------------------
    // buscarCep — CEP não encontrado (API retorna erro=true)
    // -------------------------------------------------------------------------

    @Test
    void buscarCep_cepNaoEncontrado_lancaResourceNotFoundException() {
        ViaCepResponse erroResponse = new ViaCepResponse(null, null, null, null, null, null, "true");

        when(restTemplate.getForObject(URL, ViaCepResponse.class, "99999999"))
                .thenReturn(erroResponse);

        assertThatThrownBy(() -> viaCepService.buscarCep("99999999"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("CEP não encontrado");
    }

    @Test
    void buscarCep_apiRetornaNulo_lancaResourceNotFoundException() {
        when(restTemplate.getForObject(URL, ViaCepResponse.class, "00000000"))
                .thenReturn(null);

        assertThatThrownBy(() -> viaCepService.buscarCep("00000000"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("CEP não encontrado");
    }

    // -------------------------------------------------------------------------
    // buscarCep — erro de comunicação com API
    // -------------------------------------------------------------------------

    @Test
    void buscarCep_erroDeRede_propagaRestClientException() {
        when(restTemplate.getForObject(URL, ViaCepResponse.class, "01310100"))
                .thenThrow(new RestClientException("connection timeout"));

        assertThatThrownBy(() -> viaCepService.buscarCep("01310-100"))
                .isInstanceOf(RestClientException.class)
                .hasMessageContaining("connection timeout");
    }

    // -------------------------------------------------------------------------
    // buscarCep — strips non-numeric characters
    // -------------------------------------------------------------------------

    @Test
    void buscarCep_cepComPontoEHifen_extraiSomenteDigitos() {
        ViaCepResponse viaCepResponse = new ViaCepResponse(
                "04538-133", "Av. Brigadeiro Faria Lima", "", "Itaim Bibi", "São Paulo", "SP", null);

        when(restTemplate.getForObject(URL, ViaCepResponse.class, "04538133"))
                .thenReturn(viaCepResponse);

        ViaCepResponse result = viaCepService.buscarCep("04538-133");

        assertThat(result).isNotNull();
        assertThat(result.getBairro()).isEqualTo("Itaim Bibi");
    }
}
