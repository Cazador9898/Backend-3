package cl.duoc.bancoxyz.bff.service;

import cl.duoc.bancoxyz.backend.service.AccountBackendService;
import cl.duoc.bancoxyz.bff.dto.AtmBalanceResponse;
import cl.duoc.bancoxyz.bff.dto.WithdrawalResponse;
import cl.duoc.bancoxyz.bff.exception.BusinessException;
import cl.duoc.bancoxyz.bff.exception.ResourceNotFoundException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/** BFF ATM orientado solo a operaciones críticas: saldo y retiro. */
@Service
public class AtmService {

    private final JdbcTemplate jdbcTemplate;
    private final AccountBackendService accountBackend;

    public AtmService(JdbcTemplate jdbcTemplate, AccountBackendService accountBackend) {
        this.jdbcTemplate = jdbcTemplate;
        this.accountBackend = accountBackend;
    }

    public AtmBalanceResponse consultarSaldo(Long cuentaId) {
        return new AtmBalanceResponse(
                cuentaId,
                accountBackend.obtenerSaldo(cuentaId),
                "DISPONIBLE"
        );
    }

    @Transactional
    public WithdrawalResponse retirar(Long cuentaId, String atmId, BigDecimal monto) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT saldo_final FROM intereses_procesados WHERE cuenta_id = ? FOR UPDATE",
                cuentaId);

        if (rows.isEmpty()) {
            throw new ResourceNotFoundException("Cuenta " + cuentaId + " no encontrada");
        }

        BigDecimal saldoActual = (BigDecimal) rows.get(0).get("saldo_final");
        if (saldoActual.compareTo(monto) < 0) {
            throw new BusinessException("Saldo insuficiente para realizar el retiro");
        }

        BigDecimal saldoRestante = saldoActual.subtract(monto);

        jdbcTemplate.update(
                "UPDATE intereses_procesados SET saldo_final = ? WHERE cuenta_id = ?",
                saldoRestante, cuentaId);

        jdbcTemplate.update(
                """
                INSERT INTO atm_operaciones(cuenta_id, atm_id, tipo, monto, saldo_resultante)
                VALUES (?, ?, 'RETIRO', ?, ?)
                """,
                cuentaId, atmId, monto, saldoRestante);

        return new WithdrawalResponse(
                cuentaId,
                atmId,
                monto,
                saldoRestante,
                "Retiro realizado correctamente"
        );
    }
}
