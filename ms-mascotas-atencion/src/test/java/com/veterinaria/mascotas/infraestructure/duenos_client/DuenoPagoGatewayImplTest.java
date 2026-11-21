package com.veterinaria.mascotas.infraestructure.duenos_client;

import com.veterinaria.mascotas.domain.model.exception.DuenoNoEncontradoException;
import com.veterinaria.mascotas.domain.model.exception.ReglaNegocioException;
import com.veterinaria.mascotas.domain.model.exception.SaldoInsuficienteException;
import com.veterinaria.mascotas.domain.model.exception.ServicioNoDisponibleException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class DuenoPagoGatewayImplTest {

    private static final String BASE = "http://duenos/duenos";
    private final RestClient.Builder builder = RestClient.builder();
    private final MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
    private final DuenoPagoGatewayImpl gateway = new DuenoPagoGatewayImpl(builder, "http://duenos");

    @Test
    void consultarDueno() {
        server.expect(requestTo(BASE + "/d1")).andExpect(method(HttpMethod.GET)).andRespond(withSuccess(
                "{\"idDueno\":\"d1\",\"nombre\":\"Ana\",\"correo\":\"ana@correo.com\",\"telefono\":\"3001234567\",\"saldo\":0}",
                MediaType.APPLICATION_JSON));
        assertEquals("ana@correo.com", gateway.consultarDueno("d1").correo());
    }

    @Test
    void descontarYReversar() {
        server.expect(requestTo(BASE + "/saldo/descontar")).andExpect(method(HttpMethod.POST)).andRespond(withSuccess(
                "{\"idMovimiento\":\"m1\",\"tipo\":\"DESCUENTO\",\"monto\":105000,\"saldoResultante\":95000.00}",
                MediaType.APPLICATION_JSON));
        server.expect(requestTo(BASE + "/saldo/reversar")).andRespond(withSuccess());
        server.expect(requestTo(BASE + "/saldo/reversar")).andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertEquals(new BigDecimal("95000.00"), gateway.descontar("d1", new BigDecimal("105000"), "ref", "Consulta"));
        assertTrue(gateway.reversar("d1", "ref"));
        assertFalse(gateway.reversar("d1", "otra"));
    }

    @Test
    void erroresSeTraducenAlDominio() {
        server.expect(requestTo(BASE + "/x")).andRespond(withStatus(HttpStatus.NOT_FOUND));
        server.expect(requestTo(BASE + "/saldo/descontar")).andRespond(withStatus(HttpStatus.CONFLICT)
                .body("{\"codigo\":\"SALDO_INSUFICIENTE\"}"));
        server.expect(requestTo(BASE + "/saldo/descontar")).andRespond(withStatus(HttpStatus.CONFLICT)
                .body("{\"codigo\":\"REGLA_NEGOCIO\"}"));
        server.expect(requestTo(BASE + "/saldo/descontar")).andRespond(withServerError());
        server.expect(requestTo(BASE + "/saldo/reversar")).andRespond(withServerError());

        assertThrows(DuenoNoEncontradoException.class, () -> gateway.consultarDueno("x"));
        assertThrows(SaldoInsuficienteException.class, () -> gateway.descontar("d1", BigDecimal.TEN, "r", "d"));
        assertThrows(ReglaNegocioException.class, () -> gateway.descontar("d1", BigDecimal.TEN, "r", "d"));
        assertThrows(ServicioNoDisponibleException.class, () -> gateway.descontar("d1", BigDecimal.TEN, "r", "d"));
        assertThrows(ServicioNoDisponibleException.class, () -> gateway.reversar("d1", "r"));
    }
}
