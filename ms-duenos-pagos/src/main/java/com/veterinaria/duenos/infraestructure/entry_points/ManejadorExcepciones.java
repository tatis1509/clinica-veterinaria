package com.veterinaria.duenos.infraestructure.entry_points;

import com.veterinaria.duenos.domain.model.exception.CredencialesInvalidasException;
import com.veterinaria.duenos.domain.model.exception.DuenoNoEncontradoException;
import com.veterinaria.duenos.domain.model.exception.MovimientoNoEncontradoException;
import com.veterinaria.duenos.domain.model.exception.ReglaNegocioException;
import com.veterinaria.duenos.domain.model.exception.SaldoInsuficienteException;
import com.veterinaria.duenos.infraestructure.entry_points.dto.ErrorRespuesta;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ManejadorExcepciones {

    @ExceptionHandler(DuenoNoEncontradoException.class)
    public ResponseEntity<ErrorRespuesta> duenoNoEncontrado(DuenoNoEncontradoException e) {
        return responder(HttpStatus.NOT_FOUND, "DUENO_NO_ENCONTRADO", e.getMessage());
    }

    @ExceptionHandler(MovimientoNoEncontradoException.class)
    public ResponseEntity<ErrorRespuesta> movimientoNoEncontrado(MovimientoNoEncontradoException e) {
        return responder(HttpStatus.NOT_FOUND, "MOVIMIENTO_NO_ENCONTRADO", e.getMessage());
    }

    @ExceptionHandler(SaldoInsuficienteException.class)
    public ResponseEntity<ErrorRespuesta> saldoInsuficiente(SaldoInsuficienteException e) {
        return responder(HttpStatus.CONFLICT, "SALDO_INSUFICIENTE", e.getMessage());
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<ErrorRespuesta> reglaNegocio(ReglaNegocioException e) {
        return responder(HttpStatus.CONFLICT, "REGLA_NEGOCIO", e.getMessage());
    }

    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<ErrorRespuesta> credenciales(CredencialesInvalidasException e) {
        return responder(HttpStatus.UNAUTHORIZED, "CREDENCIALES_INVALIDAS", e.getMessage());
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
                "Dato duplicado o restriccion de base de datos incumplida (por ejemplo, correo ya registrado)");
    }

    private ResponseEntity<ErrorRespuesta> responder(HttpStatus estado, String codigo, String mensaje) {
        return ResponseEntity.status(estado).body(new ErrorRespuesta(codigo, mensaje));
    }
}
