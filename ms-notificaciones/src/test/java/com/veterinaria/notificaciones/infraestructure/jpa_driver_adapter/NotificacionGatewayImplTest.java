package com.veterinaria.notificaciones.infraestructure.jpa_driver_adapter;

import com.veterinaria.notificaciones.domain.model.Notificacion;
import com.veterinaria.notificaciones.domain.model.NotificationChannel;
import com.veterinaria.notificaciones.domain.model.TipoNotificacion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificacionGatewayImplTest {

    @Mock
    private NotificacionRepositoryJPA repository;
    @InjectMocks
    private NotificacionGatewayImpl gateway;

    @Test
    void guardarBuscarListarYEliminar() {
        Notificacion n = Notificacion.nueva(NotificationChannel.EMAIL, TipoNotificacion.GENERAL, "a@b.com", "x", "y");
        n.marcarFallida("e".repeat(1500));
        when(repository.save(any(NotificacionEntity.class))).thenAnswer(inv -> {
            NotificacionEntity e = inv.getArgument(0);
            e.setIdNotificacion(1L);
            return e;
        });

        Notificacion guardada = gateway.guardar(n);
        assertEquals(1L, guardada.getIdNotificacion());
        assertEquals(1000, guardada.getDetalleError().length());

        NotificacionEntity entity = new NotificacionEntity();
        entity.setIdNotificacion(1L);
        entity.setDestinatario("a@b.com");
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(repository.findAll(any(Sort.class))).thenReturn(List.of(entity));
        when(repository.findByDestinatarioOrderByFechaCreacionDesc("a@b.com")).thenReturn(List.of(entity));

        assertTrue(gateway.buscarPorId(1L).isPresent());
        assertEquals(1, gateway.listar().size());
        assertEquals(1, gateway.listarPorDestinatario("a@b.com").size());
        gateway.eliminar(1L);
        verify(repository).deleteById(1L);
    }
}
