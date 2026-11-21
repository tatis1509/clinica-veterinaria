package com.veterinaria.mascotas.infraestructure.entry_points;

import com.veterinaria.mascotas.domain.model.TipoAtencion;
import com.veterinaria.mascotas.domain.usecase.TipoAtencionUseCase;
import com.veterinaria.mascotas.infraestructure.entry_points.dto.TipoAtencionPeticion;
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

/** CRUD de los servicios de la clinica (Consulta general, Vacunacion, ...). */
@RestController
@RequestMapping("/tipos-atencion")
@RequiredArgsConstructor
public class TipoAtencionController {

    private final TipoAtencionUseCase useCase;

    @PostMapping
    public ResponseEntity<TipoAtencion> crear(@RequestBody TipoAtencionPeticion p) {
        return new ResponseEntity<>(useCase.crear(toDomain(p)), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<TipoAtencion>> listar() {
        return ResponseEntity.ok(useCase.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TipoAtencion> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(useCase.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TipoAtencion> actualizar(@PathVariable Long id, @RequestBody TipoAtencionPeticion p) {
        return ResponseEntity.ok(useCase.actualizar(id, toDomain(p)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        useCase.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    private TipoAtencion toDomain(TipoAtencionPeticion p) {
        return new TipoAtencion(null, p.nombre(), p.descripcion(), p.precio(), p.duracionMinutos());
    }
}
