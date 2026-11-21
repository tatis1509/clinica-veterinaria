package com.veterinaria.mascotas.infraestructure.entry_points;

import com.veterinaria.mascotas.domain.model.exception.DuenoNoEncontradoException;
import com.veterinaria.mascotas.domain.model.exception.MascotaNoEncontradaException;
import com.veterinaria.mascotas.domain.model.exception.OrdenNoEncontradaException;
import com.veterinaria.mascotas.domain.model.exception.ReglaNegocioException;
import com.veterinaria.mascotas.domain.model.exception.SaldoInsuficienteException;
import com.veterinaria.mascotas.domain.model.exception.ServicioNoDisponibleException;
import com.veterinaria.mascotas.domain.model.exception.TipoAtencionNoEncontradoException;
import com.veterinaria.mascotas.infraestructure.entry_points.dto.ErrorRespuesta;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ManejadorExcepciones {

    @ExceptionHandler({MascotaNoEncontradaException.class, TipoAtencionNoEncontradoException.class,
            OrdenNoEncontradaException.class, DuenoNoEncontradoException.class})
    public ResponseEntity<ErrorRespuesta> noEncontrado(RuntimeException e) {
        return responder(HttpStatus.NOT_FOUND, "NO_ENCONTRADO", e.getMessage());
    }

    @ExceptionHandler(SaldoInsuficienteException.class)
    public ResponseEntity<ErrorRespuesta> saldoInsuficiente(SaldoInsuficienteException e) {
        return responder(HttpStatus.CONFLICT, "SALDO_INSUFICIENTE", e.getMessage());
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<ErrorRespuesta> reglaNegocio(ReglaNegocioException e) {
        return responder(HttpStatus.CONFLICT, "REGLA_NEGOCIO", e.getMessage());
    }

    @ExceptionHandler(ServicioNoDisponibleException.class)
    public ResponseEntity<ErrorRespuesta> servicioNoDisponible(ServicioNoDisponibleException e) {
        return responder(HttpStatus.SERVICE_UNAVAILABLE, "SERVICIO_NO_DISPONIBLE", e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorRespuesta> peticionInvalida(IllegalArgumentException e) {
        return responder(HttpStatus.BAD_REQUEST, "PETICION_INVALIDA", e.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorRespuesta> jsonInvalido(HttpMessageNotReadableException e) {
        return responder(HttpStatus.BAD_REQUEST, "JSON_INVALIDO", "El cuerpo de la peticion no es un JSON valido");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorRespuesta> integridad(DataIntegrityViolationException e) {
        return responder(HttpStatus.CONFLICT, "VIOLACION_INTEGRIDAD",
                "Dato duplicado o restriccion de base de datos incumplida");
    }

    private ResponseEntity<ErrorRespuesta> responder(HttpStatus estado, String codigo, String mensaje) {
        return ResponseEntity.status(estado).body(new ErrorRespuesta(codigo, mensaje));
    }
}
