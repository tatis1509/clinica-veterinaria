package com.veterinaria.duenos.infraestructure.entry_points;

import com.veterinaria.duenos.domain.model.MovimientoSaldo;
import com.veterinaria.duenos.domain.model.TipoMovimiento;
import com.veterinaria.duenos.domain.usecase.PagoUseCase;
import com.veterinaria.duenos.infraestructure.entry_points.dto.DescuentoPeticion;
import com.veterinaria.duenos.infraestructure.entry_points.dto.RecargaPeticion;
import com.veterinaria.duenos.infraestructure.entry_points.dto.ReversoPeticion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PagoControllerTest {

    @Mock
    private PagoUseCase useCase;
    @InjectMocks
    private PagoController controller;

    private MovimientoSaldo mov(TipoMovimiento tipo) {
        return new MovimientoSaldo("m1", "d1", tipo, BigDecimal.TEN, BigDecimal.TEN, "ref", "d", LocalDateTime.now());
    }

    @Test
    void operacionesDelMonedero() {
        BigDecimal monto = new BigDecimal("50000");
        when(useCase.consultarSaldo("d1")).thenReturn(monto);
        when(useCase.recargar("d1", monto)).thenReturn(mov(TipoMovimiento.RECARGA));
        when(useCase.descontar("d1", monto, "orden-1", "Vacuna")).thenReturn(mov(TipoMovimiento.DESCUENTO));
        when(useCase.reversar("d1", "orden-1")).thenReturn(mov(TipoMovimiento.REVERSO));
        when(useCase.movimientos("d1")).thenReturn(List.of(mov(TipoMovimiento.RECARGA)));

        assertEquals(monto, controller.consultarSaldo("d1").getBody().saldo());
        assertEquals(TipoMovimiento.RECARGA, controller.recargar(new RecargaPeticion("d1", monto)).getBody().getTipo());
        assertEquals(TipoMovimiento.DESCUENTO, controller.descontar(
                new DescuentoPeticion("d1", monto, "orden-1", "Vacuna")).getBody().getTipo());
        assertEquals(TipoMovimiento.REVERSO, controller.reversar(
                new ReversoPeticion("d1", "orden-1")).getBody().getTipo());
        assertEquals(1, controller.movimientos("d1").getBody().size());
    }
}
