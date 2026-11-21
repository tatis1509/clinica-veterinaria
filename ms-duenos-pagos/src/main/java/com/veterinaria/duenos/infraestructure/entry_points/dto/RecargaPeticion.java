package com.veterinaria.duenos.infraestructure.entry_points.dto;

import java.math.BigDecimal;

public record RecargaPeticion(String duenoId, BigDecimal monto) {
}
