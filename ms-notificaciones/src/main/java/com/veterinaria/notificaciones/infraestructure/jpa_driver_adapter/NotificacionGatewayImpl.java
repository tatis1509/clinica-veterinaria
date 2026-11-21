package com.veterinaria.notificaciones.infraestructure.jpa_driver_adapter;

import com.veterinaria.notificaciones.domain.model.Notificacion;
import com.veterinaria.notificaciones.domain.model.gateway.NotificacionGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class NotificacionGatewayImpl implements NotificacionGateway {

    private final NotificacionRepositoryJPA repository;

    @Override
    public Notificacion guardar(Notificacion notificacion) {
        return toDomain(repository.save(toEntity(notificacion)));
    }

    @Override
    public Optional<Notificacion> buscarPorId(Long id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Notificacion> listar() {
        return repository.findAll(Sort.by(Sort.Direction.DESC, "fechaCreacion")).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Notificacion> listarPorDestinatario(String destinatario) {
        return repository.findByDestinatarioOrderByFechaCreacionDesc(destinatario).stream()
                .map(this::toDomain).toList();
    }

    @Override
    public void eliminar(Long id) {
        repository.deleteById(id);
    }

    private NotificacionEntity toEntity(Notificacion n) {
        NotificacionEntity e = new NotificacionEntity();
        e.setIdNotificacion(n.getIdNotificacion());
        e.setCanal(n.getCanal());
        e.setTipo(n.getTipo());
        e.setDestinatario(n.getDestinatario());
        e.setAsunto(n.getAsunto());
        e.setMensaje(n.getMensaje());
        e.setEstado(n.getEstado());
        e.setIdProveedor(n.getIdProveedor());
        e.setIntentos(n.getIntentos());
        e.setDetalleError(recortar(n.getDetalleError()));
        e.setFechaCreacion(n.getFechaCreacion());
        e.setFechaEnvio(n.getFechaEnvio());
        return e;
    }

    private Notificacion toDomain(NotificacionEntity e) {
        return new Notificacion(e.getIdNotificacion(), e.getCanal(), e.getTipo(), e.getDestinatario(),
                e.getAsunto(), e.getMensaje(), e.getEstado(), e.getIdProveedor(), e.getIntentos(),
                e.getDetalleError(), e.getFechaCreacion(), e.getFechaEnvio());
    }

    private static String recortar(String texto) {
        return texto != null && texto.length() > 1000 ? texto.substring(0, 1000) : texto;
    }
}
