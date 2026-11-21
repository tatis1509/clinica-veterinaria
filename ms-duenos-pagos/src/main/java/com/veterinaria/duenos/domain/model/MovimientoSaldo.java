package com.veterinaria.duenos.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Registro de cada recarga, cobro (consulta, vacuna, bano...) o reverso del monedero. */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class MovimientoSaldo {

    private String idMovimiento;
    private String idDueno;
    private TipoMovimiento tipo;
    private BigDecimal monto;
    private BigDecimal saldoResultante;
    private String referencia;
    private String descripcion;
    private LocalDateTime fecha;
}
