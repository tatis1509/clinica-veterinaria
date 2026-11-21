package com.veterinaria.duenos.infraestructure.jpa_driver_adapter;

import com.veterinaria.duenos.domain.model.Dueno;
import com.veterinaria.duenos.domain.model.exception.DuenoNoEncontradoException;
import com.veterinaria.duenos.domain.model.gateway.DuenoGateway;
import com.veterinaria.duenos.infraestructure.mapper.DuenoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DuenoGatewayImpl implements DuenoGateway {

    private final DuenoRepositoryJPA duenoRepository;
    private final DuenoMapper mapper;

    @Override
    public Dueno guardar(Dueno dueno) {
        return mapper.toDomain(duenoRepository.save(mapper.toEntity(dueno)));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Dueno> buscarPorId(String idDueno) {
        return duenoRepository.findById(idDueno).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Dueno> buscarPorCorreo(String correo) {
        return duenoRepository.findByCorreo(correo).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Dueno> listar() {
        return duenoRepository.findAll().stream().map(mapper::toDomain).toList();
    }

    /** Bloquea la fila y conserva el saldo de la BD: el saldo solo cambia con recargas/cobros/reversos. */
    @Override
    @Transactional
    public Dueno actualizar(Dueno dueno) {
        DuenoEntity entity = duenoRepository.findByIdForUpdate(dueno.getIdDueno())
                .orElseThrow(() -> new DuenoNoEncontradoException(dueno.getIdDueno()));
        entity.setNombre(dueno.getNombre());
        entity.setDocumento(dueno.getDocumento());
        entity.setCorreo(dueno.getCorreo());
        entity.setTelefono(dueno.getTelefono());
        entity.setDireccion(dueno.getDireccion());
        if (dueno.getClave() != null) {
            entity.setClave(dueno.getClave());
        }
        return mapper.toDomain(duenoRepository.save(entity));
    }

    @Override
    public void eliminar(String idDueno) {
        duenoRepository.deleteById(idDueno);
    }
}
