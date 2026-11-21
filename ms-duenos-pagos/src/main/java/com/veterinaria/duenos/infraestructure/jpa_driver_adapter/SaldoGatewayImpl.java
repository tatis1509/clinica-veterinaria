package com.veterinaria.duenos.infraestructure.jpa_driver_adapter;

import com.veterinaria.duenos.domain.model.Dueno;
import com.veterinaria.duenos.domain.model.MovimientoSaldo;
import com.veterinaria.duenos.domain.model.TipoMovimiento;
import com.veterinaria.duenos.domain.model.exception.DuenoNoEncontradoException;
import com.veterinaria.duenos.domain.model.exception.MovimientoNoEncontradoException;
import com.veterinaria.duenos.domain.model.exception.ReglaNegocioException;
import com.veterinaria.duenos.domain.model.gateway.SaldoGateway;
import com.veterinaria.duenos.infraestructure.mapper.DuenoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SaldoGatewayImpl implements SaldoGateway {

    private final DuenoRepositoryJPA duenoRepository;
    private final MovimientoSaldoRepositoryJPA movimientoRepository;
    private final DuenoMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public BigDecimal consultarSaldo(String idDueno) {
        return duenoRepository.findById(idDueno)
                .map(DuenoEntity::getSaldo)
                .orElseThrow(() -> new DuenoNoEncontradoException(idDueno));
    }

    @Override
    @Transactional
    public MovimientoSaldo recargar(String idDueno, BigDecimal monto, String referencia) {
        DuenoEntity entity = bloquearDueno(idDueno);
        Dueno dueno = mapper.toDomain(entity);
        dueno.acreditar(monto);
        entity.setSaldo(dueno.getSaldo());
        return registrar(entity, TipoMovimiento.RECARGA, monto, referencia, "Recarga de monedero");
    }

    /** Idempotente: si la referencia ya fue cobrada, devuelve ese movimiento sin volver a cobrar. */
    @Override
    @Transactional
    public MovimientoSaldo descontar(String idDueno, BigDecimal monto, String referencia, String descripcion) {
        DuenoEntity entity = bloquearDueno(idDueno);

        Optional<MovimientoSaldoEntity> previo =
                movimientoRepository.findByReferenciaAndTipo(referencia, TipoMovimiento.DESCUENTO);
        if (previo.isPresent()) {
            if (!previo.get().getIdDueno().equals(idDueno)) {
                throw new ReglaNegocioException("La referencia ya fue usada por otro dueno");
            }
            if (movimientoRepository.findByReferenciaAndTipo(referencia, TipoMovimiento.REVERSO).isPresent()) {
                throw new ReglaNegocioException(
                        "Ese cobro ya fue reversado; use una referencia nueva para volver a cobrar");
            }
            return mapper.toDomain(previo.get());
        }

        Dueno dueno = mapper.toDomain(entity);
        dueno.debitar(monto); // lanza SaldoInsuficienteException si no alcanza
        entity.setSaldo(dueno.getSaldo());
        return registrar(entity, TipoMovimiento.DESCUENTO, monto, referencia, descripcion);
    }

    /** Idempotente: si ya se reverso, devuelve el reverso existente. */
    @Override
    @Transactional
    public MovimientoSaldo reversar(String idDueno, String referencia) {
        DuenoEntity entity = bloquearDueno(idDueno);

        Optional<MovimientoSaldoEntity> reversoPrevio =
                movimientoRepository.findByReferenciaAndTipo(referencia, TipoMovimiento.REVERSO);
        if (reversoPrevio.isPresent()) {
            return mapper.toDomain(reversoPrevio.get());
        }

        MovimientoSaldoEntity cobro = movimientoRepository
                .findByReferenciaAndTipo(referencia, TipoMovimiento.DESCUENTO)
                .orElseThrow(() -> new MovimientoNoEncontradoException(referencia));
        if (!cobro.getIdDueno().equals(idDueno)) {
            throw new ReglaNegocioException("La referencia pertenece a otro dueno");
        }

        Dueno dueno = mapper.toDomain(entity);
        dueno.acreditar(cobro.getMonto());
        entity.setSaldo(dueno.getSaldo());
        return registrar(entity, TipoMovimiento.REVERSO, cobro.getMonto(), referencia,
                "Reverso de: " + cobro.getDescripcion());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovimientoSaldo> listarMovimientos(String idDueno) {
        if (!duenoRepository.existsById(idDueno)) {
            throw new DuenoNoEncontradoException(idDueno);
        }
        return movimientoRepository.findByIdDuenoOrderByFechaDesc(idDueno).stream()
                .map(mapper::toDomain).toList();
    }

    private DuenoEntity bloquearDueno(String idDueno) {
        return duenoRepository.findByIdForUpdate(idDueno)
                .orElseThrow(() -> new DuenoNoEncontradoException(idDueno));
    }

    private MovimientoSaldo registrar(DuenoEntity dueno, TipoMovimiento tipo, BigDecimal monto,
                                      String referencia, String descripcion) {
        MovimientoSaldoEntity mov = new MovimientoSaldoEntity();
        mov.setIdDueno(dueno.getIdDueno());
        mov.setTipo(tipo);
        mov.setMonto(monto);
        mov.setSaldoResultante(dueno.getSaldo());
        mov.setReferencia(referencia);
        mov.setDescripcion(descripcion);
        mov.setFecha(LocalDateTime.now());
        return mapper.toDomain(movimientoRepository.save(mov));
    }
}
