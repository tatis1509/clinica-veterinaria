package com.veterinaria.mascotas.domain.model;

/** Datos del dueno que se consultan en ms-duenos-pagos (para notificarle). */
public record DuenoInfo(String idDueno, String nombre, String correo, String telefono) {
}
