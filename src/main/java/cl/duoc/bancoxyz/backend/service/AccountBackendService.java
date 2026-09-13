package cl.duoc.bancoxyz.backend.service;

import cl.duoc.bancoxyz.bff.exception.ResourceNotFoundException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * Simula el servicio Backend de cuentas sobre los datos legacy ya procesados.
 * Los BFF consumen este servicio en vez de consultar directamente la tabla.
 */
@Service
public class AccountBackendService {

    private final JdbcTemplate jdbcTemplate;

    public AccountBackendService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public CuentaDetalle obtenerCuenta(Long cuentaId) {
        List<CuentaDetalle> cuentas = jdbcTemplate.query(
                """
                SELECT cuenta_id, nombre, tipo, interes_calculado, saldo_final
                FROM intereses_procesados
                WHERE cuenta_id = ?
                """,
                (rs, rowNum) -> new CuentaDetalle(
                        rs.getLong("cuenta_id"),
                        rs.getString("nombre"),
                        rs.getString("tipo"),
                        rs.getBigDecimal("interes_calculado"),
                        rs.getBigDecimal("saldo_final")
                ),
                cuentaId
        );

        return cuentas.stream()
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe información procesada para la cuenta " + cuentaId));
    }

    public BigDecimal obtenerSaldo(Long cuentaId) {
        return obtenerCuenta(cuentaId).saldoFinal();
    }

    public record CuentaDetalle(
            Long cuentaId,
            String nombre,
            String tipo,
            BigDecimal interesCalculado,
            BigDecimal saldoFinal
    ) {}
}
