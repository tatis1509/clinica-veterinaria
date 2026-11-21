package com.veterinaria.duenos.infraestructure.entry_points;

import com.veterinaria.duenos.domain.model.exception.CredencialesInvalidasException;
import com.veterinaria.duenos.domain.model.exception.DuenoNoEncontradoException;
import com.veterinaria.duenos.domain.model.exception.MovimientoNoEncontradoException;
import com.veterinaria.duenos.domain.model.exception.ReglaNegocioException;
import com.veterinaria.duenos.domain.model.exception.SaldoInsuficienteException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class ManejadorExcepcionesTest {

    private final ManejadorExcepciones manejador = new ManejadorExcepciones();

    @Test
    void traduceCadaExcepcionAlCodigoHttpCorrecto() {
        assertEquals(HttpStatus.NOT_FOUND, manejador.duenoNoEncontrado(new DuenoNoEncontradoException("x")).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, manejador.movimientoNoEncontrado(new MovimientoNoEncontradoException("r")).getStatusCode());
        assertEquals(HttpStatus.CONFLICT, manejador.saldoInsuficiente(new SaldoInsuficienteException("s")).getStatusCode());
        assertEquals(HttpStatus.CONFLICT, manejador.reglaNegocio(new ReglaNegocioException("r")).getStatusCode());
        assertEquals(HttpStatus.UNAUTHORIZED, manejador.credenciales(new CredencialesInvalidasException("c")).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, manejador.peticionInvalida(new IllegalArgumentException("i")).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, manejador.jsonInvalido(mock(HttpMessageNotReadableException.class)).getStatusCode());
        assertEquals(HttpStatus.CONFLICT, manejador.integridad(new DataIntegrityViolationException("d")).getStatusCode());
        assertEquals("DUENO_NO_ENCONTRADO", manejador.duenoNoEncontrado(new DuenoNoEncontradoException("x")).getBody().codigo());
    }
}
