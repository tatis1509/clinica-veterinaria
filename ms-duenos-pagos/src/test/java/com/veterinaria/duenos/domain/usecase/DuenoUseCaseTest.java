package com.veterinaria.duenos.domain.usecase;

import com.veterinaria.duenos.domain.model.Dueno;
import com.veterinaria.duenos.domain.model.exception.CredencialesInvalidasException;
import com.veterinaria.duenos.domain.model.exception.DuenoNoEncontradoException;
import com.veterinaria.duenos.domain.model.exception.ReglaNegocioException;
import com.veterinaria.duenos.domain.model.gateway.DuenoGateway;
import com.veterinaria.duenos.domain.model.gateway.EncrypterGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DuenoUseCaseTest {

    @Mock
    private DuenoGateway duenoGateway;
    @Mock
    private EncrypterGateway encrypterGateway;
    @InjectMocks
    private DuenoUseCase useCase;

    private Dueno nuevoDueno() {
        return new Dueno(null, "Ana Gomez", "1010", "Ana@Correo.com", "Clave123", "3001234567", "Calle 1", null);
    }

    @Test
    void registrarEncriptaClaveYArrancaConSaldoCero() {
        when(duenoGateway.buscarPorCorreo("ana@correo.com")).thenReturn(Optional.empty());
        when(encrypterGateway.encrypt("Clave123")).thenReturn("hash");
        when(duenoGateway.guardar(any(Dueno.class))).thenAnswer(inv -> {
            Dueno d = inv.getArgument(0);
            d.setIdDueno("d1");
            return d;
        });

        Dueno guardado = useCase.registrar(nuevoDueno());

        assertEquals("d1", guardado.getIdDueno());
        assertEquals("hash", guardado.getClave());
        assertEquals("ana@correo.com", guardado.getCorreo());
        assertEquals(Dueno.SALDO_INICIAL, guardado.getSaldo());
    }

    @Test
    void registrarConCorreoRepetidoFalla() {
        when(duenoGateway.buscarPorCorreo("ana@correo.com")).thenReturn(Optional.of(nuevoDueno()));
        assertThrows(ReglaNegocioException.class, () -> useCase.registrar(nuevoDueno()));
    }

    @Test
    void registrarConDatosInvalidosFalla() {
        Dueno sinCorreo = nuevoDueno();
        sinCorreo.setCorreo("no-es-correo");
        assertThrows(IllegalArgumentException.class, () -> useCase.registrar(sinCorreo));

        Dueno telefonoMalo = nuevoDueno();
        telefonoMalo.setTelefono("abc");
        assertThrows(IllegalArgumentException.class, () -> useCase.registrar(telefonoMalo));

        Dueno claveDebil = nuevoDueno();
        claveDebil.setClave("123");
        assertThrows(IllegalArgumentException.class, () -> useCase.registrar(claveDebil));

        assertThrows(IllegalArgumentException.class, () -> useCase.registrar(null));
    }

    @Test
    void buscarPorIdDevuelveElDueno() {
        Dueno dueno = nuevoDueno();
        when(duenoGateway.buscarPorId("d1")).thenReturn(Optional.of(dueno));
        assertEquals(dueno, useCase.buscarPorId("d1"));
    }

    @Test
    void buscarPorIdInexistenteFalla() {
        when(duenoGateway.buscarPorId("x")).thenReturn(Optional.empty());
        assertThrows(DuenoNoEncontradoException.class, () -> useCase.buscarPorId("x"));
    }

    @Test
    void listarDevuelveTodos() {
        when(duenoGateway.listar()).thenReturn(List.of(nuevoDueno()));
        assertEquals(1, useCase.listar().size());
    }

    @Test
    void actualizarConClaveNuevaLaEncripta() {
        when(duenoGateway.buscarPorId("d1")).thenReturn(Optional.of(nuevoDueno()));
        when(encrypterGateway.encrypt("Clave123")).thenReturn("hash2");
        when(duenoGateway.actualizar(any(Dueno.class))).thenAnswer(inv -> inv.getArgument(0));

        Dueno actualizado = useCase.actualizar("d1", nuevoDueno());

        assertEquals("d1", actualizado.getIdDueno());
        assertEquals("hash2", actualizado.getClave());
    }

    @Test
    void actualizarSinClaveConservaLaActual() {
        Dueno sinClave = nuevoDueno();
        sinClave.setClave(null);
        when(duenoGateway.buscarPorId("d1")).thenReturn(Optional.of(nuevoDueno()));
        when(duenoGateway.actualizar(any(Dueno.class))).thenAnswer(inv -> inv.getArgument(0));

        Dueno actualizado = useCase.actualizar("d1", sinClave);

        assertNull(actualizado.getClave());
        verify(encrypterGateway, never()).encrypt(any());
    }

    @Test
    void eliminarBorraElDueno() {
        when(duenoGateway.buscarPorId("d1")).thenReturn(Optional.of(nuevoDueno()));
        useCase.eliminar("d1");
        verify(duenoGateway).eliminar("d1");
    }

    @Test
    void loginConCredencialesValidas() {
        Dueno dueno = new Dueno("d1", "Ana", null, "ana@correo.com", "hash", "3001234567", null, BigDecimal.ZERO);
        when(duenoGateway.buscarPorCorreo("ana@correo.com")).thenReturn(Optional.of(dueno));
        when(encrypterGateway.checkPass("Clave123", "hash")).thenReturn(true);

        assertEquals("d1", useCase.login("ana@correo.com", "Clave123").getIdDueno());
    }

    @Test
    void loginConClaveIncorrectaFalla() {
        when(duenoGateway.buscarPorCorreo("ana@correo.com")).thenReturn(Optional.empty());
        assertThrows(CredencialesInvalidasException.class, () -> useCase.login("ana@correo.com", "otra"));
        assertThrows(IllegalArgumentException.class, () -> useCase.login(" ", "otra"));
    }
}
