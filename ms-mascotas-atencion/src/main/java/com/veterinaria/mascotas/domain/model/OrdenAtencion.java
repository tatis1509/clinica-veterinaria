package com.veterinaria.mascotas.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/** Orden de atencion / cita de una mascota (espejo del Carrito): agrupa servicios y se paga una sola vez. */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class OrdenAtencion {

    private String idOrden;
    private Long mascotaId;
    private String duenoId;
    private EstadoOrden estado;
    /** Identificador que se envia a ms-duenos-pagos para que un mismo cobro no se aplique dos veces. */
    private String referenciaPago;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaAtencion;
    private BigDecimal total;
    private List<ItemOrden> items = new ArrayList<>();

    public static OrdenAtencion nueva(Long mascotaId, String duenoId, String referenciaPago) {
        OrdenAtencion orden = new OrdenAtencion();
        orden.setMascotaId(mascotaId);
        orden.setDuenoId(duenoId);
        orden.setEstado(EstadoOrden.PENDIENTE);
        orden.setReferenciaPago(referenciaPago);
        orden.setFechaCreacion(LocalDateTime.now());
        orden.setTotal(BigDecimal.ZERO);
        return orden;
    }

    public Optional<ItemOrden> buscarItem(Long tipoAtencionId) {
        return items.stream().filter(i -> i.getTipoAtencionId().equals(tipoAtencionId)).findFirst();
    }

    public BigDecimal calcularTotal() {
        return items.stream().map(ItemOrden::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Ej: "Consulta general + Vacunacion" */
    public String describirServicios() {
        return items.stream().map(ItemOrden::getNombreServicio).collect(Collectors.joining(" + "));
    }

    public void marcarRealizada(BigDecimal totalCobrado) {
        this.estado = EstadoOrden.REALIZADA;
        this.total = totalCobrado;
        this.fechaAtencion = LocalDateTime.now();
    }

    public void marcarCancelada() {
        this.estado = EstadoOrden.CANCELADA;
    }
}
