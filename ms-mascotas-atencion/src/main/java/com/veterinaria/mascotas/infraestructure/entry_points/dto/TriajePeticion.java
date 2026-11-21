package com.veterinaria.mascotas.infraestructure.entry_points.dto;

import java.math.BigDecimal;

public record TriajePeticion(String especie, String raza, Integer edad, BigDecimal peso, String sintomas) {
}
