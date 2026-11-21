package com.veterinaria.duenos.infraestructure.security;

import com.veterinaria.duenos.domain.model.gateway.EncrypterGateway;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/** BCrypt con fuerza 8: seguro y lo bastante rapido para cumplir los 500 ms del login en Render. */
@Service
public class EncrypterGatewayImpl implements EncrypterGateway {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(8);

    @Override
    public String encrypt(String clave) {
        return encoder.encode(clave);
    }

    @Override
    public boolean checkPass(String claveIngresada, String claveGuardada) {
        return encoder.matches(claveIngresada, claveGuardada);
    }
}
