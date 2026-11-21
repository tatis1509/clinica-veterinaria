package com.veterinaria.notificaciones.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** Registro de un correo o SMS enviado (o que se intento enviar) a un dueno. */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Notificacion {

    private Long idNotificacion;
    private NotificationChannel canal;
    private TipoNotificacion tipo;
    /** Correo (canal EMAIL) o telefono en formato +57... (canal SMS). */
    private String destinatario;
    private String asunto;
    private String mensaje;
    private EstadoNotificacion estado;
    private String idProveedor;
    private Integer intentos;
    private String detalleError;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaEnvio;

    public static Notificacion nueva(NotificationChannel canal, TipoNotificacion tipo, String destinatario,
                                     String asunto, String mensaje) {
        return new Notificacion(null, canal, tipo, destinatario, asunto, mensaje, EstadoNotificacion.PENDIENTE,
                null, 0, null, LocalDateTime.now(), null);
    }

    public void marcarEnviada(String idProveedor) {
        this.estado = EstadoNotificacion.ENVIADA;
        this.idProveedor = idProveedor;
        this.detalleError = null;
        this.fechaEnvio = LocalDateTime.now();
        this.intentos = (intentos == null ? 0 : intentos) + 1;
    }

    public void marcarFallida(String detalleError) {
        this.estado = EstadoNotificacion.FALLIDA;
        this.detalleError = detalleError;
        this.intentos = (intentos == null ? 0 : intentos) + 1;
    }
}
