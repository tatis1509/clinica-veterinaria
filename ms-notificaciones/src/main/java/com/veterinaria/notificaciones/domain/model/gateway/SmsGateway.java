package com.veterinaria.notificaciones.domain.model.gateway;

import com.veterinaria.notificaciones.domain.model.NotificationResult;

/** Puerto: lo que el negocio necesita para enviar un SMS, sin saber que proveedor lo hace. */
public interface SmsGateway {

    /** El telefono llega ya en formato internacional (+573001234567). */
    NotificationResult sendSms(String phoneNumber, String message);
}
