package com.veterinaria.mascotas.infraestructure.entry_points.dto;


/** cantidad es opcional (por defecto 1). */
public record AgregarServicioPeticion(Long tipoAtencionId, Integer cantidad) {
}
