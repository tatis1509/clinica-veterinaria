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
public class ItemOrden {

    private Long idItem;
    private Long tipoAtencionId;
    private String nombreServicio;
    private Integer cantidad;
    private BigDecimal precioUnitario;

    /** El subtotal siempre se calcula en el servidor: precio unitario x cantidad. */
    public BigDecimal getSubtotal() {
        return precioUnitario.multiply(BigDecimal.valueOf(cantidad));
    }
}
