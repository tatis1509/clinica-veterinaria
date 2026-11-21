package com.veterinaria.mascotas.domain.model.exception;

public class TipoAtencionNoEncontradoException extends RuntimeException {
    public TipoAtencionNoEncontradoException(Long id) {
        super("Tipo de atencion no encontrado con id: " + id);
    }
}
