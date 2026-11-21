package com.veterinaria.mascotas.domain.usecase;

import com.veterinaria.mascotas.domain.model.DuenoInfo;
import com.veterinaria.mascotas.domain.model.EstadoOrden;
import com.veterinaria.mascotas.domain.model.ItemOrden;
import com.veterinaria.mascotas.domain.model.Mascota;
import com.veterinaria.mascotas.domain.model.OrdenAtencion;
import com.veterinaria.mascotas.domain.model.ResultadoAtencion;
import com.veterinaria.mascotas.domain.model.TipoAtencion;
import com.veterinaria.mascotas.domain.model.exception.OrdenNoEncontradaException;
import com.veterinaria.mascotas.domain.model.exception.ReglaNegocioException;
import com.veterinaria.mascotas.domain.model.exception.ServicioNoDisponibleException;
import com.veterinaria.mascotas.domain.model.gateway.DuenoPagoGateway;
import com.veterinaria.mascotas.domain.model.gateway.MascotaGateway;
import com.veterinaria.mascotas.domain.model.gateway.NotificacionGateway;
import com.veterinaria.mascotas.domain.model.gateway.OrdenAtencionGateway;
import com.veterinaria.mascotas.domain.model.gateway.TipoAtencionGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AtencionUseCaseTest {

    @Mock
    private OrdenAtencionGateway ordenGateway;
    @Mock
    private MascotaGateway mascotaGateway;
    @Mock
    private TipoAtencionGateway tipoGateway;
    @Mock
    private DuenoPagoGateway duenoPagoGateway;
    @Mock
    private NotificacionGateway notificacionGateway;
    @InjectMocks
    private AtencionUseCase useCase;

    private final Mascota toby = new Mascota(1L, "Toby", "Perro", "Labrador", 4, new BigDecimal("25"), "d1");
    private final TipoAtencion consulta = new TipoAtencion(1L, "Consulta general", "", new BigDecimal("45000"), 30);
    private final TipoAtencion vacuna = new TipoAtencion(2L, "Vacunacion", "", new BigDecimal("60000"), 15);
    private final DuenoInfo ana = new DuenoInfo("d1", "Ana", "ana@correo.com", "3001234567");

    private OrdenAtencion ordenPendiente() {
        OrdenAtencion orden = OrdenAtencion.nueva(1L, "d1", "ref-1");
        orden.setIdOrden("o1");
        orden.setItems(new ArrayList<>(List.of(
                new ItemOrden(1L, 1L, "Consulta general", 1, new BigDecimal("45000")),
                new ItemOrden(2L, 2L, "Vacunacion", 1, new BigDecimal("60000")))));
        return orden;
    }

    private void guardarDevuelveLoMismo() {
        when(ordenGateway.guardar(any(OrdenAtencion.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void agregarServicioCreaLaOrdenYSumaElTotal() {
        when(mascotaGateway.buscarPorId(1L)).thenReturn(Optional.of(toby));
        when(tipoGateway.buscarPorId(1L)).thenReturn(Optional.of(consulta));
        when(ordenGateway.buscarPendientePorMascota(1L)).thenReturn(Optional.empty());
        guardarDevuelveLoMismo();

        OrdenAtencion orden = useCase.agregarServicio(1L, 1L, null);

        assertEquals(EstadoOrden.PENDIENTE, orden.getEstado());
        assertEquals("d1", orden.getDuenoId());
        assertEquals(new BigDecimal("45000"), orden.getTotal());
    }

    @Test
    void agregarElMismoServicioSumaLaCantidad() {
        OrdenAtencion existente = ordenPendiente();
        when(mascotaGateway.buscarPorId(1L)).thenReturn(Optional.of(toby));
        when(tipoGateway.buscarPorId(2L)).thenReturn(Optional.of(vacuna));
        when(ordenGateway.buscarPendientePorMascota(1L)).thenReturn(Optional.of(existente));
        guardarDevuelveLoMismo();

        OrdenAtencion orden = useCase.agregarServicio(1L, 2L, 2);

        assertEquals(3, orden.buscarItem(2L).orElseThrow().getCantidad());
        assertEquals(new BigDecimal("225000"), orden.getTotal());
    }

    @Test
    void confirmarAtencionCobraYNotifica() {
        when(ordenGateway.buscarPorId("o1")).thenReturn(Optional.of(ordenPendiente()));
        when(mascotaGateway.buscarPorId(1L)).thenReturn(Optional.of(toby));
        when(tipoGateway.buscarPorId(1L)).thenReturn(Optional.of(consulta));
        when(tipoGateway.buscarPorId(2L)).thenReturn(Optional.of(vacuna));
        when(duenoPagoGateway.consultarDueno("d1")).thenReturn(ana);
        when(duenoPagoGateway.descontar(eq("d1"), eq(new BigDecimal("105000")), eq("ref-1"), anyString()))
                .thenReturn(new BigDecimal("95000.00"));
        guardarDevuelveLoMismo();

        ResultadoAtencion r = useCase.confirmarAtencion("o1");

        assertEquals(EstadoOrden.REALIZADA, r.orden().getEstado());
        assertEquals(new BigDecimal("105000"), r.orden().getTotal());
        assertEquals(new BigDecimal("95000.00"), r.saldoRestante());
        verify(notificacionGateway).notificarDueno(eq(ana), anyString(), anyString());
    }

    @Test
    void siPagosNoRespondeSeCompensa() {
        when(ordenGateway.buscarPorId("o1")).thenReturn(Optional.of(ordenPendiente()));
        when(mascotaGateway.buscarPorId(1L)).thenReturn(Optional.of(toby));
        when(tipoGateway.buscarPorId(1L)).thenReturn(Optional.of(consulta));
        when(tipoGateway.buscarPorId(2L)).thenReturn(Optional.of(vacuna));
        when(duenoPagoGateway.consultarDueno("d1")).thenReturn(ana);
        when(duenoPagoGateway.descontar(anyString(), any(), anyString(), anyString()))
                .thenThrow(new ServicioNoDisponibleException("timeout"));
        when(duenoPagoGateway.reversar("d1", "ref-1")).thenReturn(true);

        assertThrows(ServicioNoDisponibleException.class, () -> useCase.confirmarAtencion("o1"));
        verify(ordenGateway).actualizarReferenciaPago(eq("o1"), anyString());
    }

    @Test
    void siFallaGuardarLaOrdenSeDevuelveElDinero() {
        when(ordenGateway.buscarPorId("o1")).thenReturn(Optional.of(ordenPendiente()));
        when(mascotaGateway.buscarPorId(1L)).thenReturn(Optional.of(toby));
        when(tipoGateway.buscarPorId(1L)).thenReturn(Optional.of(consulta));
        when(tipoGateway.buscarPorId(2L)).thenReturn(Optional.of(vacuna));
        when(duenoPagoGateway.consultarDueno("d1")).thenReturn(ana);
        when(duenoPagoGateway.descontar(anyString(), any(), anyString(), anyString())).thenReturn(BigDecimal.ONE);
        when(ordenGateway.guardar(any())).thenThrow(new IllegalStateException("BD caida"));
        when(duenoPagoGateway.reversar("d1", "ref-1")).thenThrow(new ServicioNoDisponibleException("tambien caido"));

        assertThrows(IllegalStateException.class, () -> useCase.confirmarAtencion("o1"));
    }

    @Test
    void confirmarUnaOrdenYaRealizadaFalla() {
        OrdenAtencion realizada = ordenPendiente();
        realizada.marcarRealizada(BigDecimal.TEN);
        when(ordenGateway.buscarPorId("o1")).thenReturn(Optional.of(realizada));
        assertThrows(ReglaNegocioException.class, () -> useCase.confirmarAtencion("o1"));
    }

    @Test
    void cancelarUnaRealizadaReversaElCobro() {
        OrdenAtencion realizada = ordenPendiente();
        realizada.marcarRealizada(new BigDecimal("105000"));
        when(ordenGateway.buscarPorId("o1")).thenReturn(Optional.of(realizada));
        guardarDevuelveLoMismo();

        assertEquals(EstadoOrden.CANCELADA, useCase.cancelarAtencion("o1").getEstado());
        verify(duenoPagoGateway).reversar("d1", "ref-1");
    }

    @Test
    void cancelarDosVecesFalla() {
        OrdenAtencion cancelada = ordenPendiente();
        cancelada.marcarCancelada();
        when(ordenGateway.buscarPorId("o1")).thenReturn(Optional.of(cancelada));
        assertThrows(ReglaNegocioException.class, () -> useCase.cancelarAtencion("o1"));
    }

    @Test
    void historialDeLaMascota() {
        when(mascotaGateway.buscarPorId(1L)).thenReturn(Optional.of(toby));
        when(ordenGateway.listarRealizadasPorMascota(1L)).thenReturn(List.of(ordenPendiente()));
        assertEquals(1, useCase.historialMascota(1L).size());
    }

    @Test
    void validaciones() {
        when(ordenGateway.buscarPorId("x")).thenReturn(Optional.empty());
        assertThrows(OrdenNoEncontradaException.class, () -> useCase.consultarOrden("x"));
        assertThrows(IllegalArgumentException.class, () -> useCase.consultarOrden(" "));
        assertThrows(IllegalArgumentException.class, () -> useCase.agregarServicio(1L, 1L, 0));
        assertThrows(IllegalArgumentException.class, () -> useCase.agregarServicio(1L, null, 1));
        assertThrows(IllegalArgumentException.class, () -> useCase.historialMascota(null));
    }
}
