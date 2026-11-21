package com.veterinaria.duenos.domain.model;

import com.veterinaria.duenos.domain.model.exception.SaldoInsuficienteException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DuenoTest {

    private Dueno duenoConSaldo(String saldo) {
        return new Dueno("d1", "Ana", "123", "ana@correo.com", "x", "3001234567", "Calle 1", new BigDecimal(saldo));
    }

    @Test
    void acreditarSumaAlSaldo() {
        Dueno dueno = duenoConSaldo("1000.00");
        dueno.acreditar(new BigDecimal("500.00"));
        assertEquals(new BigDecimal("1500.00"), dueno.getSaldo());
    }

    @Test
    void debitarRestaDelSaldo() {
        Dueno dueno = duenoConSaldo("1000.00");
        dueno.debitar(new BigDecimal("400.00"));
        assertEquals(new BigDecimal("600.00"), dueno.getSaldo());
    }

    @Test
    void debitarSinSaldoSuficienteLanzaExcepcion() {
        Dueno dueno = duenoConSaldo("100.00");
        assertThrows(SaldoInsuficienteException.class, () -> dueno.debitar(new BigDecimal("400.00")));
    }
}
