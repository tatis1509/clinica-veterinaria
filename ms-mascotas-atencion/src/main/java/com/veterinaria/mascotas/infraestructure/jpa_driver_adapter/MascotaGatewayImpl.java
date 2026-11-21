package com.veterinaria.mascotas.infraestructure.jpa_driver_adapter;

import com.veterinaria.mascotas.domain.model.Mascota;
import com.veterinaria.mascotas.domain.model.gateway.MascotaGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MascotaGatewayImpl implements MascotaGateway {

    private final MascotaRepositoryJPA repository;

    @Override
    public Mascota guardar(Mascota m) {
        return toDomain(repository.save(new MascotaEntity(m.getIdMascota(), m.getNombre(), m.getEspecie(),
                m.getRaza(), m.getEdad(), m.getPeso(), m.getDuenoId())));
    }

    @Override
    public Optional<Mascota> buscarPorId(Long idMascota) {
        return repository.findById(idMascota).map(this::toDomain);
    }

    @Override
    public List<Mascota> listar() {
        return repository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public List<Mascota> listarPorDueno(String duenoId) {
        return repository.findByDuenoIdOrderByNombreAsc(duenoId).stream().map(this::toDomain).toList();
    }

    @Override
    public void eliminar(Long idMascota) {
        repository.deleteById(idMascota);
    }

    private Mascota toDomain(MascotaEntity e) {
        return new Mascota(e.getIdMascota(), e.getNombre(), e.getEspecie(), e.getRaza(), e.getEdad(),
                e.getPeso(), e.getDuenoId());
    }
}
