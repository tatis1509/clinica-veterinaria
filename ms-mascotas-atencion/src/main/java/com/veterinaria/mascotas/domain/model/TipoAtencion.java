package com.veterinaria.mascotas.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/** Servicio que ofrece la clinica: consulta general, vacunacion, desparasitacion, bano, cirugia menor... */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TipoAtencion {

    private Long idTipoAtencion;
    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private Integer duracionMinutos;
}
