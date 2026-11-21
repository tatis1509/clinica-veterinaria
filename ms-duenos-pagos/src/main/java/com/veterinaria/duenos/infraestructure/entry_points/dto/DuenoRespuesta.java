package com.veterinaria.duenos.infraestructure.entry_points.dto;

import java.math.BigDecimal;

public record DuenoRespuesta(String idDueno, String nombre, String documento, String correo,
                             String telefono, String direccion, BigDecimal saldo) {
}
