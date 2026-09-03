package com.prediman.crm.service;

import com.prediman.crm.model.Cliente;
import com.prediman.crm.model.Contrato;
import com.prediman.crm.model.enums.Periodicidade;
import com.prediman.crm.model.enums.StatusContrato;
import com.prediman.crm.repository.ContratoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.annotation.Scheduled;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CobrancaScheduler — geração automática mensal")
class CobrancaSchedulerTest {

    @Mock
    private ContratoRepository contratoRepository;

    @Mock
    private ContratoService contratoService;

    @InjectMocks
    private CobrancaScheduler cobrancaScheduler;

    private Contrato contrato(Long id) {
        return Contrato.builder()
                .id(id)
                .cliente(Cliente.builder().id(1L).razaoSocial("Cliente Teste").build())
                .descricao("Contrato " + id)
                .valor(new BigDecimal("1000.00"))
                .periodicidade(Periodicidade.MENSAL)
                .dataInicio(LocalDate.of(2024, 1, 5))
                .status(StatusContrato.ATIVO)
                .build();
    }

    @Test
    @DisplayName("busca apenas contratos MENSAL/ATIVO e gera a parcela de cada um")
    void executarGeracaoMensal_geraParcelaParaCadaContratoMensalAtivo() {
        LocalDate referencia = LocalDate.of(2024, 3, 1);

        when(contratoRepository.findByPeriodicidadeAndStatus(Periodicidade.MENSAL, StatusContrato.ATIVO))
                .thenReturn(List.of(contrato(1L), contrato(2L)));
        when(contratoService.gerarCobrancaMensalAutomatica(any(), eq(referencia))).thenReturn(true);

        cobrancaScheduler.executarGeracaoMensal(referencia);

        verify(contratoService).gerarCobrancaMensalAutomatica(1L, referencia);
        verify(contratoService).gerarCobrancaMensalAutomatica(2L, referencia);
        verify(contratoRepository).findByPeriodicidadeAndStatus(Periodicidade.MENSAL, StatusContrato.ATIVO);
    }

    @Test
    @DisplayName("contratos com parcela já gerada são apenas ignorados")
    void executarGeracaoMensal_ignoraContratosDuplicados() {
        LocalDate referencia = LocalDate.of(2024, 3, 1);

        when(contratoRepository.findByPeriodicidadeAndStatus(Periodicidade.MENSAL, StatusContrato.ATIVO))
                .thenReturn(List.of(contrato(1L), contrato(2L)));
        when(contratoService.gerarCobrancaMensalAutomatica(eq(1L), eq(referencia))).thenReturn(false);
        when(contratoService.gerarCobrancaMensalAutomatica(eq(2L), eq(referencia))).thenReturn(true);

        cobrancaScheduler.executarGeracaoMensal(referencia);

        verify(contratoService, times(2)).gerarCobrancaMensalAutomatica(any(), eq(referencia));
    }

    @Test
    @DisplayName("falha em um contrato não aborta o processamento dos demais")
    void executarGeracaoMensal_falhaEmUmContratoNaoAbortaOsDemais() {
        LocalDate referencia = LocalDate.of(2024, 3, 1);

        when(contratoRepository.findByPeriodicidadeAndStatus(Periodicidade.MENSAL, StatusContrato.ATIVO))
                .thenReturn(List.of(contrato(1L), contrato(2L), contrato(3L)));
        when(contratoService.gerarCobrancaMensalAutomatica(eq(1L), eq(referencia)))
                .thenThrow(new RuntimeException("Falha simulada"));
        when(contratoService.gerarCobrancaMensalAutomatica(eq(2L), eq(referencia))).thenReturn(true);
        when(contratoService.gerarCobrancaMensalAutomatica(eq(3L), eq(referencia))).thenReturn(true);

        cobrancaScheduler.executarGeracaoMensal(referencia);

        verify(contratoService).gerarCobrancaMensalAutomatica(2L, referencia);
        verify(contratoService).gerarCobrancaMensalAutomatica(3L, referencia);
    }

    @Test
    @DisplayName("lista vazia de contratos não dispara geração")
    void executarGeracaoMensal_semContratos_naoGeraNada() {
        when(contratoRepository.findByPeriodicidadeAndStatus(Periodicidade.MENSAL, StatusContrato.ATIVO))
                .thenReturn(Collections.emptyList());

        cobrancaScheduler.executarGeracaoMensal(LocalDate.of(2024, 3, 1));

        verify(contratoService, never()).gerarCobrancaMensalAutomatica(any(), any());
    }

    @Test
    @DisplayName("erro ao carregar contratos é tratado sem propagar exceção")
    void executarGeracaoMensal_erroAoCarregarContratos_naoPropaga() {
        when(contratoRepository.findByPeriodicidadeAndStatus(Periodicidade.MENSAL, StatusContrato.ATIVO))
                .thenThrow(new RuntimeException("Banco indisponível"));

        cobrancaScheduler.executarGeracaoMensal(LocalDate.of(2024, 3, 1));

        verify(contratoService, never()).gerarCobrancaMensalAutomatica(any(), any());
    }

    @Test
    @DisplayName("sobrecarga sem parâmetro usa a data atual como referência")
    void executarGeracaoMensal_semParametro_usaDataAtual() {
        when(contratoRepository.findByPeriodicidadeAndStatus(Periodicidade.MENSAL, StatusContrato.ATIVO))
                .thenReturn(List.of(contrato(1L)));
        when(contratoService.gerarCobrancaMensalAutomatica(eq(1L), eq(LocalDate.now()))).thenReturn(true);

        cobrancaScheduler.executarGeracaoMensal();

        verify(contratoService).gerarCobrancaMensalAutomatica(1L, LocalDate.now());
    }

    @Test
    @DisplayName("o job está agendado para o dia 1 de cada mês às 02:00")
    void executarGeracaoMensal_possuiCronDoDiaUmAsDuasHoras() throws NoSuchMethodException {
        Scheduled scheduled = CobrancaScheduler.class
                .getMethod("executarGeracaoMensal")
                .getAnnotation(Scheduled.class);

        assertThat(scheduled).isNotNull();
        assertThat(scheduled.cron()).isEqualTo("0 0 2 1 * ?");
    }
}
