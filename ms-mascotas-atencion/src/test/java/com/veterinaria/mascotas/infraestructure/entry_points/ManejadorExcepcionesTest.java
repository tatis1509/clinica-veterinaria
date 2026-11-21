package com.veterinaria.mascotas.infraestructure.entry_points;

import com.veterinaria.mascotas.domain.model.exception.MascotaNoEncontradaException;
import com.veterinaria.mascotas.domain.model.exception.ReglaNegocioException;
import com.veterinaria.mascotas.domain.model.exception.SaldoInsuficienteException;
import com.veterinaria.mascotas.domain.model.exception.ServicioNoDisponibleException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class ManejadorExcepcionesTest {

    private final ManejadorExcepciones m = new ManejadorExcepciones();

    @Test
    void codigosHttp() {
        assertEquals(HttpStatus.NOT_FOUND, m.noEncontrado(new MascotaNoEncontradaException(1L)).getStatusCode());
        assertEquals(HttpStatus.CONFLICT, m.saldoInsuficiente(new SaldoInsuficienteException("s")).getStatusCode());
        assertEquals(HttpStatus.CONFLICT, m.reglaNegocio(new ReglaNegocioException("r")).getStatusCode());
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, m.servicioNoDisponible(new ServicioNoDisponibleException("x")).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, m.peticionInvalida(new IllegalArgumentException("i")).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, m.jsonInvalido(mock(HttpMessageNotReadableException.class)).getStatusCode());
        assertEquals(HttpStatus.CONFLICT, m.integridad(new DataIntegrityViolationException("d")).getStatusCode());
    }
}
