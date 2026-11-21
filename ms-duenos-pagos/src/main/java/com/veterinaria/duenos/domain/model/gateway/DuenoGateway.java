package com.veterinaria.duenos.domain.model.gateway;

import com.veterinaria.duenos.domain.model.Dueno;

import java.util.List;
import java.util.Optional;

/** Puerto de persistencia de duenos. El dominio no sabe que detras hay JPA/PostgreSQL. */
public interface DuenoGateway {

    Dueno guardar(Dueno dueno);

    Optional<Dueno> buscarPorId(String idDueno);

    Optional<Dueno> buscarPorCorreo(String correo);

    List<Dueno> listar();

    /** Actualiza solo los datos del perfil; el saldo NUNCA se modifica por aqui. */
    Dueno actualizar(Dueno dueno);

    void eliminar(String idDueno);
}
