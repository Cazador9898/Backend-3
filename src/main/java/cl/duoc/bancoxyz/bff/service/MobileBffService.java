package cl.duoc.bancoxyz.bff.service;

import cl.duoc.bancoxyz.backend.service.AccountBackendService;
import cl.duoc.bancoxyz.backend.service.AnalyticsBackendService;
import cl.duoc.bancoxyz.backend.service.MovementBackendService;
import cl.duoc.bancoxyz.bff.dto.MobileSummaryResponse;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * BFF Móvil: reduce el payload a datos esenciales y solo 3 movimientos.
 */
@Service
public class MobileBffService {

    private final AccountBackendService accountBackend;
    private final MovementBackendService movementBackend;
    private final AnalyticsBackendService analyticsBackend;

    public MobileBffService(AccountBackendService accountBackend,
                            MovementBackendService movementBackend,
                            AnalyticsBackendService analyticsBackend) {
        this.accountBackend = accountBackend;
        this.movementBackend = movementBackend;
        this.analyticsBackend = analyticsBackend;
    }

    public MobileSummaryResponse obtenerResumen(Long cuentaId) {
        var cuenta = accountBackend.obtenerCuenta(cuentaId);

        List<MobileSummaryResponse.MovimientoLigero> ultimos = movementBackend
                .obtenerUltimosMovimientos(cuentaId, 3)
                .stream()
                .map(m -> new MobileSummaryResponse.MovimientoLigero(
                        m.fecha(), m.tipo(), m.monto()))
                .toList();

        return new MobileSummaryResponse(
                cuenta.cuentaId(),
                cuenta.nombre(),
                cuenta.saldoFinal(),
                cuenta.tipo(),
                analyticsBackend.obtenerSaldoNetoAnual(cuentaId),
                ultimos
        );
    }
}
