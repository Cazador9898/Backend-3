package cl.duoc.bancoxyz.backend.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Servicio Backend responsable de movimientos de cuenta. */
@Service
public class MovementBackendService {

    private final JdbcTemplate jdbcTemplate;

    public MovementBackendService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<MovimientoDetalle> obtenerUltimosMovimientos(Long cuentaId, int limite) {
        int limiteSeguro = Math.max(1, Math.min(limite, 20));

        return jdbcTemplate.query(
                """
                SELECT fecha, transaccion, monto, descripcion
                FROM movimientos_anuales
                WHERE cuenta_id = ?
                ORDER BY fecha DESC, id DESC
                LIMIT ?
                """,
                (rs, rowNum) -> new MovimientoDetalle(
                        rs.getDate("fecha").toLocalDate(),
                        rs.getString("transaccion"),
                        rs.getBigDecimal("monto"),
                        rs.getString("descripcion")
                ),
                cuentaId,
                limiteSeguro
        );
    }

    public record MovimientoDetalle(
            LocalDate fecha,
            String tipo,
            BigDecimal monto,
            String descripcion
    ) {}
}
