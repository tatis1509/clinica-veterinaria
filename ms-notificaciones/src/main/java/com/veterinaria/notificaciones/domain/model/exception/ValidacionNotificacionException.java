package com.veterinaria.notificaciones.domain.model.exception;

/** Los datos de la notificacion no son validos (se responde 400). */
public class ValidacionNotificacionException extends RuntimeException {

    public ValidacionNotificacionException(String mensaje) {
        super(mensaje);
    }
}
