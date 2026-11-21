package com.veterinaria.duenos.domain.usecase;

import com.veterinaria.duenos.domain.model.MovimientoSaldo;
import com.veterinaria.duenos.domain.model.TipoMovimiento;
import com.veterinaria.duenos.domain.model.gateway.SaldoGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PagoUseCaseTest {

    @Mock
    private SaldoGateway saldoGateway;
    @InjectMocks
    private PagoUseCase useCase;

    private MovimientoSaldo movimiento(TipoMovimiento tipo, String monto) {
        return new MovimientoSaldo("m1", "d1", tipo, new BigDecimal(monto), new BigDecimal("100000.00"),
                "ref-1", "detalle", LocalDateTime.now());
    }

    @Test
    void consultarSaldo() {
        when(saldoGateway.consultarSaldo("d1")).thenReturn(new BigDecimal("50000.00"));
        assertEquals(new BigDecimal("50000.00"), useCase.consultarSaldo("d1"));
    }

    @Test
    void recargarDentroDelRango() {
        when(saldoGateway.recargar(eq("d1"), eq(new BigDecimal("100000.00")), anyString()))
                .thenReturn(movimiento(TipoMovimiento.RECARGA, "100000.00"));
        assertEquals(TipoMovimiento.RECARGA, useCase.recargar("d1", new BigDecimal("100000")).getTipo());
    }

    @Test
    void recargarFueraDeRangoFalla() {
        assertThrows(IllegalArgumentException.class, () -> useCase.recargar("d1", new BigDecimal("500")));
        assertThrows(IllegalArgumentException.class, () -> useCase.recargar("d1", null));
        assertThrows(IllegalArgumentException.class, () -> useCase.recargar("d1", new BigDecimal("-5")));
        assertThrows(IllegalArgumentException.class, () -> useCase.recargar(null, new BigDecimal("50000")));
    }

    @Test
    void descontarConDescripcion() {
        when(saldoGateway.descontar("d1", new BigDecimal("45000.00"), "orden-1", "Consulta general"))
                .thenReturn(movimiento(TipoMovimiento.DESCUENTO, "45000.00"));
        assertEquals(TipoMovimiento.DESCUENTO,
                useCase.descontar("d1", new BigDecimal("45000"), "orden-1", "Consulta general").getTipo());
    }

    @Test
    void descontarSinDescripcionUsaUnaPorDefecto() {
        when(saldoGateway.descontar("d1", new BigDecimal("45000.00"), "orden-1", "Cobro de servicio veterinario"))
                .thenReturn(movimiento(TipoMovimiento.DESCUENTO, "45000.00"));
        assertEquals("m1", useCase.descontar("d1", new BigDecimal("45000"), "orden-1", null).getIdMovimiento());
    }

    @Test
    void reversar() {
        when(saldoGateway.reversar("d1", "orden-1")).thenReturn(movimiento(TipoMovimiento.REVERSO, "45000.00"));
        assertEquals(TipoMovimiento.REVERSO, useCase.reversar("d1", "orden-1").getTipo());
    }

    @Test
    void movimientos() {
        when(saldoGateway.listarMovimientos("d1")).thenReturn(List.of(movimiento(TipoMovimiento.RECARGA, "1.00")));
        assertEquals(1, useCase.movimientos("d1").size());
    }
}
