package com.veterinaria.mascotas.infraestructure.entry_points;

import com.veterinaria.mascotas.domain.model.Mascota;
import com.veterinaria.mascotas.domain.model.TriajeResultado;
import com.veterinaria.mascotas.domain.model.TriajeSolicitud;
import com.veterinaria.mascotas.domain.usecase.MascotaUseCase;
import com.veterinaria.mascotas.domain.usecase.TriajeUseCase;
import com.veterinaria.mascotas.infraestructure.entry_points.dto.MascotaPeticion;
import com.veterinaria.mascotas.infraestructure.entry_points.dto.TriajePeticion;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/mascotas")
@RequiredArgsConstructor
public class MascotaController {

    private final MascotaUseCase mascotaUseCase;
    private final TriajeUseCase triajeUseCase;

    @PostMapping
    public ResponseEntity<Mascota> crear(@RequestBody MascotaPeticion p) {
        return new ResponseEntity<>(mascotaUseCase.crear(toDomain(p)), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Mascota>> listar() {
        return ResponseEntity.ok(mascotaUseCase.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Mascota> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(mascotaUseCase.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Mascota> actualizar(@PathVariable Long id, @RequestBody MascotaPeticion p) {
        return ResponseEntity.ok(mascotaUseCase.actualizar(id, toDomain(p)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        mascotaUseCase.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    /** API extra 3: todas las mascotas de un dueno. */
    @GetMapping("/dueno/{duenoId}")
    public ResponseEntity<List<Mascota>> listarPorDueno(@PathVariable String duenoId) {
        return ResponseEntity.ok(mascotaUseCase.listarPorDueno(duenoId));
    }

    /** API extra 5 (API externa de IA - Gemini): nivel de urgencia y recomendaciones preliminares. */
    @PostMapping("/triaje-ia")
    public ResponseEntity<TriajeResultado> triaje(@RequestBody TriajePeticion p) {
        return ResponseEntity.ok(triajeUseCase.evaluar(
                new TriajeSolicitud(p.especie(), p.raza(), p.edad(), p.peso(), p.sintomas())));
    }

    private Mascota toDomain(MascotaPeticion p) {
        return new Mascota(null, p.nombre(), p.especie(), p.raza(), p.edad(), p.peso(), p.duenoId());
    }
}
