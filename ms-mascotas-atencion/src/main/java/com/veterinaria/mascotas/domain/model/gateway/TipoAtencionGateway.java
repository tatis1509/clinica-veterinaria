package com.veterinaria.mascotas.domain.model.gateway;

import com.veterinaria.mascotas.domain.model.TipoAtencion;

import java.util.List;
import java.util.Optional;

public interface TipoAtencionGateway {

    TipoAtencion guardar(TipoAtencion tipoAtencion);

    Optional<TipoAtencion> buscarPorId(Long idTipoAtencion);

    List<TipoAtencion> listar();

    void eliminar(Long idTipoAtencion);
}
