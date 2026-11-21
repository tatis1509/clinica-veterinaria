package com.veterinaria.notificaciones.infraestructure.entry_points.dto;

import com.veterinaria.notificaciones.domain.model.TipoNotificacion;

public record SmsPeticion(String telefono, String mensaje, TipoNotificacion tipo) {
}
