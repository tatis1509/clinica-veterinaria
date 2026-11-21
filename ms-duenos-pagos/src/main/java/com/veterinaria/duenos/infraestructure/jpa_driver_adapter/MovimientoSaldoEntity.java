package com.veterinaria.duenos.infraestructure.jpa_driver_adapter;

import com.veterinaria.duenos.domain.model.TipoMovimiento;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Historial del monedero. La restriccion unica (referencia, tipo) impide cobrar dos veces la misma atencion. */
@Entity
@Table(name = "movimientos_saldo",
        uniqueConstraints = @UniqueConstraint(name = "uq_movimiento_referencia_tipo",
                columnNames = {"referencia", "tipo"}),
        indexes = @Index(name = "idx_movimientos_dueno", columnList = "id_dueno"))
@Getter
@Setter
@NoArgsConstructor
public class MovimientoSaldoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String idMovimiento;

    @Column(nullable = false)
    private String idDueno;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoMovimiento tipo;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal monto;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal saldoResultante;

    @Column(nullable = false)
    private String referencia;

    private String descripcion;

    @Column(nullable = false)
    private LocalDateTime fecha;
}
