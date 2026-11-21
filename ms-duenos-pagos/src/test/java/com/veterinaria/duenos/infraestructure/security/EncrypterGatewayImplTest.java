package com.veterinaria.duenos.infraestructure.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EncrypterGatewayImplTest {

    @Test
    void encriptaYValidaLaClave() {
        EncrypterGatewayImpl encrypter = new EncrypterGatewayImpl();
        String hash = encrypter.encrypt("Clave123");
        assertNotEquals("Clave123", hash);
        assertTrue(encrypter.checkPass("Clave123", hash));
    }
}
