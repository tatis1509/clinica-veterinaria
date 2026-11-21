package com.veterinaria.mascotas.domain.usecase;

import com.veterinaria.mascotas.domain.model.TriajeResultado;
import com.veterinaria.mascotas.domain.model.TriajeSolicitud;
import com.veterinaria.mascotas.domain.model.exception.ServicioNoDisponibleException;
import com.veterinaria.mascotas.domain.model.gateway.TriajeIaGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

/**
 * Triaje con IA (Gemini): sugiere un nivel de urgencia y recomendaciones mientras la mascota llega a consulta.
 * Si la IA no esta disponible responde con reglas locales, asi el servicio nunca se cae por la API externa.
 */
@Slf4j
@RequiredArgsConstructor
public class TriajeUseCase {

    private static final List<String> SIGNOS_ALTA = List.of("convulsion", "sangr", "no respira",
            "dificultad para respirar", "ahog", "envenen", "intoxic", "veneno", "atropell", "fractura",
            "inconsciente", "desmay", "abdomen hinchado", "no orina", "paralisis");
    private static final List<String> SIGNOS_MEDIA = List.of("vomit", "diarrea", "fiebre", "decaimiento",
            "decaido", "cojea", "no come", "tos", "estornud", "picazon", "herida", "dolor", "letargo");

    private final TriajeIaGateway triajeIaGateway;

    public TriajeResultado evaluar(TriajeSolicitud solicitud) {
        if (solicitud == null || isBlank(solicitud.especie()) || isBlank(solicitud.sintomas())) {
            throw new IllegalArgumentException("La especie y los sintomas son obligatorios");
        }
        try {
            return triajeIaGateway.evaluar(solicitud);
        } catch (ServicioNoDisponibleException e) {
            log.warn("IA no disponible, se usa triaje por reglas: {}", e.getMessage());
            return evaluarConReglas(solicitud);
        }
    }

    /** Triaje de respaldo por palabras clave. */
    public TriajeResultado evaluarConReglas(TriajeSolicitud solicitud) {
        String sintomas = normalizar(solicitud.sintomas());
        if (SIGNOS_ALTA.stream().anyMatch(sintomas::contains)) {
            return new TriajeResultado("ALTA",
                    "Acuda de inmediato a urgencias veterinarias. Mantenga a la mascota quieta y abrigada, "
                            + "no le de alimentos ni medicamentos sin indicacion.",
                    "REGLAS_LOCALES", TriajeResultado.ADVERTENCIA);
        }
        if (SIGNOS_MEDIA.stream().anyMatch(sintomas::contains)) {
            return new TriajeResultado("MEDIA",
                    "Agende una consulta en las proximas 24 horas. Ofrezca agua fresca en pequenas cantidades, "
                            + "observe si los sintomas empeoran y no la automedique.",
                    "REGLAS_LOCALES", TriajeResultado.ADVERTENCIA);
        }
        return new TriajeResultado("BAJA",
                "Puede agendar una consulta general. Mantenga su alimentacion e hidratacion habitual "
                        + "y vigile cualquier cambio de comportamiento.",
                "REGLAS_LOCALES", TriajeResultado.ADVERTENCIA);
    }

    private static String normalizar(String texto) {
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
    }

    private static boolean isBlank(String valor) {
        return valor == null || valor.isBlank();
    }
}
