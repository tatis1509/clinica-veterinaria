package com.veterinaria.notificaciones.domain.model;

import java.time.LocalDate;

/** Datos para avisar al dueno de la proxima dosis de vacuna o desparasitacion. canal: EMAIL, SMS o AMBOS. */
public record RecordatorioVacuna(
        String nombreDueno,
        String correo,
        String telefono,
        String nombreMascota,
        String vacuna,
        LocalDate fechaProximaDosis,
        String canal) {
}
