package com.veterinaria.mascotas.infraestructure.jpa_driver_adapter;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MascotaRepositoryJPA extends JpaRepository<MascotaEntity, Long> {

    List<MascotaEntity> findByDuenoIdOrderByNombreAsc(String duenoId);
}
