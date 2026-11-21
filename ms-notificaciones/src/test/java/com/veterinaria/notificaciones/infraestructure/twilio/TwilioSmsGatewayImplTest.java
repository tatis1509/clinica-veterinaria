package com.veterinaria.notificaciones.infraestructure.twilio;

import com.veterinaria.notificaciones.domain.model.NotificationResult;
import com.veterinaria.notificaciones.domain.model.exception.ProveedorNotificacionException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class TwilioSmsGatewayImplTest {

    private static final String URL = "https://api.twilio.com/2010-04-01/Accounts/AC1/Messages.json";
    private static final String OK = """
            {"sid":"SM123","status":"queued","to":"+573001234567","date_created":"Thu, 30 Jul 2015 20:12:31 +0000"}""";

    private final RestClient.Builder builder = RestClient.builder();
    private final MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();

    private TwilioSmsGatewayImpl gateway(String sid) {
        return new TwilioSmsGatewayImpl(builder, sid, "token", "+15550001111", "https://api.twilio.com",
                "sms_order_confirmation");
    }

    @Test
    void enviaElSms() {
        TwilioSmsGatewayImpl gateway = gateway("AC1");
        server.expect(requestTo(URL)).andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(OK, MediaType.APPLICATION_JSON));

        NotificationResult r = gateway.sendSms("+573001234567", "Cita confirmada");

        assertEquals("SM123", r.providerId());
        assertEquals("queued", r.status());
        server.verify();
    }

    @Test
    void cuentaTrialReintentaConPlantilla() {
        TwilioSmsGatewayImpl gateway = gateway("AC1");
        server.expect(requestTo(URL)).andRespond(withBadRequest()
                .body("{\"code\":572006}").contentType(MediaType.APPLICATION_JSON));
        server.expect(requestTo(URL)).andRespond(withSuccess(
                "{\"sid\":\"SM9\",\"status\":null,\"date_created\":\"fecha-rara\"}", MediaType.APPLICATION_JSON));

        assertEquals("SM9", gateway.sendSms("+573001234567", "Hola").providerId());
    }

    @Test
    void erroresDelProveedor() {
        TwilioSmsGatewayImpl gateway = gateway("AC1");
        server.expect(requestTo(URL)).andRespond(withBadRequest().body("{\"code\":21211}"));
        assertThrows(ProveedorNotificacionException.class, () -> gateway.sendSms("+573001234567", "Hola"));

        server.reset();
        server.expect(requestTo(URL)).andRespond(withServerError());
        assertThrows(ProveedorNotificacionException.class, () -> gateway.sendSms("+573001234567", "Hola"));
    }

    @Test
    void sinCredencialesNoEnvia() {
        assertThrows(ProveedorNotificacionException.class, () -> gateway("").sendSms("+573001234567", "Hola"));
    }
}
