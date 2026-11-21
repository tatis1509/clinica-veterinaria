package com.veterinaria.mascotas.domain.model.gateway;

import com.veterinaria.mascotas.domain.model.OrdenAtencion;

import java.util.List;
import java.util.Optional;

public interface OrdenAtencionGateway {

    OrdenAtencion guardar(OrdenAtencion orden);

    Optional<OrdenAtencion> buscarPorId(String idOrden);

    Optional<OrdenAtencion> buscarPendientePorMascota(Long mascotaId);

    /** Atenciones REALIZADAS de la mascota, de la mas reciente a la mas antigua. */
    List<OrdenAtencion> listarRealizadasPorMascota(Long mascotaId);

    void actualizarReferenciaPago(String idOrden, String nuevaReferencia);
}
