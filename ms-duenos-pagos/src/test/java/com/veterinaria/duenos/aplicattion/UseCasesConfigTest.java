package com.veterinaria.duenos.aplicattion;

import com.veterinaria.duenos.domain.model.gateway.DuenoGateway;
import com.veterinaria.duenos.domain.model.gateway.EncrypterGateway;
import com.veterinaria.duenos.domain.model.gateway.SaldoGateway;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class UseCasesConfigTest {

    @Test
    void creaLosCasosDeUso() {
        UseCasesConfig config = new UseCasesConfig();
        assertNotNull(config.duenoUseCase(mock(DuenoGateway.class), mock(EncrypterGateway.class)));
        assertNotNull(config.pagoUseCase(mock(SaldoGateway.class)));
    }
}
