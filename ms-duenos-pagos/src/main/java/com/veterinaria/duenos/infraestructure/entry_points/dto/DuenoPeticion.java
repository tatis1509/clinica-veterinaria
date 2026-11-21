package com.veterinaria.duenos.infraestructure.entry_points.dto;


/** Datos para registrar o actualizar un dueno (en la actualizacion la clave es opcional). */
public record DuenoPeticion(String nombre, String documento, String correo, String clave,
                            String telefono, String direccion) {
}
