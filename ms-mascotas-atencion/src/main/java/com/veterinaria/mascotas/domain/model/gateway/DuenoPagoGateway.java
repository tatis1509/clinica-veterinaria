package com.veterinaria.mascotas.domain.model.gateway;

import com.veterinaria.mascotas.domain.model.DuenoInfo;

import java.math.BigDecimal;

/** Puerto hacia ms-duenos-pagos, el unico dueno del dinero. */
public interface DuenoPagoGateway {

    /** Lanza DuenoNoEncontradoException si no existe. */
    DuenoInfo consultarDueno(String duenoId);

    /** Cobra el total de la atencion. Devuelve el saldo restante. Idempotente por referencia. */
    BigDecimal descontar(String duenoId, BigDecimal monto, String referencia, String descripcion);

    /** Devuelve el dinero de un cobro. false si no habia cobro con esa referencia. */
    boolean reversar(String duenoId, String referencia);
}
