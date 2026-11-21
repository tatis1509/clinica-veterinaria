package com.veterinaria.notificaciones.infraestructure.twilio;

import com.veterinaria.notificaciones.domain.model.NotificationChannel;
import com.veterinaria.notificaciones.domain.model.NotificationResult;
import com.veterinaria.notificaciones.domain.model.exception.ProveedorNotificacionException;
import com.veterinaria.notificaciones.domain.model.gateway.SmsGateway;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/** Adaptador: envia SMS llamando a la API REST de Twilio. Las credenciales vienen de variables de entorno. */
@Component
public class TwilioSmsGatewayImpl implements SmsGateway {

    private static final ZoneId BOGOTA = ZoneId.of("America/Bogota");

    private final RestClient restClient;
    private final String accountSid;
    private final String authToken;
    private final String fromNumber;
    private final String trialTemplate;

    public TwilioSmsGatewayImpl(RestClient.Builder restClientBuilder,
                                @Value("${twilio.account-sid:}") String accountSid,
                                @Value("${twilio.auth-token:}") String authToken,
                                @Value("${twilio.phone-number:}") String fromNumber,
                                @Value("${twilio.api-url:https://api.twilio.com}") String apiUrl,
                                @Value("${twilio.trial-template:sms_order_confirmation}") String trialTemplate) {
        this.accountSid = accountSid;
        this.authToken = authToken;
        this.fromNumber = fromNumber;
        this.trialTemplate = trialTemplate;

        // El builder ya trae los timeouts (ver config/RestClientConfig); en las pruebas trae un servidor simulado
        this.restClient = restClientBuilder
                .baseUrl(apiUrl + "/2010-04-01/Accounts/" + accountSid)
                .build();
    }

    @Override
    public NotificationResult sendSms(String phoneNumber, String message) {
        if (isBlank(accountSid) || isBlank(authToken) || isBlank(fromNumber)) {
            throw new ProveedorNotificacionException(
                    "Twilio no esta configurado: defina TWILIO_ACCOUNT_SID, TWILIO_AUTH_TOKEN y TWILIO_PHONE_NUMBER");
        }
        try {
            return post(phoneNumber, message);
        } catch (HttpClientErrorException e) {
            // Cuenta Trial: Twilio (error 572006) solo acepta plantillas predefinidas como Body.
            // Si pasa, se reintenta una vez enviando el nombre de la plantilla (llega el texto predefinido de Twilio).
            if (esErrorDePlantillaTrial(e) && !isBlank(trialTemplate)) {
                try {
                    return post(phoneNumber, trialTemplate);
                } catch (HttpClientErrorException e2) {
                    throw rechazo(e2);
                } catch (HttpServerErrorException | ResourceAccessException e2) {
                    throw noDisponible(e2);
                }
            }
            throw rechazo(e);
        } catch (HttpServerErrorException | ResourceAccessException e) {
            throw noDisponible(e);
        }
    }

    private NotificationResult post(String phoneNumber, String body) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("To", phoneNumber);
        form.add("From", fromNumber);
        form.add("Body", body);

        TwilioMessageResponse response = restClient.post()
                .uri("/Messages.json")
                .headers(headers -> headers.setBasicAuth(accountSid, authToken))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(TwilioMessageResponse.class);

        if (response == null || response.sid() == null) {
            throw new ProveedorNotificacionException("Twilio devolvio una respuesta vacia");
        }
        return new NotificationResult(
                NotificationChannel.SMS,
                response.status() == null ? "queued" : response.status(),
                response.sid(),
                phoneNumber,
                toColombiaTime(response.dateCreated()));
    }

    private static boolean esErrorDePlantillaTrial(HttpClientErrorException e) {
        String cuerpo = e.getResponseBodyAsString();
        return cuerpo != null && cuerpo.contains("572006");
    }

    private static ProveedorNotificacionException rechazo(HttpClientErrorException e) {
        return new ProveedorNotificacionException(
                "Twilio rechazo el envio (" + e.getStatusCode().value() + "): " + e.getResponseBodyAsString());
    }

    private static ProveedorNotificacionException noDisponible(RuntimeException e) {
        return new ProveedorNotificacionException("No se pudo comunicar con Twilio: " + e.getMessage());
    }

    /** Twilio responde la fecha en UTC (ej. "Thu, 30 Jul 2015 20:12:31 +0000"); la pasamos a hora de Colombia. */
    private static ZonedDateTime toColombiaTime(String dateCreated) {
        if (dateCreated != null) {
            try {
                return ZonedDateTime.parse(dateCreated, DateTimeFormatter.RFC_1123_DATE_TIME)
                        .withZoneSameInstant(BOGOTA);
            } catch (RuntimeException ignored) {
                // si no se puede leer, usamos la hora actual
            }
        }
        return ZonedDateTime.now(BOGOTA);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    /** Solo los campos de la respuesta de Twilio que nos interesan. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record TwilioMessageResponse(
            String sid,
            String status,
            String to,
            @JsonProperty("date_created") String dateCreated) {
    }
}
