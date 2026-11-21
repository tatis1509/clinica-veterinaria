package com.veterinaria.mascotas.infraestructure.entry_points.dto;

import java.math.BigDecimal;

public record MascotaPeticion(String nombre, String especie, String raza, Integer edad, BigDecimal peso,
                              String duenoId) {
}
