package com.veterinaria.duenos.aplicattion;

import com.veterinaria.duenos.domain.model.gateway.DuenoGateway;
import com.veterinaria.duenos.domain.model.gateway.EncrypterGateway;
import com.veterinaria.duenos.domain.model.gateway.SaldoGateway;
import com.veterinaria.duenos.domain.usecase.DuenoUseCase;
import com.veterinaria.duenos.domain.usecase.PagoUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Inyeccion de dependencias: los casos de uso son clases Java puras, aqui se conectan con los adaptadores. */
@Configuration
public class UseCasesConfig {

    @Bean
    public DuenoUseCase duenoUseCase(DuenoGateway duenoGateway, EncrypterGateway encrypterGateway) {
        return new DuenoUseCase(duenoGateway, encrypterGateway);
    }

    @Bean
    public PagoUseCase pagoUseCase(SaldoGateway saldoGateway) {
        return new PagoUseCase(saldoGateway);
    }
}
