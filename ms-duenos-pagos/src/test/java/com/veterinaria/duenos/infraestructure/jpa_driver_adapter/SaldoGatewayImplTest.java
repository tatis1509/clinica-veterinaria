package com.veterinaria.duenos.infraestructure.jpa_driver_adapter;

import com.veterinaria.duenos.domain.model.MovimientoSaldo;
import com.veterinaria.duenos.domain.model.TipoMovimiento;
import com.veterinaria.duenos.domain.model.exception.ReglaNegocioException;
import com.veterinaria.duenos.infraestructure.mapper.DuenoMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SaldoGatewayImplTest {

    @Mock
    private DuenoRepositoryJPA duenoRepository;
    @Mock
    private MovimientoSaldoRepositoryJPA movimientoRepository;
    private SaldoGatewayImpl gateway;
    private DuenoEntity dueno;

    @BeforeEach
    void setUp() {
        gateway = new SaldoGatewayImpl(duenoRepository, movimientoRepository, new DuenoMapper());
        dueno = new DuenoEntity("d1", "Ana", "1010", "ana@correo.com", "hash", "3001234567", null,
                new BigDecimal("100000.00"));
    }

    private MovimientoSaldoEntity movimiento(String idDueno, TipoMovimiento tipo, String monto) {
        MovimientoSaldoEntity m = new MovimientoSaldoEntity();
        m.setIdMovimiento("m1");
        m.setIdDueno(idDueno);
        m.setTipo(tipo);
        m.setMonto(new BigDecimal(monto));
        m.setSaldoResultante(new BigDecimal("55000.00"));
        m.setReferencia("orden-1");
        m.setDescripcion("Consulta general");
        m.setFecha(LocalDateTime.now());
        return m;
    }

    private void guardarDevuelveLoMismo() {
        when(movimientoRepository.save(any(MovimientoSaldoEntity.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void consultarSaldo() {
        when(duenoRepository.findById("d1")).thenReturn(Optional.of(dueno));
        assertEquals(new BigDecimal("100000.00"), gateway.consultarSaldo("d1"));
    }

    @Test
    void recargarSumaAlSaldo() {
        when(duenoRepository.findByIdForUpdate("d1")).thenReturn(Optional.of(dueno));
        guardarDevuelveLoMismo();

        MovimientoSaldo mov = gateway.recargar("d1", new BigDecimal("50000.00"), "RECARGA-1");

        assertEquals(TipoMovimiento.RECARGA, mov.getTipo());
        assertEquals(new BigDecimal("150000.00"), mov.getSaldoResultante());
    }

    @Test
    void descontarCobraUnaSolaVez() {
        when(duenoRepository.findByIdForUpdate("d1")).thenReturn(Optional.of(dueno));
        when(movimientoRepository.findByReferenciaAndTipo("orden-1", TipoMovimiento.DESCUENTO))
                .thenReturn(Optional.empty());
        guardarDevuelveLoMismo();

        MovimientoSaldo mov = gateway.descontar("d1", new BigDecimal("45000.00"), "orden-1", "Consulta");

        assertEquals(new BigDecimal("55000.00"), mov.getSaldoResultante());
        assertEquals(new BigDecimal("55000.00"), dueno.getSaldo());
    }

    @Test
    void descontarConReferenciaRepetidaDevuelveElCobroPrevio() {
        when(duenoRepository.findByIdForUpdate("d1")).thenReturn(Optional.of(dueno));
        when(movimientoRepository.findByReferenciaAndTipo("orden-1", TipoMovimiento.DESCUENTO))
                .thenReturn(Optional.of(movimiento("d1", TipoMovimiento.DESCUENTO, "45000.00")));
        when(movimientoRepository.findByReferenciaAndTipo("orden-1", TipoMovimiento.REVERSO))
                .thenReturn(Optional.empty());

        MovimientoSaldo mov = gateway.descontar("d1", new BigDecimal("45000.00"), "orden-1", "Consulta");

        assertEquals("m1", mov.getIdMovimiento());
        verify(movimientoRepository, never()).save(any());
    }

    @Test
    void descontarReferenciaDeOtroDuenoFalla() {
        when(duenoRepository.findByIdForUpdate("d1")).thenReturn(Optional.of(dueno));
        when(movimientoRepository.findByReferenciaAndTipo("orden-1", TipoMovimiento.DESCUENTO))
                .thenReturn(Optional.of(movimiento("otro", TipoMovimiento.DESCUENTO, "45000.00")));
        assertThrows(ReglaNegocioException.class,
                () -> gateway.descontar("d1", new BigDecimal("45000.00"), "orden-1", "Consulta"));
    }

    @Test
    void reversarDevuelveElDinero() {
        when(duenoRepository.findByIdForUpdate("d1")).thenReturn(Optional.of(dueno));
        when(movimientoRepository.findByReferenciaAndTipo("orden-1", TipoMovimiento.REVERSO))
                .thenReturn(Optional.empty());
        when(movimientoRepository.findByReferenciaAndTipo("orden-1", TipoMovimiento.DESCUENTO))
                .thenReturn(Optional.of(movimiento("d1", TipoMovimiento.DESCUENTO, "45000.00")));
        guardarDevuelveLoMismo();

        MovimientoSaldo mov = gateway.reversar("d1", "orden-1");

        assertEquals(TipoMovimiento.REVERSO, mov.getTipo());
        assertEquals(new BigDecimal("145000.00"), mov.getSaldoResultante());
    }

    @Test
    void reversarDosVecesDevuelveElReversoPrevio() {
        when(duenoRepository.findByIdForUpdate("d1")).thenReturn(Optional.of(dueno));
        when(movimientoRepository.findByReferenciaAndTipo("orden-1", TipoMovimiento.REVERSO))
                .thenReturn(Optional.of(movimiento("d1", TipoMovimiento.REVERSO, "45000.00")));
        assertEquals(TipoMovimiento.REVERSO, gateway.reversar("d1", "orden-1").getTipo());
    }

    @Test
    void listarMovimientos() {
        when(duenoRepository.existsById("d1")).thenReturn(true);
        when(movimientoRepository.findByIdDuenoOrderByFechaDesc("d1"))
                .thenReturn(List.of(movimiento("d1", TipoMovimiento.RECARGA, "50000.00")));
        assertEquals(1, gateway.listarMovimientos("d1").size());
    }
}
