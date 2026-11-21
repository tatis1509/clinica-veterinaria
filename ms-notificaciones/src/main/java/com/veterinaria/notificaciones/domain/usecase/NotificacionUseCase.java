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
import lombok.RequiredArgsConstructor;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Reglas de negocio de las notificaciones de la clinica. Valida, envia por el canal elegido
 * (sin saber nada de Twilio ni de SMTP) y deja SIEMPRE registro del resultado en el historial.
 */
@RequiredArgsConstructor
public class NotificacionUseCase {

    public static final String ASUNTO_POR_DEFECTO = "Clinica Veterinaria";
    private static final int MAX_SMS = 1600;
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final Pattern E164 = Pattern.compile("^\\+[1-9]\\d{7,14}$");
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final NotificacionGateway notificacionGateway;
    private final EmailGateway emailGateway;
    private final SmsGateway smsGateway;

    // ------------------------------------------------------------------ CRUD

    /** Registra una notificacion PENDIENTE (sin enviarla). */
    public Notificacion crear(Notificacion notificacion) {
        if (notificacion == null || notificacion.getCanal() == null) {
            throw new ValidacionNotificacionException("El canal es obligatorio (SMS o EMAIL)");
        }
        String destinatario = validarDestinatario(notificacion.getCanal(), notificacion.getDestinatario());
        validarMensaje(notificacion.getCanal(), notificacion.getMensaje());
        Notificacion nueva = Notificacion.nueva(notificacion.getCanal(),
                notificacion.getTipo() == null ? TipoNotificacion.GENERAL : notificacion.getTipo(),
                destinatario, asunto(notificacion.getAsunto()), notificacion.getMensaje().trim());
        return notificacionGateway.guardar(nueva);
    }

    public Notificacion buscarPorId(Long id) {
        if (id == null) {
            throw new ValidacionNotificacionException("El id es obligatorio");
        }
        return notificacionGateway.buscarPorId(id).orElseThrow(() -> new NotificacionNoEncontradaException(id));
    }

    public List<Notificacion> listar() {
        return notificacionGateway.listar();
    }

    public Notificacion actualizarEstado(Long id, EstadoNotificacion estado) {
        if (estado == null) {
            throw new ValidacionNotificacionException("El estado es obligatorio (PENDIENTE, ENVIADA o FALLIDA)");
        }
        Notificacion notificacion = buscarPorId(id);
        notificacion.setEstado(estado);
        return notificacionGateway.guardar(notificacion);
    }

    public void eliminar(Long id) {
        buscarPorId(id);
        notificacionGateway.eliminar(id);
    }

    // ------------------------------------------------------------- APIs extra

    /** Envio inmediato de comprobante de atencion, receta, etc. por correo. */
    public Notificacion enviarEmail(String correo, String asunto, String mensaje, TipoNotificacion tipo) {
        String destino = validarDestinatario(NotificationChannel.EMAIL, correo);
        validarMensaje(NotificationChannel.EMAIL, mensaje);
        return enviar(Notificacion.nueva(NotificationChannel.EMAIL, tipoOGeneral(tipo), destino,
                asunto(asunto), mensaje.trim()));
    }

    /** Envio de SMS por Twilio confirmando cita o resultado. */
    public Notificacion enviarSms(String telefono, String mensaje, TipoNotificacion tipo) {
        String destino = validarDestinatario(NotificationChannel.SMS, telefono);
        validarMensaje(NotificationChannel.SMS, mensaje);
        return enviar(Notificacion.nueva(NotificationChannel.SMS, tipoOGeneral(tipo), destino,
                null, mensaje.trim()));
    }

    /** Alerta de proxima dosis de vacuna o desparasitacion por EMAIL, SMS o AMBOS. */
    public List<Notificacion> recordatorioVacuna(RecordatorioVacuna datos) {
        if (datos == null || isBlank(datos.nombreMascota()) || isBlank(datos.vacuna())
                || datos.fechaProximaDosis() == null) {
            throw new ValidacionNotificacionException(
                    "nombreMascota, vacuna y fechaProximaDosis son obligatorios");
        }
        String canal = isBlank(datos.canal()) ? "EMAIL" : datos.canal().trim().toUpperCase();
        String saludo = isBlank(datos.nombreDueno()) ? "Hola" : "Hola " + datos.nombreDueno().trim();
        String mensaje = saludo + ", te recordamos que " + datos.nombreMascota().trim()
                + " tiene programada su dosis de " + datos.vacuna().trim()
                + " para el " + datos.fechaProximaDosis().format(FECHA)
                + ". Agenda tu cita en la Clinica Veterinaria.";

        List<Notificacion> enviadas = new ArrayList<>();
        if (canal.equals("EMAIL") || canal.equals("AMBOS")) {
            enviadas.add(enviarEmail(datos.correo(), "Recordatorio de vacuna - " + datos.nombreMascota().trim(),
                    mensaje, TipoNotificacion.RECORDATORIO_VACUNA));
        }
        if (canal.equals("SMS") || canal.equals("AMBOS")) {
            enviadas.add(enviarSms(datos.telefono(), mensaje, TipoNotificacion.RECORDATORIO_VACUNA));
        }
        if (enviadas.isEmpty()) {
            throw new ValidacionNotificacionException("Canal invalido: " + datos.canal() + ". Use EMAIL, SMS o AMBOS");
        }
        return enviadas;
    }

    /** Todos los correos y SMS enviados a un dueno (por correo o por telefono). */
    public List<Notificacion> historial(String destinatario) {
        if (isBlank(destinatario)) {
            throw new ValidacionNotificacionException("El destinatario es obligatorio");
        }
        String valor = destinatario.trim();
        String normalizado = EMAIL.matcher(valor).matches() ? valor.toLowerCase() : normalizarTelefono(valor);
        return notificacionGateway.listarPorDestinatario(normalizado);
    }

    /** Reenvia una notificacion que quedo FALLIDA (o una PENDIENTE que nunca se envio). */
    public Notificacion reintentar(Long id) {
        Notificacion notificacion = buscarPorId(id);
        if (notificacion.getEstado() == EstadoNotificacion.ENVIADA) {
            throw new ReglaNegocioException("La notificacion " + id + " ya fue enviada; no se reintenta");
        }
        return enviar(notificacion);
    }

    // ---------------------------------------------------------------- helpers

    private Notificacion enviar(Notificacion notificacion) {
        try {
            NotificationResult resultado = switch (notificacion.getCanal()) {
                case EMAIL -> emailGateway.sendEmail(notificacion.getDestinatario(),
                        asunto(notificacion.getAsunto()), notificacion.getMensaje());
                case SMS -> smsGateway.sendSms(notificacion.getDestinatario(), notificacion.getMensaje());
            };
            notificacion.marcarEnviada(resultado.providerId());
        } catch (ProveedorNotificacionException e) {
            notificacion.marcarFallida(e.getMessage());
        }
        return notificacionGateway.guardar(notificacion);
    }

    private String validarDestinatario(NotificationChannel canal, String destinatario) {
        if (canal == NotificationChannel.SMS) {
            return normalizarTelefono(destinatario);
        }
        if (isBlank(destinatario) || !EMAIL.matcher(destinatario.trim()).matches()) {
            throw new ValidacionNotificacionException("El correo es obligatorio y debe tener un formato valido");
        }
        return destinatario.trim().toLowerCase();
    }

    private void validarMensaje(NotificationChannel canal, String mensaje) {
        if (isBlank(mensaje)) {
            throw new ValidacionNotificacionException("El mensaje es obligatorio");
        }
        if (canal == NotificationChannel.SMS && mensaje.trim().length() > MAX_SMS) {
            throw new ValidacionNotificacionException("El mensaje SMS no puede superar " + MAX_SMS + " caracteres");
        }
    }

    /**
     * Twilio exige el formato internacional (+573001234567).
     * Acepta 3001234567, 573001234567 o +573001234567, con espacios o guiones.
     */
    static String normalizarTelefono(String raw) {
        if (isBlank(raw)) {
            throw new ValidacionNotificacionException("El telefono es obligatorio para el canal SMS");
        }
        String limpio = raw.trim().replaceAll("[\\s\\-().]", "");
        String candidato;
        if (limpio.startsWith("+")) {
            candidato = limpio;
        } else if (limpio.matches("3\\d{9}")) {
            candidato = "+57" + limpio;
        } else if (limpio.matches("57\\d{10}")) {
            candidato = "+" + limpio;
        } else {
            candidato = "";
        }
        if (!E164.matcher(candidato).matches()) {
            throw new ValidacionNotificacionException("Telefono invalido: use 3001234567 o +573001234567");
        }
        return candidato;
    }

    private static String asunto(String asunto) {
        return isBlank(asunto) ? ASUNTO_POR_DEFECTO : asunto.trim();
    }

    private static TipoNotificacion tipoOGeneral(TipoNotificacion tipo) {
        return tipo == null ? TipoNotificacion.GENERAL : tipo;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
