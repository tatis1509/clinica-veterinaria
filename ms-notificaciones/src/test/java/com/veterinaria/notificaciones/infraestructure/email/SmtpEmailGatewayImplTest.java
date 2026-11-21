package com.veterinaria.notificaciones.infraestructure.email;

import com.veterinaria.notificaciones.domain.model.NotificationResult;
import com.veterinaria.notificaciones.domain.model.exception.ProveedorNotificacionException;
import org.junit.jupiter.api.Test;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class SmtpEmailGatewayImplTest {

    private final JavaMailSender mailSender = mock(JavaMailSender.class);
    private final SmtpEmailGatewayImpl gateway = new SmtpEmailGatewayImpl(mailSender, "clinica@vet.com");

    @Test
    void enviaElCorreo() {
        NotificationResult r = gateway.sendEmail("ana@correo.com", "Comprobante", "Gracias");
        assertEquals("SENT", r.status());
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void siFallaElServidorLanzaExcepcionDeProveedor() {
        doThrow(new MailSendException("caido")).when(mailSender).send(any(SimpleMailMessage.class));
        assertThrows(ProveedorNotificacionException.class, () -> gateway.sendEmail("a@b.com", "s", "m"));
    }
}
