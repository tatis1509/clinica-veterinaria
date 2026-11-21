package com.veterinaria.mascotas.domain.model.gateway;

import com.veterinaria.mascotas.domain.model.TriajeResultado;
import com.veterinaria.mascotas.domain.model.TriajeSolicitud;

/** Puerto hacia la API externa de IA (Gemini). */
public interface TriajeIaGateway {

    TriajeResultado evaluar(TriajeSolicitud solicitud);
}
