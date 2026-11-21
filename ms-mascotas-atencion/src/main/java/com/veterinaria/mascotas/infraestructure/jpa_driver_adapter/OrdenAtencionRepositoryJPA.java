package com.veterinaria.mascotas.infraestructure.jpa_driver_adapter;

import com.veterinaria.mascotas.domain.model.EstadoOrden;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrdenAtencionRepositoryJPA extends JpaRepository<OrdenAtencionEntity, String> {

    @EntityGraph(attributePaths = "items")
    Optional<OrdenAtencionEntity> findFirstByMascotaIdAndEstado(Long mascotaId, EstadoOrden estado);

    @EntityGraph(attributePaths = "items")
    List<OrdenAtencionEntity> findByMascotaIdAndEstadoOrderByFechaAtencionDesc(Long mascotaId, EstadoOrden estado);
}
