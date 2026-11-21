package com.veterinaria.duenos.infraestructure.entry_points;

import com.veterinaria.duenos.domain.model.MovimientoSaldo;
import com.veterinaria.duenos.domain.usecase.PagoUseCase;
import com.veterinaria.duenos.infraestructure.entry_points.dto.DescuentoPeticion;
import com.veterinaria.duenos.infraestructure.entry_points.dto.RecargaPeticion;
import com.veterinaria.duenos.infraestructure.entry_points.dto.ReversoPeticion;
import com.veterinaria.duenos.infraestructure.entry_points.dto.SaldoRespuesta;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Monedero / credito veterinario del dueno. */
@RestController
@RequestMapping("/duenos")
@RequiredArgsConstructor
public class PagoController {

    private final PagoUseCase pagoUseCase;

    @GetMapping("/{id}/saldo")
    public ResponseEntity<SaldoRespuesta> consultarSaldo(@PathVariable String id) {
        return ResponseEntity.ok(new SaldoRespuesta(id, pagoUseCase.consultarSaldo(id)));
    }

    /** API extra 2: el dueno abona dinero a su cuenta. */
    @PostMapping("/saldo/recargar")
    public ResponseEntity<MovimientoSaldo> recargar(@RequestBody RecargaPeticion peticion) {
        return ResponseEntity.ok(pagoUseCase.recargar(peticion.duenoId(), peticion.monto()));
    }

    /** API extra 3: cobra una consulta, vacuna o bano. La usa ms-mascotas-atencion al confirmar la atencion. */
    @PostMapping("/saldo/descontar")
    public ResponseEntity<MovimientoSaldo> descontar(@RequestBody DescuentoPeticion peticion) {
        return ResponseEntity.ok(pagoUseCase.descontar(peticion.duenoId(), peticion.monto(),
                peticion.referencia(), peticion.descripcion()));
    }

    /** API extra 4: devuelve el dinero si se cancela una cita pagada. */
    @PostMapping("/saldo/reversar")
    public ResponseEntity<MovimientoSaldo> reversar(@RequestBody ReversoPeticion peticion) {
        return ResponseEntity.ok(pagoUseCase.reversar(peticion.duenoId(), peticion.referencia()));
    }

    /** API extra 5: historial de recargas y cobros veterinarios. */
    @GetMapping("/{id}/movimientos")
    public ResponseEntity<List<MovimientoSaldo>> movimientos(@PathVariable String id) {
        return ResponseEntity.ok(pagoUseCase.movimientos(id));
    }
}
