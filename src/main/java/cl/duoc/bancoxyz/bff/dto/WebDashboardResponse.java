package cl.duoc.bancoxyz.bff.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record WebDashboardResponse(
        Long cuentaId,
        String nombre,
        String tipoCuenta,
        BigDecimal saldoActual,
        BigDecimal interesCalculado,
        EstadoAnual estadoAnual,
        List<Movimiento> movimientosRecientes,
        List<ResumenDiario> resumenTransacciones
) {
    public record EstadoAnual(
            Integer anio,
            BigDecimal totalDepositos,
            BigDecimal totalRetirosCompras,
            BigDecimal saldoNeto,
            Integer cantidadMovimientos
    ) {}

    public record Movimiento(
            LocalDate fecha,
            String tipo,
            BigDecimal monto,
            String descripcion
    ) {}

    public record ResumenDiario(
            LocalDate fecha,
            Integer totalTransacciones,
            Integer cantidadAnomalias,
            BigDecimal montoTotal
    ) {}
}
