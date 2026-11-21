package com.veterinaria.mascotas.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Mascota {

    private Long idMascota;
    private String nombre;
    /** Perro, Gato, Ave, Conejo... */
    private String especie;
    private String raza;
    /** Edad en anos. */
    private Integer edad;
    /** Peso en kg. */
    private BigDecimal peso;
    /** Referencia LOGICA al dueno de ms-duenos-pagos (otra base de datos). */
    private String duenoId;
}
