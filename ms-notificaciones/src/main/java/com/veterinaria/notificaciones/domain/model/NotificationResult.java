package com.veterinaria.notificaciones.domain.model;

import java.time.ZonedDateTime;

/** Respuesta del proveedor (Twilio o el servidor de correo) ya traducida a nuestro modelo. */
public record NotificationResult(
        NotificationChannel channel,
        String status,
        String providerId,
        String recipient,
        ZonedDateTime sentAt) {
}
