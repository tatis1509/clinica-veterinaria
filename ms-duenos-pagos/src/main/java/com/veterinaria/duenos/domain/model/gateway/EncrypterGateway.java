package com.veterinaria.duenos.domain.model.gateway;

public interface EncrypterGateway {

    String encrypt(String clave);

    boolean checkPass(String claveIngresada, String claveGuardada);
}
