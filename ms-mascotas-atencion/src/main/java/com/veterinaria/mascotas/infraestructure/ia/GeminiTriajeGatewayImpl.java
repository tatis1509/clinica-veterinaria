package com.veterinaria.mascotas.infraestructure.ia;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.veterinaria.mascotas.domain.model.TriajeResultado;
import com.veterinaria.mascotas.domain.model.TriajeSolicitud;
import com.veterinaria.mascotas.domain.model.exception.ServicioNoDisponibleException;
import com.veterinaria.mascotas.domain.model.gateway.TriajeIaGateway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * API externa de IA: Google Gemini (generateContent). La API key va en la variable de entorno GEMINI_API_KEY.
 * Docs: https://ai.google.dev/api/generate-content
 */
@Component
public class GeminiTriajeGatewayImpl implements TriajeIaGateway {

    private final RestClient restClient;
    private final String apiKey;
    private final String modelo;

    public GeminiTriajeGatewayImpl(RestClient.Builder builder,
                                   @Value("${gemini.api-url:https://generativelanguage.googleapis.com}") String apiUrl,
                                   @Value("${gemini.api-key:}") String apiKey,
                                   @Value("${gemini.model:gemini-2.5-flash}") String modelo) {
        this.restClient = builder.baseUrl(apiUrl).build();
        this.apiKey = apiKey;
        this.modelo = modelo;
    }

    @Override
    public TriajeResultado evaluar(TriajeSolicitud s) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new ServicioNoDisponibleException("GEMINI_API_KEY no esta configurada");
        }
        Map<String, Object> cuerpo = Map.of(
                "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt(s))))),
                "generationConfig", Map.of(
                        "temperature", 0.2,
                        "maxOutputTokens", 400,
                        "thinkingConfig", Map.of("thinkingBudget", 0)));
        try {
            GeminiRespuesta respuesta = restClient.post()
                    .uri("/v1beta/models/{modelo}:generateContent", modelo)
                    .header("x-goog-api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(cuerpo)
                    .retrieve()
                    .body(GeminiRespuesta.class);
            return interpretar(extraerTexto(respuesta));
        } catch (RestClientException e) {
            throw new ServicioNoDisponibleException("Gemini no respondio: " + e.getMessage());
        }
    }

    static String prompt(TriajeSolicitud s) {
        return "Eres un asistente de triaje de una clinica veterinaria en Colombia. Con los datos de la mascota "
                + "indica el nivel de urgencia y recomendaciones preliminares breves (maximo 4) para el dueno "
                + "mientras llega a consulta. No diagnostiques ni recetes medicamentos.\n"
                + "Responde EXACTAMENTE en este formato, en espanol:\n"
                + "URGENCIA: ALTA|MEDIA|BAJA\n"
                + "RECOMENDACIONES: <texto>\n\n"
                + "Especie: " + s.especie() + "\n"
                + "Raza: " + (s.raza() == null ? "no indicada" : s.raza()) + "\n"
                + "Edad (anos): " + (s.edad() == null ? "no indicada" : s.edad()) + "\n"
                + "Peso (kg): " + (s.peso() == null ? "no indicado" : s.peso().toPlainString()) + "\n"
                + "Sintomas: " + s.sintomas();
    }

    private static String extraerTexto(GeminiRespuesta r) {
        if (r == null || r.candidates() == null || r.candidates().isEmpty()
                || r.candidates().get(0).content() == null || r.candidates().get(0).content().parts() == null
                || r.candidates().get(0).content().parts().isEmpty()) {
            throw new ServicioNoDisponibleException("Gemini devolvio una respuesta vacia");
        }
        return r.candidates().get(0).content().parts().get(0).text();
    }

    static TriajeResultado interpretar(String texto) {
        String mayus = texto == null ? "" : texto.toUpperCase(Locale.ROOT);
        String nivel = null;
        for (String candidato : List.of("ALTA", "MEDIA", "BAJA")) {
            int i = mayus.indexOf("URGENCIA:");
            if (i >= 0 && mayus.substring(i).split("\n")[0].contains(candidato)) {
                nivel = candidato;
                break;
            }
        }
        if (nivel == null) {
            throw new ServicioNoDisponibleException("Gemini no indico el nivel de urgencia");
        }
        int r = mayus.indexOf("RECOMENDACIONES:");
        String recomendaciones = r >= 0 ? texto.substring(r + "RECOMENDACIONES:".length()).trim() : texto.trim();
        return new TriajeResultado(nivel, recomendaciones, "GEMINI", TriajeResultado.ADVERTENCIA);
    }

    // ---- estructura JSON de la respuesta de Gemini (solo lo que usamos) ----

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GeminiRespuesta(List<Candidato> candidates) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Candidato(Contenido content) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Contenido(List<Parte> parts) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Parte(String text) {
    }
}
