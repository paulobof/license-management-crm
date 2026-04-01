package com.prediman.crm.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PageableConfig")
class PageableConfigTest {

    @Test
    @DisplayName("PageableConfig pode ser instanciado")
    void pageableConfig_podeSerInstanciado() {
        PageableConfig config = new PageableConfig();

        assertThat(config).isNotNull();
    }
}
