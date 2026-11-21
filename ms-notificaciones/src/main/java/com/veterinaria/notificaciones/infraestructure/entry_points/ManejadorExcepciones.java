package com.veterinaria.notificaciones.infraestructure.entry_points;

import com.veterinaria.notificaciones.domain.model.exception.NotificacionNoEncontradaException;
import com.veterinaria.notificaciones.domain.model.exception.ProveedorNotificacionException;
import com.veterinaria.notificaciones.domain.model.exception.ReglaNegocioException;
import com.veterinaria.notificaciones.domain.model.exception.ValidacionNotificacionException;
import com.veterinaria.notificaciones.infraestructure.entry_points.dto.ErrorRespuesta;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ManejadorExcepciones {

    // 400 - datos invalidos o JSON mal formado
    @ExceptionHandler(ValidacionNotificacionException.class)
    public ResponseEntity<ErrorRespuesta> validacion(ValidacionNotificacionException e) {
        return responder(HttpStatus.BAD_REQUEST, "PETICION_INVALIDA", e.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorRespuesta> jsonInvalido(HttpMessageNotReadableException e) {
        return responder(HttpStatus.BAD_REQUEST, "PETICION_INVALIDA",
                "El cuerpo de la peticion no es un JSON valido (revise canal, tipo, estado y fechas yyyy-MM-dd)");
    }

    @ExceptionHandler(NotificacionNoEncontradaException.class)
    public ResponseEntity<ErrorRespuesta> noEncontrada(NotificacionNoEncontradaException e) {
        return responder(HttpStatus.NOT_FOUND, "NOTIFICACION_NO_ENCONTRADA", e.getMessage());
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<ErrorRespuesta> reglaNegocio(ReglaNegocioException e) {
        return responder(HttpStatus.CONFLICT, "REGLA_NEGOCIO", e.getMessage());
    }

    // 502 - fallo el proveedor externo (Twilio o el servidor de correo)
    @ExceptionHandler(ProveedorNotificacionException.class)
    public ResponseEntity<ErrorRespuesta> proveedor(ProveedorNotificacionException e) {
        return responder(HttpStatus.BAD_GATEWAY, "PROVEEDOR_NO_DISPONIBLE", e.getMessage());
    }

    private ResponseEntity<ErrorRespuesta> responder(HttpStatus estado, String codigo, String mensaje) {
        return ResponseEntity.status(estado).body(new ErrorRespuesta(codigo, mensaje));
    }
}
