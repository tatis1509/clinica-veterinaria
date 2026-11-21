package com.veterinaria.mascotas.aplicattion;

import com.veterinaria.mascotas.domain.model.gateway.DuenoPagoGateway;
import com.veterinaria.mascotas.domain.model.gateway.MascotaGateway;
import com.veterinaria.mascotas.domain.model.gateway.NotificacionGateway;
import com.veterinaria.mascotas.domain.model.gateway.OrdenAtencionGateway;
import com.veterinaria.mascotas.domain.model.gateway.TipoAtencionGateway;
import com.veterinaria.mascotas.domain.model.gateway.TriajeIaGateway;
import com.veterinaria.mascotas.domain.usecase.AtencionUseCase;
import com.veterinaria.mascotas.domain.usecase.MascotaUseCase;
import com.veterinaria.mascotas.domain.usecase.TipoAtencionUseCase;
import com.veterinaria.mascotas.domain.usecase.TriajeUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCasesConfig {

    @Bean
    public MascotaUseCase mascotaUseCase(MascotaGateway mascotaGateway, DuenoPagoGateway duenoPagoGateway) {
        return new MascotaUseCase(mascotaGateway, duenoPagoGateway);
    }

    @Bean
    public TipoAtencionUseCase tipoAtencionUseCase(TipoAtencionGateway tipoAtencionGateway) {
        return new TipoAtencionUseCase(tipoAtencionGateway);
    }

    @Bean
    public AtencionUseCase atencionUseCase(OrdenAtencionGateway ordenGateway, MascotaGateway mascotaGateway,
                                           TipoAtencionGateway tipoAtencionGateway,
                                           DuenoPagoGateway duenoPagoGateway,
                                           NotificacionGateway notificacionGateway) {
        return new AtencionUseCase(ordenGateway, mascotaGateway, tipoAtencionGateway, duenoPagoGateway,
                notificacionGateway);
    }

    @Bean
    public TriajeUseCase triajeUseCase(TriajeIaGateway triajeIaGateway) {
        return new TriajeUseCase(triajeIaGateway);
    }
}
