package com.veterinaria.mascotas.infraestructure.notificaciones_client;

import com.veterinaria.mascotas.domain.model.DuenoInfo;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class NotificacionGatewayImplTest {

    private final RestClient.Builder builder = RestClient.builder();
    private final MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
    // Executor sincrono para la prueba (en produccion usa hilos virtuales)
    private final NotificacionGatewayImpl gateway = new NotificacionGatewayImpl(builder, "http://notif", Runnable::run);
    private final DuenoInfo ana = new DuenoInfo("d1", "Ana", "ana@correo.com", "3001234567");

    @Test
    void enviaElResumenPorCorreo() {
        server.expect(requestTo("http://notif/notificaciones/enviar-email"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andRespond(withSuccess());
        gateway.notificarDueno(ana, "Resumen", "Toby fue atendido");
        server.verify();
    }

    @Test
    void siFallaNoPropagaElError() {
        server.expect(requestTo("http://notif/notificaciones/enviar-email")).andRespond(withServerError());
        gateway.notificarDueno(ana, "Resumen", "Toby fue atendido");
        server.verify();
    }

    @Test
    void constructorDeProduccion() {
        new NotificacionGatewayImpl(RestClient.builder(), "http://notif");
    }
}
