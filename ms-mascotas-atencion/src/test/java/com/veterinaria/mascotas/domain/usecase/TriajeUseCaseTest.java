package com.veterinaria.mascotas.domain.usecase;

import com.veterinaria.mascotas.domain.model.TriajeResultado;
import com.veterinaria.mascotas.domain.model.TriajeSolicitud;
import com.veterinaria.mascotas.domain.model.exception.ServicioNoDisponibleException;
import com.veterinaria.mascotas.domain.model.gateway.TriajeIaGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TriajeUseCaseTest {

    @Mock
    private TriajeIaGateway iaGateway;
    @InjectMocks
    private TriajeUseCase useCase;

    private TriajeSolicitud solicitud(String sintomas) {
        return new TriajeSolicitud("Perro", "Labrador", 5, new BigDecimal("25"), sintomas);
    }

    @Test
    void usaLaRespuestaDeGemini() {
        TriajeResultado ia = new TriajeResultado("MEDIA", "Hidratar y consulta en 24h", "GEMINI", TriajeResultado.ADVERTENCIA);
        when(iaGateway.evaluar(any())).thenReturn(ia);
        assertEquals("GEMINI", useCase.evaluar(solicitud("vomito y decaimiento")).fuente());
    }

    @Test
    void siLaIaNoEstaDisponibleUsaReglas() {
        when(iaGateway.evaluar(any())).thenThrow(new ServicioNoDisponibleException("sin key"));
        TriajeResultado r = useCase.evaluar(solicitud("Vómito y decaimiento"));
        assertEquals("MEDIA", r.nivelUrgencia());
        assertEquals("REGLAS_LOCALES", r.fuente());
    }

    @Test
    void reglasClasificanAltaYBaja() {
        assertEquals("ALTA", useCase.evaluarConReglas(solicitud("tuvo una convulsión")).nivelUrgencia());
        assertEquals("BAJA", useCase.evaluarConReglas(solicitud("control de rutina")).nivelUrgencia());
    }

    @Test
    void sinSintomasFalla() {
        assertThrows(IllegalArgumentException.class, () -> useCase.evaluar(solicitud(" ")));
        assertThrows(IllegalArgumentException.class, () -> useCase.evaluar(null));
    }
}
