package com.veterinaria.mascotas.domain.model.exception;

public class OrdenNoEncontradaException extends RuntimeException {
    public OrdenNoEncontradaException(String id) {
        super("Orden de atencion no encontrada: " + id);
    }
}
