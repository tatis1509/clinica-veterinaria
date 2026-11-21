package com.veterinaria.mascotas.infraestructure.entry_points.dto;

import java.math.BigDecimal;

public record TipoAtencionPeticion(String nombre, String descripcion, BigDecimal precio, Integer duracionMinutos) {
}
