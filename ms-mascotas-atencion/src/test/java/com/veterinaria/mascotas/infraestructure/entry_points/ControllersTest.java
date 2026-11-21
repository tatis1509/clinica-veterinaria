package com.veterinaria.mascotas.infraestructure.entry_points;

import com.veterinaria.mascotas.domain.model.Mascota;
import com.veterinaria.mascotas.domain.model.OrdenAtencion;
import com.veterinaria.mascotas.domain.model.ResultadoAtencion;
import com.veterinaria.mascotas.domain.model.TipoAtencion;
import com.veterinaria.mascotas.domain.model.TriajeResultado;
import com.veterinaria.mascotas.domain.usecase.AtencionUseCase;
import com.veterinaria.mascotas.domain.usecase.MascotaUseCase;
import com.veterinaria.mascotas.domain.usecase.TipoAtencionUseCase;
import com.veterinaria.mascotas.domain.usecase.TriajeUseCase;
import com.veterinaria.mascotas.infraestructure.entry_points.dto.AgregarServicioPeticion;
import com.veterinaria.mascotas.infraestructure.entry_points.dto.MascotaPeticion;
import com.veterinaria.mascotas.infraestructure.entry_points.dto.TipoAtencionPeticion;
import com.veterinaria.mascotas.infraestructure.entry_points.dto.TriajePeticion;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ControllersTest {

    @Test
    void mascotaController() {
        MascotaUseCase useCase = mock(MascotaUseCase.class);
        TriajeUseCase triaje = mock(TriajeUseCase.class);
        MascotaController c = new MascotaController(useCase, triaje);
        Mascota toby = new Mascota(1L, "Toby", "Perro", "Labrador", 4, BigDecimal.TEN, "d1");
        MascotaPeticion p = new MascotaPeticion("Toby", "Perro", "Labrador", 4, BigDecimal.TEN, "d1");
        when(useCase.crear(any())).thenReturn(toby);
        when(useCase.listar()).thenReturn(List.of(toby));
        when(useCase.buscarPorId(1L)).thenReturn(toby);
        when(useCase.actualizar(eq(1L), any())).thenReturn(toby);
        when(useCase.listarPorDueno("d1")).thenReturn(List.of(toby));
        when(triaje.evaluar(any())).thenReturn(new TriajeResultado("ALTA", "Urgencias", "GEMINI", "x"));

        assertEquals(HttpStatus.CREATED, c.crear(p).getStatusCode());
        assertEquals(1, c.listar().getBody().size());
        assertEquals(toby, c.buscar(1L).getBody());
        assertEquals(toby, c.actualizar(1L, p).getBody());
        assertEquals(HttpStatus.NO_CONTENT, c.eliminar(1L).getStatusCode());
        assertEquals(1, c.listarPorDueno("d1").getBody().size());
        assertEquals("ALTA", c.triaje(new TriajePeticion("Perro", null, null, null, "convulsion")).getBody().nivelUrgencia());
        verify(useCase).eliminar(1L);
    }

    @Test
    void tipoAtencionController() {
        TipoAtencionUseCase useCase = mock(TipoAtencionUseCase.class);
        TipoAtencionController c = new TipoAtencionController(useCase);
        TipoAtencion t = new TipoAtencion(1L, "Consulta general", "", new BigDecimal("45000"), 30);
        TipoAtencionPeticion p = new TipoAtencionPeticion("Consulta general", "", new BigDecimal("45000"), 30);
        when(useCase.crear(any())).thenReturn(t);
        when(useCase.listar()).thenReturn(List.of(t));
        when(useCase.buscarPorId(1L)).thenReturn(t);
        when(useCase.actualizar(eq(1L), any())).thenReturn(t);

        assertEquals(HttpStatus.CREATED, c.crear(p).getStatusCode());
        assertEquals(1, c.listar().getBody().size());
        assertEquals(t, c.buscar(1L).getBody());
        assertEquals(t, c.actualizar(1L, p).getBody());
        assertEquals(HttpStatus.NO_CONTENT, c.eliminar(1L).getStatusCode());
    }

    @Test
    void atencionController() {
        AtencionUseCase useCase = mock(AtencionUseCase.class);
        AtencionController c = new AtencionController(useCase);
        OrdenAtencion orden = OrdenAtencion.nueva(1L, "d1", "ref");
        when(useCase.agregarServicio(1L, 2L, 1)).thenReturn(orden);
        when(useCase.confirmarAtencion("o1")).thenReturn(new ResultadoAtencion(orden, BigDecimal.TEN));
        when(useCase.historialMascota(1L)).thenReturn(List.of(orden));
        when(useCase.consultarOrden("o1")).thenReturn(orden);
        when(useCase.cancelarAtencion("o1")).thenReturn(orden);

        assertEquals(orden, c.agregarServicio(1L, new AgregarServicioPeticion(2L, 1)).getBody());
        assertEquals(BigDecimal.TEN, c.confirmar("o1").getBody().saldoRestante());
        assertEquals(1, c.historial(1L).getBody().size());
        assertEquals(orden, c.consultar("o1").getBody());
        assertEquals(orden, c.cancelar("o1").getBody());
    }
}
