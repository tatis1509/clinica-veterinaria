package com.veterinaria.duenos.domain.model.exception;

public class MovimientoNoEncontradoException extends RuntimeException {
    public MovimientoNoEncontradoException(String referencia) {
        super("No existe un cobro registrado con la referencia: " + referencia);
    }
}
