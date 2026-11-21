package com.veterinaria.notificaciones.domain.model.exception;

public class NotificacionNoEncontradaException extends RuntimeException {
    public NotificacionNoEncontradaException(Long id) {
        super("Notificacion no encontrada con id: " + id);
    }
}
