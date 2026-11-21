package com.veterinaria.mascotas.infraestructure.jpa_driver_adapter;

import com.veterinaria.mascotas.domain.model.TipoAtencion;
import com.veterinaria.mascotas.domain.model.gateway.TipoAtencionGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TipoAtencionGatewayImpl implements TipoAtencionGateway {

    private final TipoAtencionRepositoryJPA repository;

    @Override
    public TipoAtencion guardar(TipoAtencion t) {
        return toDomain(repository.save(new TipoAtencionEntity(t.getIdTipoAtencion(), t.getNombre(),
                t.getDescripcion(), t.getPrecio(), t.getDuracionMinutos())));
    }

    @Override
    public Optional<TipoAtencion> buscarPorId(Long id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public List<TipoAtencion> listar() {
        return repository.findAll(Sort.by("idTipoAtencion")).stream().map(this::toDomain).toList();
    }

    @Override
    public void eliminar(Long id) {
        repository.deleteById(id);
    }

    private TipoAtencion toDomain(TipoAtencionEntity e) {
        return new TipoAtencion(e.getIdTipoAtencion(), e.getNombre(), e.getDescripcion(), e.getPrecio(),
                e.getDuracionMinutos());
    }
}
