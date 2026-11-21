package com.veterinaria.notificaciones.domain.model.gateway;

import com.veterinaria.notificaciones.domain.model.Notificacion;

import java.util.List;
import java.util.Optional;

/** Puerto de persistencia del historial de notificaciones. */
public interface NotificacionGateway {

    Notificacion guardar(Notificacion notificacion);

    Optional<Notificacion> buscarPorId(Long id);

    List<Notificacion> listar();

    List<Notificacion> listarPorDestinatario(String destinatario);

    void eliminar(Long id);
}
