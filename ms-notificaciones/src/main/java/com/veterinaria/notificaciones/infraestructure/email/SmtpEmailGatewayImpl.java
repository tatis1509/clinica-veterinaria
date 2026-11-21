package com.veterinaria.notificaciones.infraestructure.email;

import com.veterinaria.notificaciones.domain.model.NotificationChannel;
import com.veterinaria.notificaciones.domain.model.NotificationResult;
import com.veterinaria.notificaciones.domain.model.exception.ProveedorNotificacionException;
import com.veterinaria.notificaciones.domain.model.gateway.EmailGateway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

/** Adaptador: envia correos por SMTP (Mailpit en desarrollo; Brevo u otro servidor cambiando solo la configuracion). */
@Component
public class SmtpEmailGatewayImpl implements EmailGateway {

    private static final ZoneId BOGOTA = ZoneId.of("America/Bogota");

    private final JavaMailSender mailSender;
    private final String from;

    public SmtpEmailGatewayImpl(JavaMailSender mailSender,
                                @Value("${notification.email.from}") String from) {
        this.mailSender = mailSender;
        this.from = from;
    }

    @Override
    public NotificationResult sendEmail(String email, String subject, String message) {
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setFrom(from);
        mail.setTo(email);
        mail.setSubject(subject);
        mail.setText(message);
        try {
            mailSender.send(mail);
        } catch (MailException e) {
            throw new ProveedorNotificacionException("No se pudo enviar el correo: " + e.getMessage());
        }
        return new NotificationResult(
                NotificationChannel.EMAIL,
                "SENT",
                UUID.randomUUID().toString(),
                email,
                ZonedDateTime.now(BOGOTA));
    }
}
