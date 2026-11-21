package com.veterinaria.duenos.domain.usecase;

import com.veterinaria.duenos.domain.model.MovimientoSaldo;
import com.veterinaria.duenos.domain.model.gateway.SaldoGateway;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

/** Monedero veterinario: recargas, cobros de servicios, reversos y movimientos. */
@RequiredArgsConstructor
public class PagoUseCase {

    public static final BigDecimal RECARGA_MINIMA = new BigDecimal("10000");
    public static final BigDecimal RECARGA_MAXIMA = new BigDecimal("2000000");

    private final SaldoGateway saldoGateway;

    public BigDecimal consultarSaldo(String idDueno) {
        validarTexto(idDueno, "duenoId");
        return saldoGateway.consultarSaldo(idDueno);
    }

    public MovimientoSaldo recargar(String idDueno, BigDecimal monto) {
        validarTexto(idDueno, "duenoId");
        BigDecimal valor = normalizarMonto(monto);
        if (valor.compareTo(RECARGA_MINIMA) < 0 || valor.compareTo(RECARGA_MAXIMA) > 0) {
            throw new IllegalArgumentException("La recarga debe estar entre $10.000 y $2.000.000 COP");
        }
        return saldoGateway.recargar(idDueno, valor, "RECARGA-" + UUID.randomUUID());
    }

    /** Lo usa ms-mascotas-atencion al confirmar una atencion (consulta, vacuna, bano...). */
    public MovimientoSaldo descontar(String idDueno, BigDecimal monto, String referencia, String descripcion) {
        validarTexto(idDueno, "duenoId");
        validarTexto(referencia, "referencia");
        String detalle = (descripcion == null || descripcion.isBlank()) ? "Cobro de servicio veterinario" : descripcion;
        return saldoGateway.descontar(idDueno, normalizarMonto(monto), referencia, detalle);
    }

    /** Devuelve el dinero de un cobro (por ejemplo si se cancela una cita ya pagada). */
    public MovimientoSaldo reversar(String idDueno, String referencia) {
        validarTexto(idDueno, "duenoId");
        validarTexto(referencia, "referencia");
        return saldoGateway.reversar(idDueno, referencia);
    }

    public List<MovimientoSaldo> movimientos(String idDueno) {
        validarTexto(idDueno, "duenoId");
        return saldoGateway.listarMovimientos(idDueno);
    }

    private BigDecimal normalizarMonto(BigDecimal monto) {
        if (monto == null) {
            throw new IllegalArgumentException("El monto es obligatorio");
        }
        BigDecimal valor = monto.setScale(2, RoundingMode.HALF_UP);
        if (valor.signum() <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor que cero");
        }
        return valor;
    }

    private void validarTexto(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("El campo " + campo + " es obligatorio");
        }
    }
}
