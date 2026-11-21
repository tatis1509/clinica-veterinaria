package com.veterinaria.mascotas.infraestructure.jpa_driver_adapter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/** Servicio dentro de una orden. Guarda el nombre y precio del momento (no depende del tipo de atencion). */
@Entity
@Table(name = "items_orden",
        uniqueConstraints = @UniqueConstraint(name = "uq_item_orden_tipo", columnNames = {"id_orden", "tipo_atencion_id"}))
@Getter
@Setter
@NoArgsConstructor
public class ItemOrdenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idItem;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_orden", nullable = false)
    private OrdenAtencionEntity orden;

    @Column(nullable = false)
    private Long tipoAtencionId;

    @Column(nullable = false, length = 80)
    private String nombreServicio;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal precioUnitario;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal subtotal;
}
