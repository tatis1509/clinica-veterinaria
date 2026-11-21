package com.veterinaria.mascotas.domain.model;

import java.math.BigDecimal;

/** Ej: especie=Perro, raza=Labrador, peso=25, sintomas="vomito y decaimiento desde ayer". */
public record TriajeSolicitud(String especie, String raza, Integer edad, BigDecimal peso, String sintomas) {
}
