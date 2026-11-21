package com.veterinaria.duenos.infraestructure.jpa_driver_adapter;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface DuenoRepositoryJPA extends JpaRepository<DuenoEntity, String> {

    Optional<DuenoEntity> findByCorreo(String correo);

    /** SELECT ... FOR UPDATE: serializa las operaciones concurrentes sobre el monedero de un dueno. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from DuenoEntity d where d.idDueno = :id")
    Optional<DuenoEntity> findByIdForUpdate(@Param("id") String id);
}
