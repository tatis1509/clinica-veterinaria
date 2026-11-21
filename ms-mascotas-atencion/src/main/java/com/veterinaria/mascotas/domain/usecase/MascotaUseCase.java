package com.veterinaria.mascotas.domain.usecase;

import com.veterinaria.mascotas.domain.model.Mascota;
import com.veterinaria.mascotas.domain.model.exception.MascotaNoEncontradaException;
import com.veterinaria.mascotas.domain.model.gateway.DuenoPagoGateway;
import com.veterinaria.mascotas.domain.model.gateway.MascotaGateway;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@RequiredArgsConstructor
public class MascotaUseCase {

    private final MascotaGateway mascotaGateway;
    private final DuenoPagoGateway duenoPagoGateway;

    public Mascota crear(Mascota mascota) {
        validar(mascota);
        duenoPagoGateway.consultarDueno(mascota.getDuenoId()); // lanza DuenoNoEncontrado si no existe
        mascota.setIdMascota(null);
        return mascotaGateway.guardar(mascota);
    }

    public Mascota buscarPorId(Long idMascota) {
        if (idMascota == null) {
            throw new IllegalArgumentException("El idMascota es obligatorio");
        }
        return mascotaGateway.buscarPorId(idMascota)
                .orElseThrow(() -> new MascotaNoEncontradaException(idMascota));
    }

    public List<Mascota> listar() {
        return mascotaGateway.listar();
    }

    public Mascota actualizar(Long idMascota, Mascota mascota) {
        Mascota actual = buscarPorId(idMascota);
        validar(mascota);
        if (!actual.getDuenoId().equals(mascota.getDuenoId())) {
            duenoPagoGateway.consultarDueno(mascota.getDuenoId()); // cambio de dueno: el nuevo debe existir
        }
        mascota.setIdMascota(idMascota);
        return mascotaGateway.guardar(mascota);
    }

    public void eliminar(Long idMascota) {
        buscarPorId(idMascota);
        mascotaGateway.eliminar(idMascota);
    }

    /** API extra: todas las mascotas registradas de un dueno. */
    public List<Mascota> listarPorDueno(String duenoId) {
        if (duenoId == null || duenoId.isBlank()) {
            throw new IllegalArgumentException("El duenoId es obligatorio");
        }
        return mascotaGateway.listarPorDueno(duenoId);
    }

    private void validar(Mascota mascota) {
        if (mascota == null) {
            throw new IllegalArgumentException("Los datos de la mascota son obligatorios");
        }
        if (mascota.getNombre() == null || mascota.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre de la mascota es obligatorio");
        }
        if (mascota.getEspecie() == null || mascota.getEspecie().isBlank()) {
            throw new IllegalArgumentException("La especie es obligatoria (Perro, Gato, ...)");
        }
        if (mascota.getEdad() != null && mascota.getEdad() < 0) {
            throw new IllegalArgumentException("La edad no puede ser negativa");
        }
        if (mascota.getPeso() != null && mascota.getPeso().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El peso debe ser mayor que cero");
        }
        if (mascota.getDuenoId() == null || mascota.getDuenoId().isBlank()) {
            throw new IllegalArgumentException("El duenoId es obligatorio");
        }
    }
}
