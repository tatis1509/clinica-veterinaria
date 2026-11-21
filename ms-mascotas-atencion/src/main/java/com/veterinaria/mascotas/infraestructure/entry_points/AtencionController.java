package com.veterinaria.mascotas.infraestructure.entry_points;

import com.veterinaria.mascotas.domain.model.OrdenAtencion;
import com.veterinaria.mascotas.domain.model.ResultadoAtencion;
import com.veterinaria.mascotas.domain.usecase.AtencionUseCase;
import com.veterinaria.mascotas.infraestructure.entry_points.dto.AgregarServicioPeticion;
import com.veterinaria.mascotas.infraestructure.entry_points.dto.AtencionConfirmadaRespuesta;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Ordenes de atencion / citas (espejo del CarritoController). */
@RestController
@RequestMapping("/atenciones")
@RequiredArgsConstructor
public class AtencionController {

    private final AtencionUseCase useCase;

    /** API extra 1: arma la orden agregando servicios ("Consulta + Vacuna") y suma el total. */
    @PostMapping("/{mascotaId}/agregar-servicio")
    public ResponseEntity<OrdenAtencion> agregarServicio(@PathVariable Long mascotaId,
                                                         @RequestBody AgregarServicioPeticion p) {
        return ResponseEntity.ok(useCase.agregarServicio(mascotaId, p.tipoAtencionId(), p.cantidad()));
    }

    /** API extra 2: cobra en ms-duenos-pagos, marca REALIZADA y envia el resumen por ms-notificaciones. */
    @PostMapping("/{id}/confirmar-atencion")
    public ResponseEntity<AtencionConfirmadaRespuesta> confirmar(@PathVariable String id) {
        ResultadoAtencion r = useCase.confirmarAtencion(id);
        return ResponseEntity.ok(new AtencionConfirmadaRespuesta(
                "Atencion realizada y cobrada. Se envio el resumen al dueno.", r.orden(), r.saldoRestante()));
    }

    /** API extra 4: historial medico / servicios realizados a la mascota. */
    @GetMapping("/historial-mascota/{mascotaId}")
    public ResponseEntity<List<OrdenAtencion>> historial(@PathVariable Long mascotaId) {
        return ResponseEntity.ok(useCase.historialMascota(mascotaId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrdenAtencion> consultar(@PathVariable String id) {
        return ResponseEntity.ok(useCase.consultarOrden(id));
    }

    /** Cancela la orden; si ya estaba pagada le devuelve el dinero al dueno (reverso en ms-duenos-pagos). */
    @PostMapping("/{id}/cancelar")
    public ResponseEntity<OrdenAtencion> cancelar(@PathVariable String id) {
        return ResponseEntity.ok(useCase.cancelarAtencion(id));
    }
}
