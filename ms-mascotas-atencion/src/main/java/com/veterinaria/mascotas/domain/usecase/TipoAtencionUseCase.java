package com.veterinaria.mascotas.domain.usecase;

import com.veterinaria.mascotas.domain.model.TipoAtencion;
import com.veterinaria.mascotas.domain.model.exception.TipoAtencionNoEncontradoException;
import com.veterinaria.mascotas.domain.model.gateway.TipoAtencionGateway;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@RequiredArgsConstructor
public class TipoAtencionUseCase {

    private final TipoAtencionGateway tipoAtencionGateway;

    public TipoAtencion crear(TipoAtencion tipo) {
        validar(tipo);
        tipo.setIdTipoAtencion(null);
        return tipoAtencionGateway.guardar(tipo);
    }

    public TipoAtencion buscarPorId(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El idTipoAtencion es obligatorio");
        }
        return tipoAtencionGateway.buscarPorId(id).orElseThrow(() -> new TipoAtencionNoEncontradoException(id));
    }

    public List<TipoAtencion> listar() {
        return tipoAtencionGateway.listar();
    }

    public TipoAtencion actualizar(Long id, TipoAtencion tipo) {
        buscarPorId(id);
        validar(tipo);
        tipo.setIdTipoAtencion(id);
        return tipoAtencionGateway.guardar(tipo);
    }

    public void eliminar(Long id) {
        buscarPorId(id);
        tipoAtencionGateway.eliminar(id);
    }

    private void validar(TipoAtencion tipo) {
        if (tipo == null || tipo.getNombre() == null || tipo.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre del tipo de atencion es obligatorio");
        }
        if (tipo.getPrecio() == null || tipo.getPrecio().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor que cero");
        }
        if (tipo.getDuracionMinutos() == null || tipo.getDuracionMinutos() <= 0) {
            throw new IllegalArgumentException("La duracion en minutos debe ser mayor que cero");
        }
    }
}
