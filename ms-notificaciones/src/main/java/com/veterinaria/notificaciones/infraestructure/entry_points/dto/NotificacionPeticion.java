package com.veterinaria.notificaciones.infraestructure.entry_points.dto;

import com.veterinaria.notificaciones.domain.model.NotificationChannel;
import com.veterinaria.notificaciones.domain.model.TipoNotificacion;

/** CRUD: registra una notificacion PENDIENTE. canal: EMAIL o SMS. */
public record NotificacionPeticion(NotificationChannel canal, TipoNotificacion tipo, String destinatario,
                                   String asunto, String mensaje) {
}
