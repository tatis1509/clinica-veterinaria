package com.veterinaria.mascotas.aplicattion;

import com.veterinaria.mascotas.domain.model.gateway.DuenoPagoGateway;
import com.veterinaria.mascotas.domain.model.gateway.MascotaGateway;
import com.veterinaria.mascotas.domain.model.gateway.NotificacionGateway;
import com.veterinaria.mascotas.domain.model.gateway.OrdenAtencionGateway;
import com.veterinaria.mascotas.domain.model.gateway.TipoAtencionGateway;
import com.veterinaria.mascotas.domain.model.gateway.TriajeIaGateway;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class UseCasesConfigTest {

    @Test
    void creaLosCasosDeUso() {
        UseCasesConfig c = new UseCasesConfig();
        assertNotNull(c.mascotaUseCase(mock(MascotaGateway.class), mock(DuenoPagoGateway.class)));
        assertNotNull(c.tipoAtencionUseCase(mock(TipoAtencionGateway.class)));
        assertNotNull(c.atencionUseCase(mock(OrdenAtencionGateway.class), mock(MascotaGateway.class),
                mock(TipoAtencionGateway.class), mock(DuenoPagoGateway.class), mock(NotificacionGateway.class)));
        assertNotNull(c.triajeUseCase(mock(TriajeIaGateway.class)));
    }
}
