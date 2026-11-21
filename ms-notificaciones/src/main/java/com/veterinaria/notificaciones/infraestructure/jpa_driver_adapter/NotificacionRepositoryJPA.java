package com.veterinaria.notificaciones.infraestructure.jpa_driver_adapter;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificacionRepositoryJPA extends JpaRepository<NotificacionEntity, Long> {

    List<NotificacionEntity> findByDestinatarioOrderByFechaCreacionDesc(String destinatario);
}
