package com.veterinaria.duenos.infraestructure.entry_points;

import com.veterinaria.duenos.domain.model.Dueno;
import com.veterinaria.duenos.domain.usecase.DuenoUseCase;
import com.veterinaria.duenos.infraestructure.entry_points.dto.DuenoPeticion;
import com.veterinaria.duenos.infraestructure.mapper.DuenoMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DuenoControllerTest {

    @Mock
    private DuenoUseCase useCase;
    private DuenoController controller;

    private final Dueno dueno = new Dueno("d1", "Ana", "1010", "ana@correo.com", "hash", "3001234567",
            "Calle 1", BigDecimal.ZERO);
    private final DuenoPeticion peticion = new DuenoPeticion("Ana", "1010", "ana@correo.com", "Clave123",
            "3001234567", "Calle 1");

    @BeforeEach
    void setUp() {
        controller = new DuenoController(useCase, new DuenoMapper());
    }

    @Test
    void registrarDevuelve201SinClave() {
        when(useCase.registrar(any(Dueno.class))).thenReturn(dueno);
        var respuesta = controller.registrar(peticion);
        assertEquals(HttpStatus.CREATED, respuesta.getStatusCode());
        assertEquals("d1", respuesta.getBody().idDueno());
    }

    @Test
    void listarBuscarActualizarEliminar() {
        when(useCase.listar()).thenReturn(List.of(dueno));
        when(useCase.buscarPorId("d1")).thenReturn(dueno);
        when(useCase.actualizar(eq("d1"), any(Dueno.class))).thenReturn(dueno);

        assertEquals(1, controller.listar().getBody().size());
        assertEquals("Ana", controller.buscar("d1").getBody().nombre());
        assertEquals("d1", controller.actualizar("d1", peticion).getBody().idDueno());
        assertEquals(HttpStatus.NO_CONTENT, controller.eliminar("d1").getStatusCode());
        verify(useCase).eliminar("d1");
    }

    @Test
    void loginCorrecto() {
        when(useCase.login("ana@correo.com", "Clave123")).thenReturn(dueno);
        var respuesta = controller.login(new com.veterinaria.duenos.infraestructure.entry_points.dto
                .LoginPeticion("ana@correo.com", "Clave123"));
        assertTrue(respuesta.getBody().autenticado());
        assertEquals("d1", respuesta.getBody().dueno().idDueno());
    }
}
