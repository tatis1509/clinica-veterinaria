package com.veterinaria.notificaciones.aplicattion;

import com.veterinaria.notificaciones.domain.model.gateway.EmailGateway;
import com.veterinaria.notificaciones.domain.model.gateway.NotificacionGateway;
import com.veterinaria.notificaciones.domain.model.gateway.SmsGateway;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class UseCasesConfigTest {

    @Test
    void creaElCasoDeUso() {
        assertNotNull(new UseCasesConfig().notificacionUseCase(mock(NotificacionGateway.class),
                mock(EmailGateway.class), mock(SmsGateway.class)));
    }
}
