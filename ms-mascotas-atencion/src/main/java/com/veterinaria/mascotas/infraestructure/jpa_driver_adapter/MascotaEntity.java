package com.veterinaria.mascotas.infraestructure.jpa_driver_adapter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "mascotas", indexes = @Index(name = "idx_mascotas_dueno", columnList = "dueno_id"))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MascotaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idMascota;

    @Column(nullable = false, length = 80)
    private String nombre;

    @Column(nullable = false, length = 40)
    private String especie;

    @Column(length = 60)
    private String raza;

    private Integer edad;

    @Column(precision = 6, scale = 2)
    private BigDecimal peso;

    /** Referencia LOGICA al dueno de ms-duenos-pagos (otra base de datos): no es clave foranea fisica. */
    @Column(nullable = false)
    private String duenoId;
}
