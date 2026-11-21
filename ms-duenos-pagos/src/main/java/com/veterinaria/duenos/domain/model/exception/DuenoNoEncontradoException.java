package com.veterinaria.duenos.domain.model.exception;

public class DuenoNoEncontradoException extends RuntimeException {
    public DuenoNoEncontradoException(String idDueno) {
        super("Dueno no encontrado con id: " + idDueno);
    }
}
