package com.veterinaria.mascotas.domain.model.gateway;

import com.veterinaria.mascotas.domain.model.DuenoInfo;

/** Puerto hacia ms-notificaciones. Es "best effort": si falla no se deshace la atencion. */
public interface NotificacionGateway {

    void notificarDueno(DuenoInfo dueno, String asunto, String mensaje);
}
