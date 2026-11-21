package com.veterinaria.notificaciones.infraestructure.entry_points;

import com.veterinaria.notificaciones.domain.model.EstadoNotificacion;
import com.veterinaria.notificaciones.domain.model.Notificacion;
import com.veterinaria.notificaciones.domain.model.NotificationChannel;
import com.veterinaria.notificaciones.domain.model.RecordatorioVacuna;
import com.veterinaria.notificaciones.domain.model.TipoNotificacion;
import com.veterinaria.notificaciones.domain.usecase.NotificacionUseCase;
import com.veterinaria.notificaciones.infraestructure.entry_points.dto.EmailPeticion;
import com.veterinaria.notificaciones.infraestructure.entry_points.dto.EstadoPeticion;
import com.veterinaria.notificaciones.infraestructure.entry_points.dto.NotificacionPeticion;
import com.veterinaria.notificaciones.infraestructure.entry_points.dto.SmsPeticion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificacionControllerTest {

    @Mock
    private NotificacionUseCase useCase;
    @InjectMocks
    private NotificacionController controller;

    private Notificacion enviada() {
        Notificacion n = Notificacion.nueva(NotificationChannel.EMAIL, TipoNotificacion.GENERAL, "a@b.com", "s", "m");
        n.marcarEnviada("p1");
        return n;
    }

    @Test
    void crud() {
        Notificacion n = enviada();
        when(useCase.crear(any(Notificacion.class))).thenReturn(n);
        when(useCase.listar()).thenReturn(List.of(n));
        when(useCase.buscarPorId(1L)).thenReturn(n);
        when(useCase.actualizarEstado(1L, EstadoNotificacion.ENVIADA)).thenReturn(n);

        assertEquals(HttpStatus.CREATED, controller.crear(new NotificacionPeticion(NotificationChannel.EMAIL,
                TipoNotificacion.GENERAL, "a@b.com", "s", "m")).getStatusCode());
        assertEquals(1, controller.listar().getBody().size());
        assertEquals(n, controller.buscar(1L).getBody());
        assertEquals(n, controller.actualizarEstado(1L, new EstadoPeticion(EstadoNotificacion.ENVIADA)).getBody());
        assertEquals(HttpStatus.NO_CONTENT, controller.eliminar(1L).getStatusCode());
        verify(useCase).eliminar(1L);
    }

    @Test
    void apisExtra() {
        Notificacion ok = enviada();
        Notificacion fallida = Notificacion.nueva(NotificationChannel.SMS, TipoNotificacion.GENERAL, "+573001234567", null, "m");
        fallida.marcarFallida("caido");
        RecordatorioVacuna r = new RecordatorioVacuna("Ana", "a@b.com", null, "Toby", "Rabia", LocalDate.now(), "EMAIL");
        when(useCase.enviarEmail("a@b.com", "s", "m", null)).thenReturn(ok);
        when(useCase.enviarSms("3001234567", "m", null)).thenReturn(fallida);
        when(useCase.recordatorioVacuna(r)).thenReturn(List.of(ok));
        when(useCase.historial("a@b.com")).thenReturn(List.of(ok));
        when(useCase.reintentar(2L)).thenReturn(ok);

        assertEquals(HttpStatus.OK, controller.enviarEmail(new EmailPeticion("a@b.com", "s", "m", null)).getStatusCode());
        assertEquals(HttpStatus.BAD_GATEWAY, controller.enviarSms(new SmsPeticion("3001234567", "m", null)).getStatusCode());
        assertEquals(HttpStatus.OK, controller.recordatorioVacuna(r).getStatusCode());
        assertEquals(1, controller.historial("a@b.com").getBody().size());
        assertEquals(HttpStatus.OK, controller.reintentar(2L).getStatusCode());
    }
}
