package com.veterinaria.mascotas.domain.usecase;

import com.veterinaria.mascotas.domain.model.DuenoInfo;
import com.veterinaria.mascotas.domain.model.EstadoOrden;
import com.veterinaria.mascotas.domain.model.ItemOrden;
import com.veterinaria.mascotas.domain.model.Mascota;
import com.veterinaria.mascotas.domain.model.OrdenAtencion;
import com.veterinaria.mascotas.domain.model.ResultadoAtencion;
import com.veterinaria.mascotas.domain.model.TipoAtencion;
import com.veterinaria.mascotas.domain.model.exception.MascotaNoEncontradaException;
import com.veterinaria.mascotas.domain.model.exception.OrdenNoEncontradaException;
import com.veterinaria.mascotas.domain.model.exception.ReglaNegocioException;
import com.veterinaria.mascotas.domain.model.exception.ServicioNoDisponibleException;
import com.veterinaria.mascotas.domain.model.exception.TipoAtencionNoEncontradoException;
import com.veterinaria.mascotas.domain.model.gateway.DuenoPagoGateway;
import com.veterinaria.mascotas.domain.model.gateway.MascotaGateway;
import com.veterinaria.mascotas.domain.model.gateway.NotificacionGateway;
import com.veterinaria.mascotas.domain.model.gateway.OrdenAtencionGateway;
import com.veterinaria.mascotas.domain.model.gateway.TipoAtencionGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Orden de atencion (espejo del CarritoUseCase): se agregan servicios a la orden PENDIENTE de la mascota,
 * se confirma (cobra en ms-duenos-pagos y notifica por ms-notificaciones) o se cancela (con reverso).
 */
@Slf4j
@RequiredArgsConstructor
public class AtencionUseCase {

    private final OrdenAtencionGateway ordenGateway;
    private final MascotaGateway mascotaGateway;
    private final TipoAtencionGateway tipoAtencionGateway;
    private final DuenoPagoGateway duenoPagoGateway;
    private final NotificacionGateway notificacionGateway;

    /** API extra: agrega un servicio (Consulta, Vacuna...) a la orden PENDIENTE de la mascota y suma el total. */
    public OrdenAtencion agregarServicio(Long mascotaId, Long tipoAtencionId, Integer cantidad) {
        int cant = cantidad == null ? 1 : cantidad;
        if (cant <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero");
        }
        if (tipoAtencionId == null) {
            throw new IllegalArgumentException("El tipoAtencionId es obligatorio");
        }
        Mascota mascota = buscarMascota(mascotaId);
        TipoAtencion tipo = buscarTipo(tipoAtencionId);

        // Si la mascota no tiene una orden abierta, se crea (como crear el carrito)
        OrdenAtencion orden = ordenGateway.buscarPendientePorMascota(mascotaId)
                .orElseGet(() -> OrdenAtencion.nueva(mascotaId, mascota.getDuenoId(), UUID.randomUUID().toString()));

        ItemOrden item = orden.buscarItem(tipoAtencionId).orElse(null);
        if (item == null) {
            orden.getItems().add(new ItemOrden(null, tipo.getIdTipoAtencion(), tipo.getNombre(), cant, tipo.getPrecio()));
        } else {
            item.setCantidad(item.getCantidad() + cant);
            item.setPrecioUnitario(tipo.getPrecio());
        }
        orden.setTotal(orden.calcularTotal());
        return ordenGateway.guardar(orden);
    }

    public OrdenAtencion consultarOrden(String idOrden) {
        if (idOrden == null || idOrden.isBlank()) {
            throw new IllegalArgumentException("El id de la orden es obligatorio");
        }
        return ordenGateway.buscarPorId(idOrden).orElseThrow(() -> new OrdenNoEncontradaException(idOrden));
    }

    /** API extra: cobra el total al dueno, marca la atencion REALIZADA y le envia el resumen al dueno. */
    public ResultadoAtencion confirmarAtencion(String idOrden) {
        OrdenAtencion orden = consultarOrden(idOrden);
        if (orden.getEstado() != EstadoOrden.PENDIENTE) {
            throw new ReglaNegocioException("La orden ya esta " + orden.getEstado() + "; no se puede volver a cobrar");
        }
        if (orden.getItems().isEmpty()) {
            throw new IllegalArgumentException("La orden no tiene servicios");
        }
        Mascota mascota = buscarMascota(orden.getMascotaId());

        // 1. Precios vigentes del servidor
        for (ItemOrden item : orden.getItems()) {
            item.setPrecioUnitario(buscarTipo(item.getTipoAtencionId()).getPrecio());
        }
        BigDecimal total = orden.calcularTotal();

        // 2. El dueno existe (y sus datos para notificarle)
        DuenoInfo dueno = duenoPagoGateway.consultarDueno(orden.getDuenoId());

        // 3. Cobro en ms-duenos-pagos (idempotente por referenciaPago; lanza SaldoInsuficienteException)
        String descripcion = "Atencion de " + mascota.getNombre() + ": " + orden.describirServicios();
        BigDecimal saldoRestante;
        try {
            saldoRestante = duenoPagoGateway.descontar(orden.getDuenoId(), total, orden.getReferenciaPago(), descripcion);
        } catch (ServicioNoDisponibleException e) {
            compensar(orden); // no sabemos si alcanzo a cobrar: ante la duda, se reversa
            throw e;
        }

        // 4. Confirmacion local
        OrdenAtencion realizada;
        try {
            orden.marcarRealizada(total);
            realizada = ordenGateway.guardar(orden);
        } catch (RuntimeException e) {
            compensar(orden);
            throw e;
        }

        // 5. Resumen al dueno (asincrono, no afecta el tiempo de respuesta ni deshace el cobro si falla)
        notificacionGateway.notificarDueno(dueno, "Resumen de atencion - " + mascota.getNombre(),
                "Hola " + dueno.nombre() + ", " + mascota.getNombre() + " fue atendido(a) hoy. Servicios: "
                        + orden.describirServicios() + ". Total cobrado: $" + total.toPlainString()
                        + ". Saldo disponible: $" + saldoRestante.toPlainString() + ". Gracias por confiar en nosotros.");

        return new ResultadoAtencion(realizada, saldoRestante);
    }

    /** Cancela la orden. Si ya estaba pagada (REALIZADA) le devuelve el dinero al dueno. */
    public OrdenAtencion cancelarAtencion(String idOrden) {
        OrdenAtencion orden = consultarOrden(idOrden);
        if (orden.getEstado() == EstadoOrden.CANCELADA) {
            throw new ReglaNegocioException("La orden ya esta cancelada");
        }
        if (orden.getEstado() == EstadoOrden.REALIZADA) {
            duenoPagoGateway.reversar(orden.getDuenoId(), orden.getReferenciaPago());
        }
        orden.marcarCancelada();
        return ordenGateway.guardar(orden);
    }

    /** API extra: historial medico / servicios realizados a la mascota. */
    public List<OrdenAtencion> historialMascota(Long mascotaId) {
        buscarMascota(mascotaId);
        return ordenGateway.listarRealizadasPorMascota(mascotaId);
    }

    private void compensar(OrdenAtencion orden) {
        try {
            if (duenoPagoGateway.reversar(orden.getDuenoId(), orden.getReferenciaPago())) {
                ordenGateway.actualizarReferenciaPago(orden.getIdOrden(), UUID.randomUUID().toString());
            }
        } catch (RuntimeException ex) {
            log.error("No se pudo reversar el cobro de la orden {} (referencia {}). Requiere revision manual.",
                    orden.getIdOrden(), orden.getReferenciaPago(), ex);
        }
    }

    private Mascota buscarMascota(Long mascotaId) {
        if (mascotaId == null) {
            throw new IllegalArgumentException("El mascotaId es obligatorio");
        }
        return mascotaGateway.buscarPorId(mascotaId).orElseThrow(() -> new MascotaNoEncontradaException(mascotaId));
    }

    private TipoAtencion buscarTipo(Long tipoAtencionId) {
        return tipoAtencionGateway.buscarPorId(tipoAtencionId)
                .orElseThrow(() -> new TipoAtencionNoEncontradoException(tipoAtencionId));
    }
}
