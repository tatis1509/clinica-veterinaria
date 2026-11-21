package com.veterinaria.notificaciones.domain.usecase;

import com.veterinaria.notificaciones.domain.model.EstadoNotificacion;
import com.veterinaria.notificaciones.domain.model.Notificacion;
import com.veterinaria.notificaciones.domain.model.NotificationChannel;
import com.veterinaria.notificaciones.domain.model.NotificationResult;
import com.veterinaria.notificaciones.domain.model.RecordatorioVacuna;
import com.veterinaria.notificaciones.domain.model.TipoNotificacion;
import com.veterinaria.notificaciones.domain.model.exception.NotificacionNoEncontradaException;
import com.veterinaria.notificaciones.domain.model.exception.ProveedorNotificacionException;
import com.veterinaria.notificaciones.domain.model.exception.ReglaNegocioException;
import com.veterinaria.notificaciones.domain.model.exception.ValidacionNotificacionException;
import com.veterinaria.notificaciones.domain.model.gateway.EmailGateway;
import com.veterinaria.notificaciones.domain.model.gateway.NotificacionGateway;
import com.veterinaria.notificaciones.domain.model.gateway.SmsGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.ZonedDateTime;
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
class NotificacionUseCaseTest {

    @Mock
    private NotificacionGateway gateway;
    @Mock
    private EmailGateway emailGateway;
    @Mock
    private SmsGateway smsGateway;
    @InjectMocks
    private NotificacionUseCase useCase;

    private void guardarDevuelveLoMismo() {
        when(gateway.guardar(any(Notificacion.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private NotificationResult ok(NotificationChannel canal, String destino) {
        return new NotificationResult(canal, "SENT", "prov-1", destino, ZonedDateTime.now());
    }

    @Test
    void crearRegistraPendiente() {
        guardarDevuelveLoMismo();
        Notificacion n = new Notificacion();
        n.setCanal(NotificationChannel.SMS);
        n.setDestinatario("300 123 4567");
        n.setMensaje(" Cita confirmada ");

        Notificacion creada = useCase.crear(n);

        assertEquals(EstadoNotificacion.PENDIENTE, creada.getEstado());
        assertEquals("+573001234567", creada.getDestinatario());
        assertEquals(TipoNotificacion.GENERAL, creada.getTipo());
        assertEquals("Cita confirmada", creada.getMensaje());
    }

    @Test
    void crudBasico() {
        Notificacion n = Notificacion.nueva(NotificationChannel.EMAIL, TipoNotificacion.GENERAL, "a@b.com", "x", "y");
        when(gateway.buscarPorId(1L)).thenReturn(Optional.of(n));
        when(gateway.listar()).thenReturn(List.of(n));
        guardarDevuelveLoMismo();

        assertEquals(n, useCase.buscarPorId(1L));
        assertEquals(1, useCase.listar().size());
        assertEquals(EstadoNotificacion.FALLIDA, useCase.actualizarEstado(1L, EstadoNotificacion.FALLIDA).getEstado());
        useCase.eliminar(1L);
        verify(gateway).eliminar(1L);
    }

    @Test
    void buscarInexistenteFalla() {
        when(gateway.buscarPorId(9L)).thenReturn(Optional.empty());
        assertThrows(NotificacionNoEncontradaException.class, () -> useCase.buscarPorId(9L));
    }

    @Test
    void enviarEmailQuedaEnviada() {
        guardarDevuelveLoMismo();
        when(emailGateway.sendEmail("ana@correo.com", "Comprobante", "Gracias por su visita"))
                .thenReturn(ok(NotificationChannel.EMAIL, "ana@correo.com"));

        Notificacion n = useCase.enviarEmail("Ana@Correo.com", "Comprobante", "Gracias por su visita",
                TipoNotificacion.COMPROBANTE_ATENCION);

        assertEquals(EstadoNotificacion.ENVIADA, n.getEstado());
        assertEquals("prov-1", n.getIdProveedor());
        assertEquals(1, n.getIntentos());
    }

    @Test
    void enviarEmailSiElProveedorFallaQuedaFallida() {
        guardarDevuelveLoMismo();
        when(emailGateway.sendEmail(anyString(), anyString(), anyString()))
                .thenThrow(new ProveedorNotificacionException("SMTP caido"));

        Notificacion n = useCase.enviarEmail("ana@correo.com", null, "Hola", null);

        assertEquals(EstadoNotificacion.FALLIDA, n.getEstado());
        assertEquals("SMTP caido", n.getDetalleError());
        assertEquals(NotificacionUseCase.ASUNTO_POR_DEFECTO, n.getAsunto());
    }

    @Test
    void enviarSmsNormalizaElTelefono() {
        guardarDevuelveLoMismo();
        when(smsGateway.sendSms("+573001234567", "Cita confirmada"))
                .thenReturn(ok(NotificationChannel.SMS, "+573001234567"));

        Notificacion n = useCase.enviarSms("573001234567", "Cita confirmada", TipoNotificacion.CONFIRMACION_CITA);

        assertEquals(EstadoNotificacion.ENVIADA, n.getEstado());
        assertEquals("+573001234567", n.getDestinatario());
    }

    @Test
    void recordatorioPorAmbosCanales() {
        guardarDevuelveLoMismo();
        when(emailGateway.sendEmail(eq("ana@correo.com"), anyString(), anyString()))
                .thenReturn(ok(NotificationChannel.EMAIL, "ana@correo.com"));
        when(smsGateway.sendSms(eq("+573001234567"), anyString()))
                .thenReturn(ok(NotificationChannel.SMS, "+573001234567"));

        List<Notificacion> enviadas = useCase.recordatorioVacuna(new RecordatorioVacuna("Ana", "ana@correo.com",
                "3001234567", "Firulais", "Rabia", LocalDate.of(2026, 12, 15), "ambos"));

        assertEquals(2, enviadas.size());
        assertEquals(TipoNotificacion.RECORDATORIO_VACUNA, enviadas.get(0).getTipo());
    }

    @Test
    void recordatorioConCanalInvalidoFalla() {
        RecordatorioVacuna datos = new RecordatorioVacuna(null, "ana@correo.com", null, "Firulais", "Rabia",
                LocalDate.of(2026, 12, 15), "FAX");
        assertThrows(ValidacionNotificacionException.class, () -> useCase.recordatorioVacuna(datos));
        assertThrows(ValidacionNotificacionException.class, () -> useCase.recordatorioVacuna(null));
    }

    @Test
    void historialPorCorreoYPorTelefono() {
        when(gateway.listarPorDestinatario("ana@correo.com")).thenReturn(List.of());
        when(gateway.listarPorDestinatario("+573001234567")).thenReturn(List.of());
        assertEquals(0, useCase.historial("ANA@correo.com").size());
        assertEquals(0, useCase.historial("3001234567").size());
    }

    @Test
    void reintentarUnaFallida() {
        Notificacion fallida = Notificacion.nueva(NotificationChannel.SMS, TipoNotificacion.GENERAL,
                "+573001234567", null, "Hola");
        fallida.marcarFallida("Twilio caido");
        when(gateway.buscarPorId(3L)).thenReturn(Optional.of(fallida));
        when(smsGateway.sendSms("+573001234567", "Hola")).thenReturn(ok(NotificationChannel.SMS, "+573001234567"));
        guardarDevuelveLoMismo();

        Notificacion n = useCase.reintentar(3L);

        assertEquals(EstadoNotificacion.ENVIADA, n.getEstado());
        assertEquals(2, n.getIntentos());
    }

    @Test
    void reintentarUnaEnviadaFalla() {
        Notificacion enviada = Notificacion.nueva(NotificationChannel.SMS, TipoNotificacion.GENERAL,
                "+573001234567", null, "Hola");
        enviada.marcarEnviada("SM1");
        when(gateway.buscarPorId(3L)).thenReturn(Optional.of(enviada));
        assertThrows(ReglaNegocioException.class, () -> useCase.reintentar(3L));
    }

    @Test
    void validacionesDeEntrada() {
        assertThrows(ValidacionNotificacionException.class, () -> useCase.enviarEmail("no-correo", "a", "b", null));
        assertThrows(ValidacionNotificacionException.class, () -> useCase.enviarSms("123", "b", null));
        assertThrows(ValidacionNotificacionException.class, () -> useCase.enviarSms("3001234567", " ", null));
        assertThrows(ValidacionNotificacionException.class, () -> useCase.enviarSms("3001234567", "x".repeat(1601), null));
        assertThrows(ValidacionNotificacionException.class, () -> useCase.historial(""));
        assertThrows(ValidacionNotificacionException.class, () -> useCase.crear(null));
        assertThrows(ValidacionNotificacionException.class, () -> useCase.actualizarEstado(1L, null));
    }
}
