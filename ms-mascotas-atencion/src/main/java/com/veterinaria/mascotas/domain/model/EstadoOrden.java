package com.veterinaria.mascotas.domain.model;

public enum EstadoOrden {
    /** Se le estan agregando servicios (equivale al carrito ACTIVO). */
    PENDIENTE,
    /** Cobrada y atendida. */
    REALIZADA,
    /** Cancelada (si estaba pagada se le devuelve el dinero al dueno). */
    CANCELADA
}
