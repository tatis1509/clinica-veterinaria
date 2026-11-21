package com.veterinaria.duenos.domain.model.gateway;

import com.veterinaria.duenos.domain.model.MovimientoSaldo;

import java.math.BigDecimal;
import java.util.List;

/**
 * Operaciones sobre el monedero. Cada escritura debe ser ATOMICA (bloquea al dueno, valida
 * y guarda saldo + movimiento en una sola transaccion). descontar y reversar son IDEMPOTENTES
 * respecto a la referencia (un mismo cobro nunca se aplica dos veces).
 */
public interface SaldoGateway {

    BigDecimal consultarSaldo(String idDueno);

    MovimientoSaldo recargar(String idDueno, BigDecimal monto, String referencia);

    MovimientoSaldo descontar(String idDueno, BigDecimal monto, String referencia, String descripcion);

    MovimientoSaldo reversar(String idDueno, String referencia);

    List<MovimientoSaldo> listarMovimientos(String idDueno);
}
