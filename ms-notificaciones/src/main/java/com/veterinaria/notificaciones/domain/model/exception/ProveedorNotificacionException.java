package com.veterinaria.notificaciones.domain.model.exception;

/** El proveedor externo (Twilio o el servidor de correo) fallo o no esta disponible (se responde 502). */
public class ProveedorNotificacionException extends RuntimeException {

    public ProveedorNotificacionException(String mensaje) {
        super(mensaje);
    }
}
