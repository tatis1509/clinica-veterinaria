package com.veterinaria.duenos.infraestructure.jpa_driver_adapter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "duenos")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class DuenoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String idDueno;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(length = 20)
    private String documento;

    @Column(nullable = false, unique = true, length = 150)
    private String correo;

    @Column(nullable = false)
    private String clave;

    @Column(nullable = false, length = 15)
    private String telefono;

    private String direccion;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal saldo;
}
