package cl.duoc.bancoxyz.backend.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Servicio Backend de estados de cuenta y métricas procesadas. */
@Service
public class AnalyticsBackendService {

    private final JdbcTemplate jdbcTemplate;

    public AnalyticsBackendService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public EstadoAnual obtenerUltimoEstadoAnual(Long cuentaId) {
        List<EstadoAnual> estados = jdbcTemplate.query(
                """
                SELECT anio, total_depositos, total_retiros_compras, saldo_neto, cantidad_movimientos
                FROM estados_cuenta_anuales
                WHERE cuenta_id = ?
                ORDER BY anio DESC
                LIMIT 1
                """,
                (rs, rowNum) -> new EstadoAnual(
                        rs.getInt("anio"),
                        rs.getBigDecimal("total_depositos"),
                        rs.getBigDecimal("total_retiros_compras"),
                        rs.getBigDecimal("saldo_neto"),
                        rs.getInt("cantidad_movimientos")
                ),
                cuentaId
        );

        return estados.stream().findFirst().orElse(null);
    }

    public BigDecimal obtenerSaldoNetoAnual(Long cuentaId) {
        EstadoAnual estado = obtenerUltimoEstadoAnual(cuentaId);
        return estado == null ? BigDecimal.ZERO : estado.saldoNeto();
    }

    public List<ResumenDiario> obtenerResumenDiario(int limite) {
        int limiteSeguro = Math.max(1, Math.min(limite, 30));

        return jdbcTemplate.query(
                """
                SELECT fecha, total_transacciones, cantidad_anomalias, monto_total
                FROM resumen_transacciones_diarias
                ORDER BY fecha DESC
                LIMIT ?
                """,
                (rs, rowNum) -> new ResumenDiario(
                        rs.getDate("fecha").toLocalDate(),
                        rs.getInt("total_transacciones"),
                        rs.getInt("cantidad_anomalias"),
                        rs.getBigDecimal("monto_total")
                ),
                limiteSeguro
        );
    }

    public record EstadoAnual(
            Integer anio,
            BigDecimal totalDepositos,
            BigDecimal totalRetirosCompras,
            BigDecimal saldoNeto,
            Integer cantidadMovimientos
    ) {}

    public record ResumenDiario(
            LocalDate fecha,
            Integer totalTransacciones,
            Integer cantidadAnomalias,
            BigDecimal montoTotal
    ) {}
}
