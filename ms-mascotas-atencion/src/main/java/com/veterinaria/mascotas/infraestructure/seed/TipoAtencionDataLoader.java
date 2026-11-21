package com.veterinaria.mascotas.infraestructure.seed;

import com.veterinaria.mascotas.infraestructure.jpa_driver_adapter.TipoAtencionEntity;
import com.veterinaria.mascotas.infraestructure.jpa_driver_adapter.TipoAtencionRepositoryJPA;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/** Carga los 5 servicios de la clinica la primera vez, si la tabla esta vacia. */
@Component
@RequiredArgsConstructor
public class TipoAtencionDataLoader implements CommandLineRunner {

    private final TipoAtencionRepositoryJPA repository;

    @Value("${mascotas.seed.enabled:true}")
    private boolean seedHabilitado;

    @Override
    public void run(String... args) {
        if (!seedHabilitado || repository.count() > 0) {
            return;
        }
        repository.saveAll(List.of(
                tipo("Consulta general", "Valoracion medica completa: peso, temperatura, auscultacion y diagnostico.", "45000", 30),
                tipo("Vacunacion", "Aplicacion de vacuna (rabia, polivalente o triple felina) con carne de vacunas.", "60000", 15),
                tipo("Desparasitacion", "Desparasitacion interna y externa segun peso de la mascota.", "35000", 15),
                tipo("Bano y peluqueria", "Bano medicado, corte de pelo, limpieza de oidos y corte de unas.", "55000", 90),
                tipo("Cirugia menor", "Procedimientos ambulatorios: suturas, extraccion de masas pequenas, limpieza dental.", "250000", 120)));
    }

    private TipoAtencionEntity tipo(String nombre, String descripcion, String precio, int minutos) {
        return new TipoAtencionEntity(null, nombre, descripcion, new BigDecimal(precio), minutos);
    }
}
