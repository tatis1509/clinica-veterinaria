package com.veterinaria.mascotas.infraestructure.jpa_driver_adapter;

import com.veterinaria.mascotas.domain.model.EstadoOrden;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ordenes_atencion", indexes = @Index(name = "idx_ordenes_mascota", columnList = "mascota_id"))
@Getter
@Setter
@NoArgsConstructor
public class OrdenAtencionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String idOrden;

    @Column(nullable = false)
    private Long mascotaId;

    @Column(nullable = false)
    private String duenoId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private EstadoOrden estado;

    @Column(nullable = false)
    private String referenciaPago;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    private LocalDateTime fechaAtencion;

    @Column(precision = 15, scale = 2)
    private BigDecimal total;

    @OneToMany(mappedBy = "orden", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemOrdenEntity> items = new ArrayList<>();
}
