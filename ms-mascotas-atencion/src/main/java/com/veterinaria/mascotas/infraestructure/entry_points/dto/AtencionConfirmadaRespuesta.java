package com.veterinaria.mascotas.infraestructure.entry_points.dto;

import com.veterinaria.mascotas.domain.model.OrdenAtencion;

import java.math.BigDecimal;

public record AtencionConfirmadaRespuesta(String mensaje, OrdenAtencion orden, BigDecimal saldoRestante) {
}
