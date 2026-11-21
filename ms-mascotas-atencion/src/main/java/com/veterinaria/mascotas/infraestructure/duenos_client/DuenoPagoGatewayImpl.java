package com.veterinaria.mascotas.infraestructure.duenos_client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.veterinaria.mascotas.domain.model.DuenoInfo;
import com.veterinaria.mascotas.domain.model.exception.DuenoNoEncontradoException;
import com.veterinaria.mascotas.domain.model.exception.ReglaNegocioException;
import com.veterinaria.mascotas.domain.model.exception.SaldoInsuficienteException;
import com.veterinaria.mascotas.domain.model.exception.ServicioNoDisponibleException;
import com.veterinaria.mascotas.domain.model.gateway.DuenoPagoGateway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

/** Adaptador REST: ms-mascotas-atencion le habla a ms-duenos-pagos, el unico dueno del dinero. */
@Component
public class DuenoPagoGatewayImpl implements DuenoPagoGateway {

    private final RestClient restClient;

    public DuenoPagoGatewayImpl(RestClient.Builder builder, @Value("${duenos.service.url}") String duenosUrl) {
        this.restClient = builder.baseUrl(duenosUrl + "/duenos").build();
    }

    @Override
    public DuenoInfo consultarDueno(String duenoId) {
        try {
            DuenoRespuesta r = restClient.get().uri("/{id}", duenoId).retrieve().body(DuenoRespuesta.class);
            if (r == null || r.idDueno() == null) {
                throw new ServicioNoDisponibleException("ms-duenos-pagos devolvio una respuesta vacia");
            }
            return new DuenoInfo(r.idDueno(), r.nombre(), r.correo(), r.telefono());
        } catch (HttpClientErrorException.NotFound e) {
            throw new DuenoNoEncontradoException(duenoId);
        } catch (HttpClientErrorException e) {
            throw new ReglaNegocioException("ms-duenos-pagos rechazo la consulta: " + e.getResponseBodyAsString());
        } catch (HttpServerErrorException | ResourceAccessException e) {
            throw noDisponible(e);
        }
    }

    @Override
    public BigDecimal descontar(String duenoId, BigDecimal monto, String referencia, String descripcion) {
        try {
            MovimientoRespuesta r = restClient.post().uri("/saldo/descontar")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new DescuentoPeticion(duenoId, monto, referencia, descripcion))
                    .retrieve()
                    .body(MovimientoRespuesta.class);
            if (r == null || r.saldoResultante() == null) {
                throw new ServicioNoDisponibleException("ms-duenos-pagos devolvio una respuesta vacia al cobrar");
            }
            return r.saldoResultante();
        } catch (HttpClientErrorException.NotFound e) {
            throw new DuenoNoEncontradoException(duenoId);
        } catch (HttpClientErrorException e) {
            String cuerpo = e.getResponseBodyAsString();
            if (cuerpo.contains("SALDO_INSUFICIENTE")) {
                throw new SaldoInsuficienteException("Saldo insuficiente en el monedero del dueno para pagar la atencion");
            }
            throw new ReglaNegocioException("ms-duenos-pagos rechazo el cobro: " + cuerpo);
        } catch (HttpServerErrorException | ResourceAccessException e) {
            throw noDisponible(e);
        }
    }

    @Override
    public boolean reversar(String duenoId, String referencia) {
        try {
            restClient.post().uri("/saldo/reversar")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ReversoPeticion(duenoId, referencia))
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (HttpClientErrorException.NotFound e) {
            return false; // no existia ningun cobro con esa referencia: nada que devolver
        } catch (HttpClientErrorException e) {
            throw new ReglaNegocioException("ms-duenos-pagos rechazo el reverso: " + e.getResponseBodyAsString());
        } catch (HttpServerErrorException | ResourceAccessException e) {
            throw noDisponible(e);
        }
    }

    private static ServicioNoDisponibleException noDisponible(RuntimeException e) {
        return new ServicioNoDisponibleException("No se pudo comunicar con ms-duenos-pagos: " + e.getMessage());
    }

    // ---- contrato JSON con ms-duenos-pagos ----

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DuenoRespuesta(String idDueno, String nombre, String correo, String telefono) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record MovimientoRespuesta(String idMovimiento, String tipo, BigDecimal monto, BigDecimal saldoResultante) {
    }

    public record DescuentoPeticion(String duenoId, BigDecimal monto, String referencia, String descripcion) {
    }

    public record ReversoPeticion(String duenoId, String referencia) {
    }
}
