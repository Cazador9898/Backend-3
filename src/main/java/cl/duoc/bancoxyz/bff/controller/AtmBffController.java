package cl.duoc.bancoxyz.bff.controller;

import cl.duoc.bancoxyz.bff.dto.AtmBalanceResponse;
import cl.duoc.bancoxyz.bff.dto.WithdrawalRequest;
import cl.duoc.bancoxyz.bff.dto.WithdrawalResponse;
import cl.duoc.bancoxyz.bff.service.AtmService;
import jakarta.validation.Valid;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bff/atm")
public class AtmBffController {

    private final AtmService atmService;

    public AtmBffController(AtmService atmService) {
        this.atmService = atmService;
    }

    @GetMapping("/cuentas/{cuentaId}/saldo")
    public ResponseEntity<AtmBalanceResponse> saldo(@PathVariable Long cuentaId) {
        return ResponseEntity.ok()
                .header("X-BFF-Channel", "ATM")
                .cacheControl(CacheControl.noStore())
                .body(atmService.consultarSaldo(cuentaId));
    }

    @PostMapping("/cuentas/{cuentaId}/retiros")
    public ResponseEntity<WithdrawalResponse> retirar(
            @PathVariable Long cuentaId,
            @RequestHeader("X-ATM-ID") String atmId,
            @Valid @RequestBody WithdrawalRequest request) {

        return ResponseEntity.ok()
                .header("X-BFF-Channel", "ATM")
                .cacheControl(CacheControl.noStore())
                .body(atmService.retirar(cuentaId, atmId, request.monto()));
    }
}
