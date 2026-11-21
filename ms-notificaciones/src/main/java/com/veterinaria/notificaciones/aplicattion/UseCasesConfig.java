package com.veterinaria.notificaciones.aplicattion;

import com.veterinaria.notificaciones.domain.model.gateway.EmailGateway;
import com.veterinaria.notificaciones.domain.model.gateway.NotificacionGateway;
import com.veterinaria.notificaciones.domain.model.gateway.SmsGateway;
import com.veterinaria.notificaciones.domain.usecase.NotificacionUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCasesConfig {

    @Bean
    public NotificacionUseCase notificacionUseCase(NotificacionGateway notificacionGateway,
                                                   EmailGateway emailGateway, SmsGateway smsGateway) {
        return new NotificacionUseCase(notificacionGateway, emailGateway, smsGateway);
    }
}
