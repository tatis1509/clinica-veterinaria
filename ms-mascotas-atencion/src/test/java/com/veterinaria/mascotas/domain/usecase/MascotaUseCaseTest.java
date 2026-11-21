package com.veterinaria.mascotas.domain.usecase;

import com.veterinaria.mascotas.domain.model.DuenoInfo;
import com.veterinaria.mascotas.domain.model.Mascota;
import com.veterinaria.mascotas.domain.model.exception.MascotaNoEncontradaException;
import com.veterinaria.mascotas.domain.model.gateway.DuenoPagoGateway;
import com.veterinaria.mascotas.domain.model.gateway.MascotaGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MascotaUseCaseTest {

    @Mock
    private MascotaGateway mascotaGateway;
    @Mock
    private DuenoPagoGateway duenoPagoGateway;
    @InjectMocks
    private MascotaUseCase useCase;

    private Mascota toby(String duenoId) {
        return new Mascota(null, "Toby", "Perro", "Labrador", 4, new BigDecimal("25.5"), duenoId);
    }

    @Test
    void crearValidaQueElDuenoExista() {
        when(duenoPagoGateway.consultarDueno("d1")).thenReturn(new DuenoInfo("d1", "Ana", "a@b.com", "300"));
        when(mascotaGateway.guardar(any(Mascota.class))).thenAnswer(inv -> {
            Mascota m = inv.getArgument(0);
            m.setIdMascota(1L);
            return m;
        });
        assertEquals(1L, useCase.crear(toby("d1")).getIdMascota());
    }

    @Test
    void consultarListarYEliminar() {
        Mascota m = toby("d1");
        when(mascotaGateway.buscarPorId(1L)).thenReturn(Optional.of(m));
        when(mascotaGateway.listar()).thenReturn(List.of(m));
        when(mascotaGateway.listarPorDueno("d1")).thenReturn(List.of(m));

        assertEquals(m, useCase.buscarPorId(1L));
        assertEquals(1, useCase.listar().size());
        assertEquals(1, useCase.listarPorDueno("d1").size());
        useCase.eliminar(1L);
        verify(mascotaGateway).eliminar(1L);
    }

    @Test
    void actualizarConCambioDeDueno() {
        when(mascotaGateway.buscarPorId(1L)).thenReturn(Optional.of(toby("d1")));
        when(duenoPagoGateway.consultarDueno("d2")).thenReturn(new DuenoInfo("d2", "Luis", "l@b.com", "300"));
        when(mascotaGateway.guardar(any(Mascota.class))).thenAnswer(inv -> inv.getArgument(0));

        Mascota actualizada = useCase.actualizar(1L, toby("d2"));

        assertEquals(1L, actualizada.getIdMascota());
        assertEquals("d2", actualizada.getDuenoId());
    }

    @Test
    void validaciones() {
        when(mascotaGateway.buscarPorId(9L)).thenReturn(Optional.empty());
        assertThrows(MascotaNoEncontradaException.class, () -> useCase.buscarPorId(9L));
        assertThrows(IllegalArgumentException.class, () -> useCase.buscarPorId(null));
        assertThrows(IllegalArgumentException.class, () -> useCase.crear(null));
        assertThrows(IllegalArgumentException.class, () -> useCase.crear(new Mascota(null, " ", "Perro", null, 1, null, "d1")));
        assertThrows(IllegalArgumentException.class, () -> useCase.crear(new Mascota(null, "T", null, null, 1, null, "d1")));
        assertThrows(IllegalArgumentException.class, () -> useCase.crear(new Mascota(null, "T", "Gato", null, -1, null, "d1")));
        assertThrows(IllegalArgumentException.class, () -> useCase.crear(new Mascota(null, "T", "Gato", null, 1, BigDecimal.ZERO, "d1")));
        assertThrows(IllegalArgumentException.class, () -> useCase.crear(new Mascota(null, "T", "Gato", null, 1, null, null)));
        assertThrows(IllegalArgumentException.class, () -> useCase.listarPorDueno(""));
    }
}
