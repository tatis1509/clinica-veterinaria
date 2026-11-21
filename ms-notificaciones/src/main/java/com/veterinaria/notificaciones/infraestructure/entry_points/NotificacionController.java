package com.veterinaria.notificaciones.infraestructure.entry_points;

import com.veterinaria.notificaciones.domain.model.EstadoNotificacion;
import com.veterinaria.notificaciones.domain.model.Notificacion;
import com.veterinaria.notificaciones.domain.model.RecordatorioVacuna;
import com.veterinaria.notificaciones.domain.model.exception.ValidacionNotificacionException;
import com.veterinaria.notificaciones.domain.usecase.NotificacionUseCase;
import com.veterinaria.notificaciones.infraestructure.entry_points.dto.EmailPeticion;
import com.veterinaria.notificaciones.infraestructure.entry_points.dto.EstadoPeticion;
import com.veterinaria.notificaciones.infraestructure.entry_points.dto.NotificacionPeticion;
import com.veterinaria.notificaciones.infraestructure.entry_points.dto.SmsPeticion;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * CRUD del historial de notificaciones + 5 APIs extra.
 * Los envios responden 200 si el proveedor acepto el mensaje y 502 si fallo (queda FALLIDA para reintentar).
 */
@RestController
@RequestMapping("/notificaciones")
@RequiredArgsConstructor
public class NotificacionController {

    private final NotificacionUseCase useCase;

    // ------------------------------------------------------------------ CRUD

    @PostMapping
    public ResponseEntity<Notificacion> crear(@RequestBody NotificacionPeticion p) {
        Notificacion n = new Notificacion();
        n.setCanal(p.canal());
        n.setTipo(p.tipo());
        n.setDestinatario(p.destinatario());
        n.setAsunto(p.asunto());
        n.setMensaje(p.mensaje());
        return new ResponseEntity<>(useCase.crear(n), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Notificacion>> listar() {
        return ResponseEntity.ok(useCase.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Notificacion> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(useCase.buscarPorId(id));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<Notificacion> actualizarEstado(@PathVariable Long id, @RequestBody EstadoPeticion p) {
        return ResponseEntity.ok(useCase.actualizarEstado(id, p.estado()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        useCase.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // ------------------------------------------------------------- APIs extra

    /** API extra 1: comprobante de atencion o receta por correo. */
    @PostMapping("/enviar-email")
    public ResponseEntity<Notificacion> enviarEmail(@RequestBody EmailPeticion p) {
        return responder(useCase.enviarEmail(p.destinatario(), p.asunto(), p.mensaje(), p.tipo()));
    }

    /** API extra 2: SMS por Twilio confirmando cita o resultado. */
    @PostMapping("/enviar-sms")
    public ResponseEntity<Notificacion> enviarSms(@RequestBody SmsPeticion p) {
        return responder(useCase.enviarSms(p.telefono(), p.mensaje(), p.tipo()));
    }

    /** API extra 3: recordatorio de proxima dosis de vacuna / desparasitacion. */
    @PostMapping("/recordatorio-vacuna")
    public ResponseEntity<List<Notificacion>> recordatorioVacuna(@RequestBody RecordatorioVacuna p) {
        if (p == null) {
            throw new ValidacionNotificacionException("El cuerpo de la peticion es obligatorio");
        }
        List<Notificacion> enviadas = useCase.recordatorioVacuna(p);
        boolean todasEnviadas = enviadas.stream().allMatch(n -> n.getEstado() == EstadoNotificacion.ENVIADA);
        return ResponseEntity.status(todasEnviadas ? HttpStatus.OK : HttpStatus.BAD_GATEWAY).body(enviadas);
    }

    /** API extra 4: todos los correos y SMS enviados a un dueno (correo o telefono). */
    @GetMapping("/historial/{destinatario}")
    public ResponseEntity<List<Notificacion>> historial(@PathVariable String destinatario) {
        return ResponseEntity.ok(useCase.historial(destinatario));
    }

    /** API extra 5: reenvia una notificacion FALLIDA. */
    @PostMapping("/{id}/reintentar")
    public ResponseEntity<Notificacion> reintentar(@PathVariable Long id) {
        return responder(useCase.reintentar(id));
    }

    private ResponseEntity<Notificacion> responder(Notificacion n) {
        HttpStatus estado = n.getEstado() == EstadoNotificacion.ENVIADA ? HttpStatus.OK : HttpStatus.BAD_GATEWAY;
        return ResponseEntity.status(estado).body(n);
    }
}
