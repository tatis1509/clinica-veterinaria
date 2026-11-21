package com.veterinaria.mascotas.domain.usecase;

import com.veterinaria.mascotas.domain.model.TipoAtencion;
import com.veterinaria.mascotas.domain.model.exception.TipoAtencionNoEncontradoException;
import com.veterinaria.mascotas.domain.model.gateway.TipoAtencionGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TipoAtencionUseCaseTest {

    @Mock
    private TipoAtencionGateway gateway;
    @InjectMocks
    private TipoAtencionUseCase useCase;

    private TipoAtencion vacuna() {
        return new TipoAtencion(null, "Vacunacion", "Rabia", new BigDecimal("60000"), 15);
    }

    @Test
    void crudCompleto() {
        when(gateway.guardar(any(TipoAtencion.class))).thenAnswer(inv -> inv.getArgument(0));
        when(gateway.buscarPorId(2L)).thenReturn(Optional.of(vacuna()));
        when(gateway.listar()).thenReturn(List.of(vacuna()));

        assertEquals("Vacunacion", useCase.crear(vacuna()).getNombre());
        assertEquals("Vacunacion", useCase.buscarPorId(2L).getNombre());
        assertEquals(1, useCase.listar().size());
        assertEquals(2L, useCase.actualizar(2L, vacuna()).getIdTipoAtencion());
        useCase.eliminar(2L);
        verify(gateway).eliminar(2L);
    }

    @Test
    void validaciones() {
        when(gateway.buscarPorId(9L)).thenReturn(Optional.empty());
        assertThrows(TipoAtencionNoEncontradoException.class, () -> useCase.buscarPorId(9L));
        assertThrows(IllegalArgumentException.class, () -> useCase.buscarPorId(null));
        assertThrows(IllegalArgumentException.class, () -> useCase.crear(new TipoAtencion(null, null, null, BigDecimal.ONE, 1)));
        assertThrows(IllegalArgumentException.class, () -> useCase.crear(new TipoAtencion(null, "X", null, BigDecimal.ZERO, 1)));
        assertThrows(IllegalArgumentException.class, () -> useCase.crear(new TipoAtencion(null, "X", null, BigDecimal.ONE, 0)));
    }
}
