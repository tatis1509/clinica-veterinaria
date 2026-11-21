package com.veterinaria.mascotas.infraestructure.jpa_driver_adapter;

import com.veterinaria.mascotas.domain.model.EstadoOrden;
import com.veterinaria.mascotas.domain.model.ItemOrden;
import com.veterinaria.mascotas.domain.model.Mascota;
import com.veterinaria.mascotas.domain.model.OrdenAtencion;
import com.veterinaria.mascotas.domain.model.TipoAtencion;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GatewaysJpaTest {

    @Test
    void mascotaGateway() {
        MascotaRepositoryJPA repo = mock(MascotaRepositoryJPA.class);
        MascotaGatewayImpl gateway = new MascotaGatewayImpl(repo);
        MascotaEntity entity = new MascotaEntity(1L, "Toby", "Perro", "Labrador", 4, BigDecimal.TEN, "d1");
        when(repo.save(any(MascotaEntity.class))).thenReturn(entity);
        when(repo.findById(1L)).thenReturn(Optional.of(entity));
        when(repo.findAll()).thenReturn(List.of(entity));
        when(repo.findByDuenoIdOrderByNombreAsc("d1")).thenReturn(List.of(entity));

        assertEquals(1L, gateway.guardar(new Mascota(null, "Toby", "Perro", "Labrador", 4, BigDecimal.TEN, "d1")).getIdMascota());
        assertTrue(gateway.buscarPorId(1L).isPresent());
        assertEquals(1, gateway.listar().size());
        assertEquals(1, gateway.listarPorDueno("d1").size());
        gateway.eliminar(1L);
        verify(repo).deleteById(1L);
    }

    @Test
    void tipoAtencionGateway() {
        TipoAtencionRepositoryJPA repo = mock(TipoAtencionRepositoryJPA.class);
        TipoAtencionGatewayImpl gateway = new TipoAtencionGatewayImpl(repo);
        TipoAtencionEntity entity = new TipoAtencionEntity(1L, "Consulta general", "d", new BigDecimal("45000"), 30);
        when(repo.save(any(TipoAtencionEntity.class))).thenReturn(entity);
        when(repo.findById(1L)).thenReturn(Optional.of(entity));
        when(repo.findAll(any(Sort.class))).thenReturn(List.of(entity));

        assertEquals(1L, gateway.guardar(new TipoAtencion(null, "Consulta general", "d", new BigDecimal("45000"), 30)).getIdTipoAtencion());
        assertTrue(gateway.buscarPorId(1L).isPresent());
        assertEquals(1, gateway.listar().size());
        gateway.eliminar(1L);
        verify(repo).deleteById(1L);
    }

    @Test
    void ordenGatewaySincronizaLosItems() {
        OrdenAtencionRepositoryJPA repo = mock(OrdenAtencionRepositoryJPA.class);
        OrdenAtencionGatewayImpl gateway = new OrdenAtencionGatewayImpl(repo);
        when(repo.save(any(OrdenAtencionEntity.class))).thenAnswer(inv -> {
            OrdenAtencionEntity e = inv.getArgument(0);
            e.setIdOrden("o1");
            return e;
        });

        // 1. orden nueva con dos servicios
        OrdenAtencion orden = OrdenAtencion.nueva(1L, "d1", "ref-1");
        orden.setItems(new ArrayList<>(List.of(
                new ItemOrden(null, 1L, "Consulta general", 1, new BigDecimal("45000")),
                new ItemOrden(null, 2L, "Vacunacion", 1, new BigDecimal("60000")))));
        OrdenAtencion guardada = gateway.guardar(orden);
        assertEquals("o1", guardada.getIdOrden());
        assertEquals(2, guardada.getItems().size());

        // 2. se actualiza: cambia la cantidad de uno y se retira el otro
        OrdenAtencionEntity entity = new OrdenAtencionEntity();
        entity.setIdOrden("o1");
        ItemOrdenEntity i1 = new ItemOrdenEntity();
        i1.setTipoAtencionId(1L);
        ItemOrdenEntity i2 = new ItemOrdenEntity();
        i2.setTipoAtencionId(2L);
        entity.getItems().addAll(List.of(i1, i2));
        when(repo.findById("o1")).thenReturn(Optional.of(entity));

        guardada.getItems().removeIf(i -> i.getTipoAtencionId() == 2L);
        guardada.getItems().get(0).setCantidad(2);
        OrdenAtencion actualizada = gateway.guardar(guardada);
        assertEquals(1, actualizada.getItems().size());
        assertEquals(new BigDecimal("90000"), actualizada.getItems().get(0).getSubtotal());

        // 3. consultas
        when(repo.findFirstByMascotaIdAndEstado(1L, EstadoOrden.PENDIENTE)).thenReturn(Optional.of(entity));
        when(repo.findByMascotaIdAndEstadoOrderByFechaAtencionDesc(1L, EstadoOrden.REALIZADA)).thenReturn(List.of(entity));
        assertTrue(gateway.buscarPorId("o1").isPresent());
        assertTrue(gateway.buscarPendientePorMascota(1L).isPresent());
        assertEquals(1, gateway.listarRealizadasPorMascota(1L).size());
        gateway.actualizarReferenciaPago("o1", "ref-2");
        assertEquals("ref-2", entity.getReferenciaPago());
    }
}
