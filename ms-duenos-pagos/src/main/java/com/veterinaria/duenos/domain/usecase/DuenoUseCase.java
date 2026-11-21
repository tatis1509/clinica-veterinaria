package com.veterinaria.duenos.domain.usecase;

import com.veterinaria.duenos.domain.model.Dueno;
import com.veterinaria.duenos.domain.model.exception.CredencialesInvalidasException;
import com.veterinaria.duenos.domain.model.exception.DuenoNoEncontradoException;
import com.veterinaria.duenos.domain.model.exception.ReglaNegocioException;
import com.veterinaria.duenos.domain.model.gateway.DuenoGateway;
import com.veterinaria.duenos.domain.model.gateway.EncrypterGateway;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.regex.Pattern;

/** Reglas de negocio de los duenos: registro, consulta, actualizacion, eliminacion y login. */
@RequiredArgsConstructor
public class DuenoUseCase {

    private static final Pattern CORREO = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final Pattern TELEFONO = Pattern.compile("\\d{7,15}");
    private static final Pattern CLAVE = Pattern.compile("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).{8,}$");

    private final DuenoGateway duenoGateway;
    private final EncrypterGateway encrypterGateway;

    public Dueno registrar(Dueno dueno) {
        validarDatos(dueno);
        validarClave(dueno.getClave());
        if (duenoGateway.buscarPorCorreo(dueno.getCorreo().trim().toLowerCase()).isPresent()) {
            throw new ReglaNegocioException("Ya existe un dueno registrado con el correo " + dueno.getCorreo());
        }
        dueno.setIdDueno(null);
        dueno.setCorreo(dueno.getCorreo().trim().toLowerCase());
        dueno.setClave(encrypterGateway.encrypt(dueno.getClave()));
        // El saldo NUNCA viene del cliente
        dueno.setSaldo(Dueno.SALDO_INICIAL);
        return duenoGateway.guardar(dueno);
    }

    public Dueno buscarPorId(String idDueno) {
        validarTexto(idDueno, "idDueno");
        return duenoGateway.buscarPorId(idDueno)
                .orElseThrow(() -> new DuenoNoEncontradoException(idDueno));
    }

    public List<Dueno> listar() {
        return duenoGateway.listar();
    }

    public Dueno actualizar(String idDueno, Dueno dueno) {
        buscarPorId(idDueno);
        dueno.setIdDueno(idDueno);
        validarDatos(dueno);
        dueno.setCorreo(dueno.getCorreo().trim().toLowerCase());
        if (dueno.getClave() != null && !dueno.getClave().isBlank()) {
            validarClave(dueno.getClave());
            dueno.setClave(encrypterGateway.encrypt(dueno.getClave()));
        } else {
            dueno.setClave(null); // el gateway conserva la clave actual
        }
        return duenoGateway.actualizar(dueno);
    }

    public void eliminar(String idDueno) {
        buscarPorId(idDueno);
        duenoGateway.eliminar(idDueno);
    }

    /** Devuelve el dueno autenticado o lanza CredencialesInvalidasException (401). */
    public Dueno login(String correo, String clave) {
        validarTexto(correo, "correo");
        validarTexto(clave, "clave");
        return duenoGateway.buscarPorCorreo(correo.trim().toLowerCase())
                .filter(dueno -> encrypterGateway.checkPass(clave, dueno.getClave()))
                .orElseThrow(() -> new CredencialesInvalidasException("Correo o clave incorrectos"));
    }

    private void validarDatos(Dueno dueno) {
        if (dueno == null) {
            throw new IllegalArgumentException("Los datos del dueno son obligatorios");
        }
        validarTexto(dueno.getNombre(), "nombre");
        validarTexto(dueno.getCorreo(), "correo");
        if (!CORREO.matcher(dueno.getCorreo().trim()).matches()) {
            throw new IllegalArgumentException("El correo no tiene un formato valido");
        }
        if (dueno.getTelefono() == null || !TELEFONO.matcher(dueno.getTelefono()).matches()) {
            throw new IllegalArgumentException(
                    "Numero telefonico invalido, debe contener solo numeros y tener entre 7 y 15 digitos");
        }
    }

    private void validarClave(String clave) {
        if (clave == null || !CLAVE.matcher(clave).matches()) {
            throw new IllegalArgumentException(
                    "Clave invalida, utiliza al menos 8 caracteres con mayuscula, minuscula y numero");
        }
    }

    private void validarTexto(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("El campo " + campo + " es obligatorio");
        }
    }
}
