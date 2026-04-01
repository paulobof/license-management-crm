package com.prediman.crm;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;

@DisplayName("CrmApplication — main method coverage")
class CrmApplicationMainTest {

    @Test
    @DisplayName("main invoca SpringApplication.run com a classe e os args")
    void main_invocaSpringApplicationRun() {
        try (MockedStatic<SpringApplication> mocked = mockStatic(SpringApplication.class)) {
            mocked.when(() -> SpringApplication.run(eq(CrmApplication.class), any(String[].class)))
                    .thenReturn(null);

            CrmApplication.main(new String[]{});

            mocked.verify(() -> SpringApplication.run(eq(CrmApplication.class), any(String[].class)));
        }
    }
}
