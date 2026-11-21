package com.veterinaria.mascotas.infraestructure.ia;

import com.veterinaria.mascotas.domain.model.TriajeResultado;
import com.veterinaria.mascotas.domain.model.TriajeSolicitud;
import com.veterinaria.mascotas.domain.model.exception.ServicioNoDisponibleException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class GeminiTriajeGatewayImplTest {

    private static final String URL = "https://gemini.test/v1beta/models/gemini-2.5-flash:generateContent";
    private final RestClient.Builder builder = RestClient.builder();
    private final MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
    private final TriajeSolicitud solicitud = new TriajeSolicitud("Perro", "Labrador", 5, new BigDecimal("25"),
            "vomito y decaimiento");

    private GeminiTriajeGatewayImpl gateway(String key) {
        return new GeminiTriajeGatewayImpl(builder, "https://gemini.test", key, "gemini-2.5-flash");
    }

    @Test
    void interpretaLaRespuestaDeGemini() {
        GeminiTriajeGatewayImpl gateway = gateway("clave");
        server.expect(requestTo(URL)).andExpect(method(HttpMethod.POST)).andExpect(header("x-goog-api-key", "clave"))
                .andRespond(withSuccess("""
                        {"candidates":[{"content":{"parts":[{"text":"URGENCIA: MEDIA\\nRECOMENDACIONES: Ofrezca agua y consulte hoy."}]}}]}
                        """, MediaType.APPLICATION_JSON));

        TriajeResultado r = gateway.evaluar(solicitud);

        assertEquals("MEDIA", r.nivelUrgencia());
        assertEquals("Ofrezca agua y consulte hoy.", r.recomendaciones());
        assertEquals("GEMINI", r.fuente());
    }

    @Test
    void erroresDeGeminiSeTraducenAServicioNoDisponible() {
        GeminiTriajeGatewayImpl gateway = gateway("clave");
        server.expect(requestTo(URL)).andRespond(withServerError());
        server.expect(requestTo(URL)).andRespond(withSuccess("{\"candidates\":[]}", MediaType.APPLICATION_JSON));

        assertThrows(ServicioNoDisponibleException.class, () -> gateway.evaluar(solicitud));
        assertThrows(ServicioNoDisponibleException.class, () -> gateway.evaluar(solicitud));
        assertThrows(ServicioNoDisponibleException.class, () -> gateway("").evaluar(solicitud));
        assertThrows(ServicioNoDisponibleException.class, () -> GeminiTriajeGatewayImpl.interpretar("no se"));
    }

    @Test
    void elPromptIncluyeLosDatosDeLaMascota() {
        String prompt = GeminiTriajeGatewayImpl.prompt(new TriajeSolicitud("Gato", null, null, null, "tos"));
        assertTrue(prompt.contains("Especie: Gato"));
        assertTrue(prompt.contains("Sintomas: tos"));
        assertEquals("BAJA", GeminiTriajeGatewayImpl.interpretar("urgencia: baja").nivelUrgencia());
    }
}
