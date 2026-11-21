package com.veterinaria.duenos.domain.model;

import com.veterinaria.duenos.domain.model.exception.SaldoInsuficienteException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/** Dueno de una o varias mascotas. Tiene un monedero (saldo) para pagar los servicios de la clinica. */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Dueno {

    /** Todo dueno nuevo arranca con el monedero en cero: debe recargar para pagar atenciones. */
    public static final BigDecimal SALDO_INICIAL = new BigDecimal("0.00");

    private String idDueno;
    private String nombre;
    private String documento;
    private String correo;
    private String clave;
    private String telefono;
    private String direccion;
    private BigDecimal saldo;

    /** Suma dinero al saldo (recarga o reverso). */
    public void acreditar(BigDecimal monto) {
        this.saldo = this.saldo.add(monto);
    }

    /** Resta dinero al saldo. Regla de dominio: el saldo nunca puede quedar negativo. */
    public void debitar(BigDecimal monto) {
        if (this.saldo.compareTo(monto) < 0) {
            throw new SaldoInsuficienteException("Saldo insuficiente: disponible $"
                    + this.saldo.toPlainString() + ", requerido $" + monto.toPlainString());
        }
        this.saldo = this.saldo.subtract(monto);
    }
}
