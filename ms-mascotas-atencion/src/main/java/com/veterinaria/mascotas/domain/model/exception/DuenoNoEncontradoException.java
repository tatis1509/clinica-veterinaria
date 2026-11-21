package com.veterinaria.mascotas.domain.model.exception;

public class DuenoNoEncontradoException extends RuntimeException {
    public DuenoNoEncontradoException(String id) {
        super("Dueno no encontrado en ms-duenos-pagos con id: " + id);
    }
}
