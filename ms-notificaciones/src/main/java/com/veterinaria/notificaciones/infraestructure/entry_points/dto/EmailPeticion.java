package com.veterinaria.notificaciones.infraestructure.entry_points.dto;

import com.veterinaria.notificaciones.domain.model.TipoNotificacion;

public record EmailPeticion(String destinatario, String asunto, String mensaje, TipoNotificacion tipo) {
}
