package com.veterinaria.duenos.infraestructure.entry_points;

import com.veterinaria.duenos.domain.model.Dueno;
import com.veterinaria.duenos.domain.usecase.DuenoUseCase;
import com.veterinaria.duenos.infraestructure.entry_points.dto.DuenoPeticion;
import com.veterinaria.duenos.infraestructure.entry_points.dto.DuenoRespuesta;
import com.veterinaria.duenos.infraestructure.entry_points.dto.LoginPeticion;
import com.veterinaria.duenos.infraestructure.entry_points.dto.LoginRespuesta;
import com.veterinaria.duenos.infraestructure.mapper.DuenoMapper;
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

/** CRUD de duenos + login. */
@RestController
@RequestMapping("/duenos")
@RequiredArgsConstructor
public class DuenoController {

    private final DuenoUseCase duenoUseCase;
    private final DuenoMapper mapper;

    @PostMapping
    public ResponseEntity<DuenoRespuesta> registrar(@RequestBody DuenoPeticion peticion) {
        Dueno guardado = duenoUseCase.registrar(mapper.toDomain(peticion));
        return new ResponseEntity<>(mapper.toRespuesta(guardado), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<DuenoRespuesta>> listar() {
        return ResponseEntity.ok(duenoUseCase.listar().stream().map(mapper::toRespuesta).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DuenoRespuesta> buscar(@PathVariable String id) {
        return ResponseEntity.ok(mapper.toRespuesta(duenoUseCase.buscarPorId(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DuenoRespuesta> actualizar(@PathVariable String id, @RequestBody DuenoPeticion peticion) {
        return ResponseEntity.ok(mapper.toRespuesta(duenoUseCase.actualizar(id, mapper.toDomain(peticion))));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable String id) {
        duenoUseCase.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    /** API extra 1: inicio de sesion y validacion de credenciales. */
    @PostMapping("/login")
    public ResponseEntity<LoginRespuesta> login(@RequestBody LoginPeticion peticion) {
        Dueno dueno = duenoUseCase.login(peticion.correo(), peticion.clave());
        return ResponseEntity.ok(new LoginRespuesta(true, "Credenciales validas", mapper.toRespuesta(dueno)));
    }
}
