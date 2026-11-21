package com.veterinaria.mascotas.infraestructure.jpa_driver_adapter;

import com.veterinaria.mascotas.domain.model.EstadoOrden;
import com.veterinaria.mascotas.domain.model.ItemOrden;
import com.veterinaria.mascotas.domain.model.OrdenAtencion;
import com.veterinaria.mascotas.domain.model.exception.OrdenNoEncontradaException;
import com.veterinaria.mascotas.domain.model.gateway.OrdenAtencionGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Transactional
public class OrdenAtencionGatewayImpl implements OrdenAtencionGateway {

    private final OrdenAtencionRepositoryJPA repository;

    @Override
    public OrdenAtencion guardar(OrdenAtencion orden) {
        return toDomain(persistir(orden));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<OrdenAtencion> buscarPorId(String idOrden) {
        return repository.findById(idOrden).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<OrdenAtencion> buscarPendientePorMascota(Long mascotaId) {
        return repository.findFirstByMascotaIdAndEstado(mascotaId, EstadoOrden.PENDIENTE).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrdenAtencion> listarRealizadasPorMascota(Long mascotaId) {
        return repository.findByMascotaIdAndEstadoOrderByFechaAtencionDesc(mascotaId, EstadoOrden.REALIZADA)
                .stream().map(this::toDomain).toList();
    }

    @Override
    public void actualizarReferenciaPago(String idOrden, String nuevaReferencia) {
        repository.findById(idOrden).ifPresent(e -> e.setReferenciaPago(nuevaReferencia));
    }

    // ------------------------------------------------------------------ mapeo

    private OrdenAtencionEntity persistir(OrdenAtencion orden) {
        OrdenAtencionEntity entity = orden.getIdOrden() == null
                ? new OrdenAtencionEntity()
                : repository.findById(orden.getIdOrden())
                .orElseThrow(() -> new OrdenNoEncontradaException(orden.getIdOrden()));

        entity.setMascotaId(orden.getMascotaId());
        entity.setDuenoId(orden.getDuenoId());
        entity.setEstado(orden.getEstado());
        entity.setReferenciaPago(orden.getReferenciaPago());
        entity.setFechaCreacion(orden.getFechaCreacion());
        entity.setFechaAtencion(orden.getFechaAtencion());
        entity.setTotal(orden.getTotal());

        // Sincroniza items: actualiza los existentes, crea los nuevos y elimina los retirados
        Map<Long, ItemOrdenEntity> actuales = entity.getItems().stream()
                .collect(Collectors.toMap(ItemOrdenEntity::getTipoAtencionId, Function.identity()));
        Set<Long> vigentes = new HashSet<>();
        for (ItemOrden item : orden.getItems()) {
            vigentes.add(item.getTipoAtencionId());
            ItemOrdenEntity itemEntity = actuales.get(item.getTipoAtencionId());
            if (itemEntity == null) {
                itemEntity = new ItemOrdenEntity();
                itemEntity.setOrden(entity);
                itemEntity.setTipoAtencionId(item.getTipoAtencionId());
                entity.getItems().add(itemEntity);
            }
            itemEntity.setNombreServicio(item.getNombreServicio());
            itemEntity.setCantidad(item.getCantidad());
            itemEntity.setPrecioUnitario(item.getPrecioUnitario());
            itemEntity.setSubtotal(item.getSubtotal());
        }
        entity.getItems().removeIf(i -> !vigentes.contains(i.getTipoAtencionId()));
        return repository.save(entity);
    }

    private OrdenAtencion toDomain(OrdenAtencionEntity e) {
        List<ItemOrden> items = e.getItems().stream()
                .map(i -> new ItemOrden(i.getIdItem(), i.getTipoAtencionId(), i.getNombreServicio(),
                        i.getCantidad(), i.getPrecioUnitario()))
                .collect(Collectors.toCollection(ArrayList::new));
        return new OrdenAtencion(e.getIdOrden(), e.getMascotaId(), e.getDuenoId(), e.getEstado(),
                e.getReferenciaPago(), e.getFechaCreacion(), e.getFechaAtencion(), e.getTotal(), items);
    }
}
