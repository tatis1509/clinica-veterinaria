package com.veterinaria.duenos.infraestructure.mapper;

import com.veterinaria.duenos.domain.model.Dueno;
import com.veterinaria.duenos.domain.model.MovimientoSaldo;
import com.veterinaria.duenos.infraestructure.entry_points.dto.DuenoPeticion;
import com.veterinaria.duenos.infraestructure.entry_points.dto.DuenoRespuesta;
import com.veterinaria.duenos.infraestructure.jpa_driver_adapter.DuenoEntity;
import com.veterinaria.duenos.infraestructure.jpa_driver_adapter.MovimientoSaldoEntity;
import org.springframework.stereotype.Component;

/** Traduce entre el modelo de dominio, las entidades JPA y los DTO de la API. */
@Component
public class DuenoMapper {

    public Dueno toDomain(DuenoEntity e) {
        return new Dueno(e.getIdDueno(), e.getNombre(), e.getDocumento(), e.getCorreo(), e.getClave(),
                e.getTelefono(), e.getDireccion(), e.getSaldo());
    }

    public DuenoEntity toEntity(Dueno d) {
        return new DuenoEntity(d.getIdDueno(), d.getNombre(), d.getDocumento(), d.getCorreo(), d.getClave(),
                d.getTelefono(), d.getDireccion(), d.getSaldo());
    }

    public Dueno toDomain(DuenoPeticion p) {
        return new Dueno(null, p.nombre(), p.documento(), p.correo(), p.clave(), p.telefono(), p.direccion(), null);
    }

    /** La respuesta NUNCA incluye la clave. */
    public DuenoRespuesta toRespuesta(Dueno d) {
        return new DuenoRespuesta(d.getIdDueno(), d.getNombre(), d.getDocumento(), d.getCorreo(),
                d.getTelefono(), d.getDireccion(), d.getSaldo());
    }

    public MovimientoSaldo toDomain(MovimientoSaldoEntity e) {
        return new MovimientoSaldo(e.getIdMovimiento(), e.getIdDueno(), e.getTipo(), e.getMonto(),
                e.getSaldoResultante(), e.getReferencia(), e.getDescripcion(), e.getFecha());
    }
}
