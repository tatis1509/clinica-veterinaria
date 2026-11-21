package com.veterinaria.mascotas.infraestructure.notificaciones_client;

import com.veterinaria.mascotas.domain.model.DuenoInfo;
import com.veterinaria.mascotas.domain.model.gateway.NotificacionGateway;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Adaptador REST hacia ms-notificaciones. Envia en segundo plano (hilos virtuales) para que confirmar
 * una atencion responda en menos de 500 ms aunque el correo tarde; si falla solo queda en el log
 * (y en ms-notificaciones queda FALLIDA para reintentarla).
 */
@Slf4j
@Component
public class NotificacionGatewayImpl implements NotificacionGateway {

    private final RestClient restClient;
    private final Executor executor;

    @Autowired
    public NotificacionGatewayImpl(RestClient.Builder builder,
                                   @Value("${notificaciones.service.url}") String notificacionesUrl) {
        this(builder, notificacionesUrl, Executors.newVirtualThreadPerTaskExecutor());
    }

    NotificacionGatewayImpl(RestClient.Builder builder, String notificacionesUrl, Executor executor) {
        this.restClient = builder.baseUrl(notificacionesUrl + "/notificaciones").build();
        this.executor = executor;
    }

    @Override
    public void notificarDueno(DuenoInfo dueno, String asunto, String mensaje) {
        executor.execute(() -> {
            try {
                restClient.post().uri("/enviar-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(new EmailPeticion(dueno.correo(), asunto, mensaje, "COMPROBANTE_ATENCION"))
                        .retrieve()
                        .toBodilessEntity();
            } catch (RuntimeException e) {
                log.warn("No se pudo notificar al dueno {}: {}", dueno.idDueno(), e.getMessage());
            }
        });
    }

    public record EmailPeticion(String destinatario, String asunto, String mensaje, String tipo) {
    }
}
