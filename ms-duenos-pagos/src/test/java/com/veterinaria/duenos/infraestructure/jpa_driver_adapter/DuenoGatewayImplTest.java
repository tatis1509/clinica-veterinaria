package com.veterinaria.duenos.infraestructure.jpa_driver_adapter;

import com.veterinaria.duenos.domain.model.Dueno;
import com.veterinaria.duenos.infraestructure.mapper.DuenoMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DuenoGatewayImplTest {

    @Mock
    private DuenoRepositoryJPA repository;
    private DuenoGatewayImpl gateway;

    private final DuenoEntity entity = new DuenoEntity("d1", "Ana", "1010", "ana@correo.com", "hash",
            "3001234567", "Calle 1", new BigDecimal("1000.00"));

    @BeforeEach
    void setUp() {
        gateway = new DuenoGatewayImpl(repository, new DuenoMapper());
    }

    @Test
    void guardar() {
        when(repository.save(any(DuenoEntity.class))).thenReturn(entity);
        Dueno guardado = gateway.guardar(new Dueno(null, "Ana", "1010", "ana@correo.com", "hash",
                "3001234567", "Calle 1", BigDecimal.ZERO));
        assertEquals("d1", guardado.getIdDueno());
    }

    @Test
    void buscarPorIdYCorreoYListar() {
        when(repository.findById("d1")).thenReturn(Optional.of(entity));
        when(repository.findByCorreo("ana@correo.com")).thenReturn(Optional.of(entity));
        when(repository.findAll()).thenReturn(List.of(entity));

        assertTrue(gateway.buscarPorId("d1").isPresent());
        assertTrue(gateway.buscarPorCorreo("ana@correo.com").isPresent());
        assertEquals(1, gateway.listar().size());
    }

    @Test
    void actualizarConservaElSaldoYCambiaLaClave() {
        when(repository.findByIdForUpdate("d1")).thenReturn(Optional.of(entity));
        when(repository.save(entity)).thenReturn(entity);

        Dueno cambios = new Dueno("d1", "Ana Maria", "1010", "ana@correo.com", "nuevoHash",
                "3009999999", "Calle 2", new BigDecimal("999999"));
        Dueno actualizado = gateway.actualizar(cambios);

        assertEquals("Ana Maria", actualizado.getNombre());
        assertEquals("nuevoHash", actualizado.getClave());
        assertEquals(new BigDecimal("1000.00"), actualizado.getSaldo());
    }

    @Test
    void eliminar() {
        gateway.eliminar("d1");
        verify(repository).deleteById("d1");
    }
}
