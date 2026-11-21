package com.veterinaria.mascotas.domain.model;

/**
 * nivelUrgencia: ALTA, MEDIA o BAJA. fuente: GEMINI o REGLAS_LOCALES (si la IA no esta disponible).
 * Es orientacion preliminar: NO reemplaza la valoracion del veterinario.
 */
public record TriajeResultado(String nivelUrgencia, String recomendaciones, String fuente, String advertencia) {

    public static final String ADVERTENCIA =
            "Orientacion preliminar generada automaticamente. No reemplaza la valoracion del medico veterinario.";
}
