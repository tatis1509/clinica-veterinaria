package com.veterinaria.mascotas.domain.model.gateway;

import com.veterinaria.mascotas.domain.model.Mascota;

import java.util.List;
import java.util.Optional;

public interface MascotaGateway {

    Mascota guardar(Mascota mascota);

    Optional<Mascota> buscarPorId(Long idMascota);

    List<Mascota> listar();

    List<Mascota> listarPorDueno(String duenoId);

    void eliminar(Long idMascota);
}
