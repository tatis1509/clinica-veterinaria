package com.veterinaria.mascotas.domain.model;

import java.math.BigDecimal;

public record ResultadoAtencion(OrdenAtencion orden, BigDecimal saldoRestante) {
}
