package com.veterinaria.mascotas.domain.model.exception;

public class MascotaNoEncontradaException extends RuntimeException {
    public MascotaNoEncontradaException(Long id) {
        super("Mascota no encontrada con id: " + id);
    }
}
