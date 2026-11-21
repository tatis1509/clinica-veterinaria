package com.veterinaria.notificaciones.infraestructure.entry_points;

import com.veterinaria.notificaciones.domain.model.exception.NotificacionNoEncontradaException;
import com.veterinaria.notificaciones.domain.model.exception.ProveedorNotificacionException;
import com.veterinaria.notificaciones.domain.model.exception.ReglaNegocioException;
import com.veterinaria.notificaciones.domain.model.exception.ValidacionNotificacionException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class ManejadorExcepcionesTest {

    private final ManejadorExcepciones m = new ManejadorExcepciones();

    @Test
    void codigosHttp() {
        assertEquals(HttpStatus.BAD_REQUEST, m.validacion(new ValidacionNotificacionException("v")).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, m.jsonInvalido(mock(HttpMessageNotReadableException.class)).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, m.noEncontrada(new NotificacionNoEncontradaException(1L)).getStatusCode());
        assertEquals(HttpStatus.CONFLICT, m.reglaNegocio(new ReglaNegocioException("r")).getStatusCode());
        assertEquals(HttpStatus.BAD_GATEWAY, m.proveedor(new ProveedorNotificacionException("p")).getStatusCode());
    }
}
