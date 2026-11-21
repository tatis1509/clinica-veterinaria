package com.veterinaria.duenos.infraestructure.jpa_driver_adapter;

import com.veterinaria.duenos.domain.model.TipoMovimiento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MovimientoSaldoRepositoryJPA extends JpaRepository<MovimientoSaldoEntity, String> {

    Optional<MovimientoSaldoEntity> findByReferenciaAndTipo(String referencia, TipoMovimiento tipo);

    List<MovimientoSaldoEntity> findByIdDuenoOrderByFechaDesc(String idDueno);
}
