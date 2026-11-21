package com.veterinaria.mascotas.infraestructure.jpa_driver_adapter;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TipoAtencionRepositoryJPA extends JpaRepository<TipoAtencionEntity, Long> {
}
