package com.veterinaria.duenos.infraestructure.entry_points.dto;

import java.math.BigDecimal;

/** referencia: identificador unico del cobro (ej. el id de la orden de atencion) para no cobrar dos veces. */
public record DescuentoPeticion(String duenoId, BigDecimal monto, String referencia, String descripcion) {
}
