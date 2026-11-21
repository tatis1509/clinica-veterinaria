package com.veterinaria.notificaciones.domain.model.gateway;

import com.veterinaria.notificaciones.domain.model.NotificationResult;

/** Puerto: lo que el negocio necesita para enviar un correo, sin saber que servidor lo hace. */
public interface EmailGateway {

    NotificationResult sendEmail(String email, String subject, String message);
}
