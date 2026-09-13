package cl.duoc.bancoxyz.bff.service;

import cl.duoc.bancoxyz.backend.service.AccountBackendService;
import cl.duoc.bancoxyz.backend.service.AnalyticsBackendService;
import cl.duoc.bancoxyz.backend.service.MovementBackendService;
import cl.duoc.bancoxyz.bff.dto.WebDashboardResponse;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * BFF Web: agrega información desde varios servicios Backend y entrega
 * una respuesta completa para una interfaz de escritorio.
 */
@Service
public class WebBffService {

    private final AccountBackendService accountBackend;
    private final MovementBackendService movementBackend;
    private final AnalyticsBackendService analyticsBackend;

    public WebBffService(AccountBackendService accountBackend,
                         MovementBackendService movementBackend,
                         AnalyticsBackendService analyticsBackend) {
        this.accountBackend = accountBackend;
        this.movementBackend = movementBackend;
        this.analyticsBackend = analyticsBackend;
    }

    public WebDashboardResponse obtenerDashboard(Long cuentaId) {
        var cuenta = accountBackend.obtenerCuenta(cuentaId);
        var estado = analyticsBackend.obtenerUltimoEstadoAnual(cuentaId);

        List<WebDashboardResponse.Movimiento> movimientos = movementBackend
                .obtenerUltimosMovimientos(cuentaId, 10)
                .stream()
                .map(m -> new WebDashboardResponse.Movimiento(
                        m.fecha(), m.tipo(), m.monto(), m.descripcion()))
                .toList();

        List<WebDashboardResponse.ResumenDiario> resumen = analyticsBackend
                .obtenerResumenDiario(7)
                .stream()
                .map(r -> new WebDashboardResponse.ResumenDiario(
                        r.fecha(), r.totalTransacciones(), r.cantidadAnomalias(), r.montoTotal()))
                .toList();

        WebDashboardResponse.EstadoAnual estadoWeb = estado == null ? null :
                new WebDashboardResponse.EstadoAnual(
                        estado.anio(),
                        estado.totalDepositos(),
                        estado.totalRetirosCompras(),
                        estado.saldoNeto(),
                        estado.cantidadMovimientos()
                );

        return new WebDashboardResponse(
                cuenta.cuentaId(),
                cuenta.nombre(),
                cuenta.tipo(),
                cuenta.saldoFinal(),
                cuenta.interesCalculado(),
                estadoWeb,
                movimientos,
                resumen
        );
    }
}
